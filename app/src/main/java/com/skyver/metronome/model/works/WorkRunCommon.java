package com.skyver.metronome.model.works;

import java.util.concurrent.ScheduledFuture;

/**
 * Created by Skyver on 23.03.2016.
 */
//common model for tasks

public abstract class WorkRunCommon implements Runnable{

    //time to be able to count time for start new task for synchronous work
    //accessed by runnable from a different thread
    protected volatile long mLastStartedTime;

    //Future to access and cancel a work
    protected ScheduledFuture<?> runningState;

    public ScheduledFuture<?> getRunningState() {
        return runningState;
    }

    public void setRunningState(ScheduledFuture<?> runningState) {
        this.runningState = runningState;
    }

    public long getLastStartedTime(){
        return mLastStartedTime;
    }

    public abstract void finalizeWorkObject();
}
