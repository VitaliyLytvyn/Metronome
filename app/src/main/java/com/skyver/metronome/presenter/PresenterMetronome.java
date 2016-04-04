package com.skyver.metronome.presenter;

import android.content.Context;


import com.skyver.metronome.MVP;
import com.skyver.metronome.common.GenericPresenter;
import com.skyver.metronome.model.ModelMetronome;
import com.skyver.metronome.model.WorkTypes;

import java.lang.ref.WeakReference;

/**
 * Created by Skyver on 21.03.2016.
 */
public class PresenterMetronome extends GenericPresenter<MVP.RequiredPresenterOps,
        MVP.ProvidedModelOps,
        ModelMetronome>
        implements MVP.ProvidedPresenterOps,  MVP.RequiredPresenterOps {

    /**
     * Debugging tag used by the Android logger.
     */
    protected final static String TAG =
            PresenterMetronome.class.getSimpleName();

    /**
     * Used to enable garbage collection.
     */
    protected WeakReference<MVP.RequiredViewOps> mView;


    /**
     * Default constructor.
     */
    public PresenterMetronome() {
    }


    @Override
    public void viewStarting() {
        getModel().startBinding();
    }

    @Override
    public void viewStopping() {
        getModel().stopBinding();
    }

    @Override
    public boolean stopAllWorks() {
        return getModel().StopAllWorks();
    }

    @Override
    public boolean startWorks(int interval, int soundId) {
        return getModel().startWorks(interval, soundId);
    }

    @Override
    public boolean changeSound(int newSoundId) {
        return getModel().changeSound(newSoundId);
    }

    @Override
    public boolean addWork(WorkTypes type) {
        return getModel().addWork(type);
    }

    @Override
    public boolean removeWork(WorkTypes type) {
        return getModel().removeWork(type);
    }

    @Override
    public boolean changeBPM(int newBBPM) {
        return getModel().changeBPM(newBBPM);
    }

    @Override
    public void showIndicatorOnUIThread() {
        if(mView.get() != null)
            mView.get().showIndicatorOnUIThread();
    }


    /**
     * Hook method called when a new instance of PresenterMetronome is
     * created.  One time initialization code goes here, e.g., storing
     * a WeakReference to the View layer and initializing the Model
     * layer.
     *
     * @param view
     *            A reference to the View layer.
     */
    @Override
    public void onCreate(MVP.RequiredViewOps view) {
        // Set the WeakReference.
        mView = new WeakReference<>(view);

        // Invoke the special onCreate() method in GenericPresenter,
        // passing in the ModelMetronome class to instantiate/manage and
        // "this" to provide ModelMetronome with this
        // MVP.RequiredModelOps instance.
        super.onCreate(ModelMetronome.class,
                this);
    }

    /**
     * Hook method dispatched by the GenericActivity framework to
     * initialize the PresenterMetronome object after a runtime
     * configuration change.
     *
     * @param view         The currently active PresenterMetronome.View.
     */
    @Override
    public void onConfigurationChange(MVP.RequiredViewOps view) {
        // Reset the mView WeakReference.
        mView = new WeakReference<>(view);
    }

    /**
     * Hook method called to shutdown the Presenter layer.
     *
     * @param isChangeConfigurations
     *        True if a runtime configuration triggered the onDestroy() call.
     */
    @Override
    public void onDestroy(boolean isChangingConfigurations) {
        // Destroy the model.
        getModel().onDestroy(isChangingConfigurations);
    }


    /**
     * Return the Activity context.
     */
    @Override
    public Context getActivityContext() {
        return mView.get().getActivityContext();
    }

    /**
     * Return the Application context.
     */
    @Override
    public Context getApplicationContext() {
        return mView.get().getApplicationContext();
    }

}