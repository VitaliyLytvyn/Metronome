package com.skyver.metronome.model.works;

import android.hardware.Camera;
import android.util.Log;

/**
 * Created by Skyver on 25.03.2016.
 */
public class WorkRunFlash extends WorkRunCommon {

    private final static String TAG = WorkRunFlash.class.getSimpleName();

    private Camera mCamera;

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d(TAG, e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    public void run() {

        if(mCamera == null)
            mCamera = getCameraInstance();

        //Camera is not available (in use or does not exist)
        if(mCamera == null)
            return;

        // get Camera parameters
        Camera.Parameters params = mCamera.getParameters();
        // set the focus mode
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        // set Camera parameters
        mCamera.setParameters(params);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
        releaseCamera();
    }

    @Override
    public void finalizeWorkObject() {
        releaseCamera();
    }

}

