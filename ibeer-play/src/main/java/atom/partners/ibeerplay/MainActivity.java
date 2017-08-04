package atom.partners.ibeerplay;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import static atom.partners.ibeerplay.AppHandler.H;
/*
user: iBeerAdmin
pass: 93Us-Wt=
*/

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 6000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    SizedVideoView video;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            video.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean systemBarVisible, locked = true;
    private static int count;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideSystemBar();
        }
    };
    static NetUtil netUtil;

    static AppHandler appHandler;


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final OnTouchListener mDelayHideTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // startActivity(new Intent(getApplicationContext(), SplashActivity.class));
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "iBeerPlay");
        wakeLock.acquire();
        setContentView(R.layout.activity_fullscreen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

       video = (SizedVideoView) findViewById(R.id.videoView);
        //((DrawerLayout)video).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        netUtil = new NetUtil(this);
        appHandler = new AppHandler(this);
       video.setOnErrorListener(appHandler);
        video.setOnCompletionListener(appHandler);
        video.setOnPreparedListener(appHandler);
        video.setKeepScreenOn(true);

        Intent i = new Intent(MainActivity.this, WebActivity.class);
        i.putExtra("id", appHandler.getDeviceId());
        startActivity(i);

        systemBarVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);


        // Set up the user interaction to manually showSystemBar or hideSystemBar the system UI.
       video.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Log.d("Main", String.format("x=%f, y=%f", event.getX(), event.getY()));
                        if (systemBarVisible) {
                            locked = true;
                            hideSystemBar();
                            return true;
                        } else {
                            switch (count) {
                                case 0:
                                    if (event.getX() < 200 && event.getY() < 200) count++;
                                    else count = 0;
                                    break;
                                case 1:
                                    if (event.getX() > 1700 && event.getY() < 200) count++;
                                    else count = 0;
                                    break;
                                case 2:
                                    if (event.getX() < 200 && event.getY() > 1000) count++;
                                    else count = 0;
                                    break;
                                case 3:
                                    count = 0;
                                    if (event.getX() > 1700 && event.getY() > 1000) {
                                        showSystemBar();
                                        locked = false;
                                        // toggle();
                                        return true;
                                    }

                            }
                        }
                }
                return false;
            }
        });



        // Upon interacting with UI controls, delay any scheduled hideSystemBar()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.screen).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.play_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (H.getDeviceId() != null) H.getPlaylist();
            }
        });
        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = H.getDeviceId();
                if (id != null) {
                    Intent i = new Intent(MainActivity.this, WebActivity.class);
                    i.putExtra("id", id);
                    startActivity(i);
                }
            }
        });
        netUtil.registerReceiver(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hideSystemBar() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
     if (appHandler.getDeviceId() != null)  delayedHide(100);
    }

    private void hideSystemBar() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        systemBarVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void toggle() {
        if (systemBarVisible) {
            hideSystemBar();
        } else {
            showSystemBar();
        }
    }


    @SuppressLint("InlinedApi")
    private void showSystemBar() {
        video.pause();
        video.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        systemBarVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hideSystemBar() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(netUtil);
        appHandler.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if(appHandler.playerId != -1) {
            if (locked) {
                video.postDelayed(new Runnable() {
                                      @Override
                                      public void run() {

                                          Intent startMain = new Intent(Intent.ACTION_MAIN);
                                          startMain.addCategory(Intent.CATEGORY_LAUNCHER);
                                          startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                          startActivity(startMain);

                                          Intent i = new Intent(MainActivity.this, MainActivity.class);
                                          i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                          startActivity(i);
                                      }
                                  }
                        , 600);
            } else video.suspend();
        }
        super.onPause();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (appHandler.playerId != -1)
            video.start();
    }
}
