package com.skyver.metronome;

/**
 * Created by Skyver on 21.03.2016.
 */

import com.skyver.metronome.common.ContextView;
import com.skyver.metronome.common.ModelOps;
import com.skyver.metronome.common.PresenterOps;
import com.skyver.metronome.model.WorkTypes;

/**
 * Defines the interfaces for the application
 * that are required and provided by the layers in the
 * Model-View-Presenter (MVP) pattern.  This design ensures loose
 * coupling between the layers in the app's MVP-based architecture.
 */
public interface MVP {
    /**
     * This interface defines the minimum API needed by the
     * PresenterMetronome class in the Presenter layer to interact with
     * MainActivity in the View layer.  It extends the
     * ContextView interface so the Model layer can access Context's
     * defined in the View layer.
     */
    public interface RequiredViewOps
            extends ContextView {

        public void showIndicatorOnUIThread();
    }

    /**
     * This interface defines the minimum public API provided by the
     * PresenterMetronome class in the Presenter layer to the
     * MainActivity in the View layer.  It extends the
     * PresenterOps interface, which is instantiated by the
     * MVP.RequiredViewOps interface used to define the parameter
     * that's passed to the onConfigurationChange() method.
     */
    public interface ProvidedPresenterOps
            extends PresenterOps<RequiredViewOps> {

        public void viewStarting();
        public void viewStopping();
        public boolean stopAllWorks();
        public boolean startWorks(int interval, int soundId);
        public boolean changeSound(int newSoundId);
        public boolean addWork(WorkTypes type);
        public boolean removeWork(WorkTypes type);
        public boolean changeBPM(int newBPM);
    }

    /**
     * This interface defines the minimum API needed by the ModelMetronome
     * class in the Model layer to interact with PresenterMetronome class
     * in the Presenter layer.  It extends the ContextView interface
     * so the Model layer can access Context's defined in the View
     * layer.
     */
    public interface RequiredPresenterOps
            extends ContextView {
        /**
         * Forwards to the View layer to displays the  data to
         * the user.
         */

        public void showIndicatorOnUIThread();
    }

    /**
     * This interface defines the minimum public API provided by the
     * ModelMetronome class in the Model layer to the PresenterMetronome
     * class in the Presenter layer.  It extends the ModelOps
     * interface, which is parameterized by the
     * MVP.RequiredPresenterOps interface used to define the argument
     * passed to the onConfigurationChange() method.
     */
    public interface ProvidedModelOps
            extends ModelOps<RequiredPresenterOps> {

        public void startBinding();
        public void stopBinding();
        public boolean StopAllWorks();
        public boolean startWorks(int interval, int soundId);
        public boolean changeSound(int newSoundId);
        public boolean addWork(WorkTypes type);
        public boolean removeWork(WorkTypes type);
        public boolean changeBPM(int newBPM);
    }
}
