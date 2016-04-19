package com.skyver.metronome.view;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.skyver.metronome.MVP;
import com.skyver.metronome.R;
import com.skyver.metronome.common.GenericActivity;
import com.skyver.metronome.common.Utils;
import com.skyver.metronome.model.WorkTypes;
import com.skyver.metronome.model.works.WorkRunFlash;
import com.skyver.metronome.presenter.PresenterMetronome;

import java.util.List;


/**
 * It plays the role of  the "View" in the Model-View-Presenter (MVP) pattern.
 * It extends GenericActivity that provides a framework to automatically handle
 * runtime configuration changes of an PresenterMetronome object, which
 * plays the role of the "Presenter" in the MVP pattern.  The
 * MPV.RequiredViewOps and MVP.ProvidedPresenterOps interfaces are
 * used to minimize dependencies between the View and Presenter
 * layers.
 */

public class MainActivity
        extends GenericActivity<MVP.RequiredViewOps,
                MVP.ProvidedPresenterOps,
        PresenterMetronome>
        implements MVP.RequiredViewOps {


    //range - min and max of bpm
    private static final int MIN_BPM = 10;
    private static final int MAX_BPM = 240;
    public static final int DEFAULT_BPM = 60;

    public static final String BTN_VIBRO = "vibro_button";
    public static final String BTN_FLASH = "flash_button";
    public static final String BTN_SOUND = "sound_button";
    public static final String BTN_START = "start_button";
    public static final String BPM_FIELD = "bpm_field";
    public static final String SOUND_FIELD = "sound_field";

    private int mCurrentBPM;
    private int mCurrentSound;

    private boolean mIsFlashAvailable;

    private ToggleButton mFlashBtn;
    private ToggleButton mVibroBtn;
    private ToggleButton mSoundBtn;
    private ToggleButton mStartBtn;

    private EditText editField;

    private ImageView indicator;

    /**
     * Hook method called when a new instance of Activity is created.
     * One time initialization code goes here, e.g., initializing
     * views.
     *
     * @param Bundle object that contains saved state information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ////////////////
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.inflateMenu(R.menu.menu_main);

        initialiseComponents();

        checkIfFlashAvailable();

        //initialise Presenter layer of MVP
        super.onCreate(PresenterMetronome.class,
                this);
    }

    private void initialiseComponents(){

        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        mFlashBtn = (ToggleButton) findViewById(R.id.buttonFlash);
        mFlashBtn.setChecked(sharedPref.getBoolean(BTN_FLASH, false));  //default is false

        mSoundBtn = (ToggleButton) findViewById(R.id.buttonSound);
        mSoundBtn.setChecked(sharedPref.getBoolean(BTN_SOUND, false));  //default is false

        mVibroBtn = (ToggleButton) findViewById(R.id.buttonVibro);
        mVibroBtn.setChecked(sharedPref.getBoolean(BTN_VIBRO, false));  //default is false

        mStartBtn = (ToggleButton) findViewById(R.id.buttonStartStop);
        mStartBtn.setChecked(sharedPref.getBoolean(BTN_START, false));  //default is false

        mCurrentBPM = sharedPref.getInt(BPM_FIELD, DEFAULT_BPM);
        mCurrentSound = sharedPref.getInt(SOUND_FIELD, R.raw.variant_1);

        indicator = (ImageView) findViewById(R.id.indicatorView);

        editField = (EditText) findViewById(R.id.enterBpmField);
        editField.setText("" + mCurrentBPM);

        editField.setOnKeyListener
                (new View.OnKeyListener() {
                     public boolean onKey(View v, int keyCode, KeyEvent event) {

                         if (event.getAction() == KeyEvent.ACTION_DOWN &&
                                 (keyCode == KeyEvent.KEYCODE_ENTER)) {

                             // Hide the keyboard.
                             Utils.hideKeyboard(MainActivity.this,
                                     v.getWindowToken());

                             processAnInput();
                             return true;
                         }
                         return false;
                     }
                 }
                );
    }

    //save buttons state to be synchronous with Service's works
    private void savePreferences(){

        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(BTN_SOUND, mSoundBtn.isChecked());
        editor.putBoolean(BTN_FLASH, mFlashBtn.isChecked());
        editor.putBoolean(BTN_VIBRO, mVibroBtn.isChecked());
        editor.putBoolean(BTN_START, mStartBtn.isChecked());
        editor.putInt(BPM_FIELD, mCurrentBPM);
        editor.putInt(SOUND_FIELD, mCurrentSound);
        editor.commit();
    }

    private void processAnInput(){
        String inputString = editField.getText().toString();

        //nothing was entered
        if(inputString.equals("")){
            editField.setText(""+mCurrentBPM );
            return;
        }
        //inputString = inputString.replace(" BPM", "");
        int number = Integer.parseInt(inputString);
        checkAndSetNewBPM(number);
    }


    /** Check if this device has a flash
     *
     * this method is invoked every time when 'Flash' button is pressed
     * unless there is a Torch mode so we sett flag 'mIsFlashAvailable'
     * this method can return false even if flash is present
     * because the Camera instance can be in use
     * */
    private boolean checkIfFlashAvailable() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            return false;
        Camera mCamera = null;
        try{
            // this device has a camera
            // get Camera parameters
            mCamera = WorkRunFlash.getCameraInstance();
            if(mCamera == null)
                return false;
            Camera.Parameters params = mCamera.getParameters();
            if(params == null)
                return false;

            List<String> flashModes = params.getSupportedFlashModes();
            if (flashModes != null &&
                    flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                // torch mode is supported
                mIsFlashAvailable = true;
                return true;
            }
            else{
                return false;
            }
        }
        finally{
            if (mCamera != null)
                mCamera.release();        // release the camera for other applications
        }

    }

    //reaction on click of toggle buttons
    public void onClickToggle(View btn){

        WorkTypes type = null;
        boolean isSuccess = false;

        switch (btn.getId()){

            case R.id.buttonVibro:{
                type = WorkTypes.VIBRO;
                break;
            }
            case R.id.buttonFlash:{
                type = WorkTypes.FLASH;
                if(((ToggleButton) btn).isChecked()){

                    //in generl flash can be present but not available at the moment(busy)
                    //in such a case we allow checking this option - flash we'll be used
                    //as soon as it is freed
                    if(!mIsFlashAvailable && !checkIfFlashAvailable()){
                        mFlashBtn.setChecked(false);
                        Utils.showToast(this, "Flash is unavailable");
                        return;
                    }
                }
                break;
            }
            case R.id.buttonSound:{
                type = WorkTypes.SOUND;
                if(((ToggleButton) btn).isChecked()) {
                    isSuccess = getPresenter().removeWork(type);
                    if(!isSuccess)
                        ((ToggleButton) btn).setChecked(false);
                } else {
                    isSuccess = getPresenter().addWork(type);
                    if(!isSuccess)
                        ((ToggleButton) btn).setChecked(true);
                }
                return;
            }
            case R.id.buttonStartStop:{
                if(((ToggleButton) btn).isChecked()) {
                    isSuccess = getPresenter().startWorks(mCurrentBPM, mCurrentSound);
                    if(!isSuccess)
                        ((ToggleButton) btn).setChecked(false);

                } else {
                    isSuccess = getPresenter().stopAllWorks();
                    if(!isSuccess)
                        ((ToggleButton) btn).setChecked(true);
                }
                return;
            }

        }

        //should never hppen
        if(type == null)
            return;

        if(((ToggleButton) btn).isChecked()) {
            isSuccess = getPresenter().addWork(type);
            if(!isSuccess)
                ((ToggleButton) btn).setChecked(false);
        } else {
            isSuccess = getPresenter().removeWork(type);
            if(!isSuccess)
                ((ToggleButton) btn).setChecked(true);
        }

    }

    ////reaction on click of Add nd Remove BPM buttons
    public void onClickAddRemove(View btn){

        if(btn.getId() == R.id.imgViewMinus)
            checkAndSetNewBPM(mCurrentBPM - 1);
        if(btn.getId() == R.id.imgViewPlus)
            checkAndSetNewBPM(mCurrentBPM + 1);

    }

    // set new BPM
    private void checkAndSetNewBPM(int number ){

        number = number < MIN_BPM ? MIN_BPM : number;
        number = number > MAX_BPM ? MAX_BPM : number;

        //if nothing to change  - return
        if(number == mCurrentBPM)
            return;

        boolean isSuccess = getPresenter().changeBPM(number);
        if(isSuccess){
            mCurrentBPM = number;
            editField.setText(""+number );
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        getPresenter().viewStarting();

    }

    @Override
    protected void onStop() {
        getPresenter().viewStopping();

        savePreferences();
        super.onStop();
    }

    /**
     * Hook method called by Android when this Activity becomes is
     * destroyed.
     */
    @Override
    protected void onDestroy() {
        // Destroy the presenter layer, passing in whether this is
        // triggered by a runtime configuration or not.
        getPresenter().onDestroy(isChangingConfigurations());

        // Always call super class for necessary operations when
        // stopping.
        super.onDestroy();
    }


    @Override
    public void showIndicatorOnUIThread() {
        indicator.post(new Runnable() {
            @Override
            public void run() {
                indicator.setImageResource(R.drawable.presence_online);
            }
        });
        indicator.postDelayed(new Runnable() {
            @Override
            public void run() {
                indicator.setImageResource(R.drawable.presence_invisible);
            }
        }, 150);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        int soundId = mCurrentSound;

        switch (id){

            case R.id.variant_1 :
                soundId = R.raw.variant_1;
                break;

            case R.id.variant_2 :
                soundId = R.raw.variant_2;
                break;

            case R.id.variant_3 :
                soundId = R.raw.variant_3;
                break;

            case R.id.variant_4 :
                soundId = R.raw.variant_4;
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        mCurrentSound = soundId;
        getPresenter().changeSound(soundId);
        return true;
    }
}