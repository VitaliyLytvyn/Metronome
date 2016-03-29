package com.skyver.metronome.common;

/**
 * Created by Skyver on 21.03.2016.
 */

import android.content.Context;

/**
 * Defines methods for obtaining Contexts used by all views in the
 * "View" layer.
 */
public interface ContextView {
    /**
     * Get the Activity Context.
     */
    Context getActivityContext();

    /**
     * Get the Application Context.
     */
    Context getApplicationContext();
}


