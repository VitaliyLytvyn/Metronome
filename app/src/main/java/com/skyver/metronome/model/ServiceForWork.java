package com.skyver.metronome.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;

import com.skyver.metronome.common.LifecycleLoggingService;
import com.skyver.metronome.model.works.WorkRunCommon;
import com.skyver.metronome.model.works.WorkRunFlash;
import com.skyver.metronome.model.works.WorkRunIndicator;
import com.skyver.metronome.model.works.WorkRunSound;
import com.skyver.metronome.model.works.WorkRunVibro;
import com.skyver.metronome.view.MainActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Skyver on 23.03.2016.
 */
public class ServiceForWork extends LifecycleLoggingService {

    public static Messenger mMessenger;

    private static final String MESSENGER = "MESSENGER";
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Reference to the ExecutorService that manages a pool of
     * threads.
     */
    private ScheduledThreadPoolExecutor mScheduledExecutor;

    private Map<WorkTypes, WorkRunCommon> mapCurrentWorks;

    private long mCurrentInterval;

    private boolean isWorksRunning;

    private int mBPM;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        ServiceForWork getService() {
            // Return this instance of Service so clients can call public methods
            return ServiceForWork.this;
        }
    }

    /**
     * Factory method that returns an explicit Intent for strting Service
     *
     */
    public static Intent makeIntent(Context context, Handler downloadHandler) {

        Intent intent = new Intent(context, ServiceForWork.class);
        intent.putExtra(MESSENGER, new Messenger(downloadHandler));
        return intent;
    }

    public void startWorks(int bpm){

        calculateNewInterval(bpm);

        for (WorkRunCommon work : mapCurrentWorks.values()){
            ScheduledFuture<?> future = mScheduledExecutor
                    .scheduleAtFixedRate(work, 0, mCurrentInterval, TimeUnit.MILLISECONDS);
            work.setRunningState(future);
        }
        isWorksRunning = true;
    }

    public void stopWorks(){

        for (WorkRunCommon work : mapCurrentWorks.values()){
            ScheduledFuture future = work.getRunningState();
            if(future != null)
                work.getRunningState().cancel(false);
        }
        isWorksRunning = false;
    }

    public boolean addWork(WorkTypes type) {

        WorkRunCommon work = null;
        if(!mapCurrentWorks.containsKey(type)){
            switch (type){
                case FLASH:
                    work = new WorkRunFlash();
                    break;
                case SOUND:
                    work = new WorkRunSound(this);
                    break;
                case VIBRO:
                    work = new WorkRunVibro(this);
                    break;
            }
            if(work == null)
                return false;
            mapCurrentWorks.put(type, work);
        }

        if(!isWorksRunning)
            return true;

        long lastStartTime = mapCurrentWorks.get(WorkTypes.INDICATOR).getLastStartedTime();
        long delay = lastStartTime + mCurrentInterval - System.currentTimeMillis();
        ScheduledFuture future = mScheduledExecutor
                .scheduleAtFixedRate(work, delay, mCurrentInterval, TimeUnit.MILLISECONDS);
        work.setRunningState(future);
        return true;
    }

    public boolean removeWork(WorkTypes type) {

        ScheduledFuture runTask;

        if(!mapCurrentWorks.containsKey(type))
            return true;

        if(isWorksRunning){
            runTask = mapCurrentWorks.get(type).getRunningState();
            if(runTask != null)
                runTask.cancel(false);
            if(!runTask.isCancelled())
                return false;
        }
        if(!type.equals(WorkTypes.INDICATOR)){

            WorkRunCommon work = mapCurrentWorks.get(type);
            work.finalizeWorkObject();

            mapCurrentWorks.remove(type);
            return true;
        }
        else
            return false;
    }

    public void changeBPM(int newBPM) {

        calculateNewInterval(newBPM);

        if (!isWorksRunning)
            return;

        stopWorks();

        startWorks(newBPM);
    }

    private void calculateNewInterval(int bpm){

        mBPM = bpm;
        mCurrentInterval = (long)(60.0 / bpm * 1000);
    }


    @Override
    public void onCreate() {
        super.onCreate();

        initialisation();
    }

    private void initialisation(){
        mScheduledExecutor = (ScheduledThreadPoolExecutor)
                Executors.newScheduledThreadPool(4);

        mapCurrentWorks = new HashMap<>();
        mapCurrentWorks.put(WorkTypes.SOUND, new WorkRunSound(this));
        mapCurrentWorks.put(WorkTypes.INDICATOR, new WorkRunIndicator());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(intent != null)
            mMessenger = intent.getParcelableExtra(MESSENGER);
        else
            restartService();

        return START_STICKY;
    }

    private void restartService(){

        initialisation();

        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if(sharedPref.getBoolean(MainActivity.BTN_FLASH, false))
            mapCurrentWorks.put(WorkTypes.FLASH, new WorkRunFlash());
        if(sharedPref.getBoolean(MainActivity.BTN_SOUND, false))
            mapCurrentWorks.remove(WorkTypes.SOUND);
        if(sharedPref.getBoolean(MainActivity.BTN_VIBRO, false))
            mapCurrentWorks.put(WorkTypes.VIBRO, new WorkRunVibro(this));

        mBPM = sharedPref.getInt(MainActivity.BPM_FIELD, MainActivity.DEFAULT_BPM);

        startWorks(mBPM);
    }

    @Override
    public void onDestroy() {
        if(mScheduledExecutor != null){
            mScheduledExecutor.shutdownNow();
            mScheduledExecutor = null;
        }
        mMessenger = null;

        for (WorkRunCommon work : mapCurrentWorks.values()){

            work.finalizeWorkObject();
        }
        mapCurrentWorks = null;

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return mBinder;
    }
}
