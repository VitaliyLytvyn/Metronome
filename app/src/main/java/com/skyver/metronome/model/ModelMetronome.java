package com.skyver.metronome.model;

/**
 * Created by Skyver on 21.03.2016.
 */

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.skyver.metronome.MVP;

import java.lang.ref.WeakReference;

/**
 * This class plays the "Model" role in the Model-View-Presenter (MVP)
 * pattern by defining an interface for providing data that will be
 * acted upon by the "Presenter" and "View" layers in the MVP pattern.
 * It implements the MVP.ProvidedModelOps so it can be created/managed
 * by the GenericPresenter framework.
 */
public class ModelMetronome extends Handler implements MVP.ProvidedModelOps {

    private ServiceForWork mService;
    /**
     * Debugging tag used by the Android logger.
     */
    protected final static String TAG =
            ModelMetronome.class.getSimpleName();

    //service connection flag
    private boolean mIsBound;

    private boolean mIsAllStopped;

    /**
     * Used to get connected to the ServiceForWork using bindService().
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /**
         * Called by the Android Binder framework after the
         * Service is connected
         */
        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            Log.d(TAG,  "onServiceConnected() "
                            + className);

            mService = ((ServiceForWork.LocalBinder) binder).getService();
        }

        /**
         * Called if the Service crashes and is no longer
         * available.  The ServiceConnection will remain bound,
         * but the Service will not respond to any requests.
         */
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG,
                    "onServiceDisconnected ");
            mService = null;
        }
    };

    /**
     * A WeakReference used to access methods in the Presenter layer.
     * The WeakReference enables garbage collection.
     */
    protected WeakReference<MVP.RequiredPresenterOps> mPresenter;

    @Override
    public void handleMessage(Message msg) {

        if(mPresenter.get() != null)
            mPresenter.get().showIndicatorOnUIThread();
        //super.handleMessage(msg);
    }

    /**
     * Hook method called when a new Model instance is created
     * to initialize the ServicConnections and bind to the Service.
     *
     * @param presenter
     *            A reference to the Presenter layer.
     */
    @Override
    public void onCreate(MVP.RequiredPresenterOps presenter) {

        // Set the WeakReference.
        mPresenter = new WeakReference<>(presenter);

        //start the service as 'Started'
        Log.i(TAG, "Service starting...");
        Intent intent = ServiceForWork
                .makeIntent(mPresenter.get().getApplicationContext(), this);

        mPresenter.get().getApplicationContext().startService(intent);
    }

    /**
     * Hook method called to shutdown the Presenter layer.
     */
    @Override
    public void onDestroy(boolean isChangingConfigurations) {
        // Don't bother unbinding the service if we're simply changing
        // configurations.
        if (isChangingConfigurations)
            Log.d(TAG,
                    "Simply changing configurations, no need to destroy the Service");
        else {
            if(mIsAllStopped){
                //stopping the service as 'Started' - kill service
                Log.d(TAG, "Service stopping...");
                Intent intent = ServiceForWork
                        .makeIntent(mPresenter.get().getApplicationContext(), this);
                mPresenter.get().getApplicationContext().stopService(intent);
            }

            mService = null;
            mServiceConnection = null;
        }
    }

    @Override
    public void startBinding() {
        Log.d(TAG,
                "calling bindService()");

            Intent intent = ServiceForWork
                    .makeIntent(mPresenter.get().getApplicationContext(), this);
            mIsBound = mPresenter.get().getApplicationContext()
                .bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void stopBinding() {
        Log.d(TAG,
                "calling unbindService()");
        // Unbind from the Service.
        if(mIsBound){
            mPresenter.get()
                    .getApplicationContext()
                    .unbindService(mServiceConnection);

            mIsBound = false;
        }
    }

    @Override
    public boolean StopAllWorks() {
        mIsAllStopped = true;

        if(mService != null){
            mService.stopWorks();
            return true;
        }
        else
            return false;
    }

    @Override
    public boolean changeSound(int newSoundId) {
        if(mService != null){
            mService.changeSound(newSoundId);
            return true;
        }
        else
            return false;
    }

    @Override
    public boolean startWorks(int interval, int soundId) {
        mIsAllStopped = false;
        if(mService != null){
            mService.startWorks(interval, soundId);
            return true;
        }
        else
            return false;
    }

    @Override
    public boolean addWork(WorkTypes type) {

        if(mService != null){
            return mService.addWork(type);
        }
        else
            return false;
    }

    @Override
    public boolean removeWork(WorkTypes type) {

        if(mService != null){
            return mService.removeWork(type);
        }
        else
            return false;
    }

    @Override
    public boolean changeBPM(int newBPM) {
        if(mService != null){
            mService.changeBPM(newBPM);
            return true;
        }
        else
            return false;
    }
}
