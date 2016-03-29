package com.skyver.metronome.model.works;

import android.content.Context;
import android.media.MediaPlayer;

import com.skyver.metronome.R;

/**
 * Created by Skyver on 23.03.2016.
 */
public class WorkRunSound extends WorkRunCommon {

    private MediaPlayer mPlayer;
    private static String TAG = WorkRunCommon.class.getSimpleName();
    Context context;

    public WorkRunSound(Context context) {

        this.context = context;
        initiatePlayer();
    }

    private void initiatePlayer() {

        // Set up the Media Player
        mPlayer = MediaPlayer.create(context, R.raw.metronome_up);

        if (mPlayer != null) {

            mPlayer.setLooping(false);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {

                    // Rewind to beginning
                    mPlayer.seekTo(0);
                }
            });
        }
    }

    @Override
    public void run() {

        // Start playing
        if (null != mPlayer) {
            mPlayer.start();
        }
    }

    @Override
    public void finalizeWorkObject() {

        if (mPlayer != null) {

            mPlayer.stop();
            mPlayer.release();
        }
        context = null;
    }
}
