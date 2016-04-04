package com.skyver.metronome.model.works;

import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.skyver.metronome.model.ServiceForWork;

/**
 * Created by Skyver on 23.03.2016.
 */
public class WorkRunIndicator extends WorkRunCommon {

    private static String TAG = WorkRunIndicator.class.getSimpleName();

    @Override
    public void run() {

        mLastStartedTime = System.currentTimeMillis();//////

        Message msg = Message.obtain();
        try {
            if(ServiceForWork.mMessenger != null)
                ServiceForWork.mMessenger.send(msg);
        } catch (RemoteException e) {
            Log.d(TAG,
                    "Exception while sending reply message back to Activity.",
                    e);
        }
    }

    @Override
    public void finalizeWorkObject() {
    }
}
