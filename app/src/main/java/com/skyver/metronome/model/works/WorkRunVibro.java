package com.skyver.metronome.model.works;

import android.content.Context;
import android.os.Vibrator;

/**
 * Created by Skyver on 25.03.2016.
 */
public class WorkRunVibro extends WorkRunCommon {

    private static String TAG = WorkRunCommon.class.getSimpleName();

    private Context mContext;

    private Vibrator vibro;

    public WorkRunVibro(Context context) {
        mContext = context;
        vibro = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void run() {

        vibro.vibrate(50);
    }

    @Override
    public void finalizeWorkObject() {

        if(vibro != null)
            vibro.cancel();
        vibro = null;
        mContext = null;
    }
}
