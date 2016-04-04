package com.skyver.metronome.model.works;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.skyver.metronome.R;

/**
 * Created by Skyver on 23.03.2016.
 */
public class WorkRunSound extends WorkRunCommon {

    private static String TAG = WorkRunCommon.class.getSimpleName();
    private Context context;

    private final int MAX_STREAMS = 1;

    private final int DEFAULT_SOUND = R.raw.variant_1;

    private SoundPool soundPool;
    private volatile boolean isLoaded;
    private int soundId;
    private int currentSound;


    public WorkRunSound(Context context, int newSound) {

        if(newSound == 0)
            currentSound = DEFAULT_SOUND;
        else currentSound = newSound;

        this.context = context;
        initiatePlayer();
    }

    private void initiatePlayer() {

        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

                isLoaded = true;
            }
        });

        soundId = soundPool.load(context, currentSound, 1);
    }

    public void setNewSoundId(int newSound){

        soundPool.unload(soundId);
        isLoaded = false;
        soundId = soundPool.load(context, newSound, 1);
    }

    @Override
    public void run() {

        if(isLoaded)
            soundPool.play(soundId, 1, 1, 0, 0, 1);

    }

    @Override
    public void finalizeWorkObject() {

        if(soundPool != null){
            soundPool.unload(soundId);
            soundPool.release();
            soundPool = null;
        }
        context = null;
    }
}
