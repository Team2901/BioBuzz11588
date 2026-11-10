package android.app;

import android.content.Context;

/**
 * Virtual Robot's shim for Android's Activity.
 *
 * Exists so team code that reaches the Robot Controller activity - most often
 * {@code AppUtil.getInstance().getActivity().getAssets()} to read files packaged with
 * the app - compiles and runs in the simulator. The simulator's single stand-in
 * activity is handed out by {@link org.firstinspires.ftc.robotcore.internal.system.AppUtil}.
 */
public class Activity extends Context {

    public Application getApplication() {
        return ActivityThread.currentApplication();
    }

    @Override
    public Context getApplicationContext() {
        return getApplication();
    }

    /** No UI thread to hop to in the simulator; the action runs immediately. */
    public void runOnUiThread(Runnable action) {
        action.run();
    }
}
