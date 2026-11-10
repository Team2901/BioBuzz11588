package org.firstinspires.ftc.robotcore.internal.system;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Virtual Robot's stand-in for the FTC SDK's AppUtil.
 *
 * Supplies the parts team code typically uses: the singleton, the Robot Controller
 * activity / application (so {@code getActivity().getAssets().open(...)} reads
 * TeamCode/src/main/assets), and the well-known FIRST folders.
 */
public class AppUtil {

    public static final File ROOT_FOLDER = Environment.getExternalStorageDirectory();
    public static final File FIRST_FOLDER = new File(ROOT_FOLDER + "/FIRST/");
    public static final File ROBOT_SETTINGS = new File(FIRST_FOLDER, "/settings/");
    public static final File ROBOT_DATA_DIR = new File(FIRST_FOLDER, "/data/");
    public static final File MATCH_LOG_FOLDER = new File(FIRST_FOLDER, "/matchlogs/");
    public static final File LOG_FOLDER = ROOT_FOLDER;

    private static final AppUtil INSTANCE = new AppUtil();

    private final Activity activity = new Activity();

    protected AppUtil() {
    }

    public static AppUtil getInstance() {
        return INSTANCE;
    }

    public static Context getDefContext() {
        return getInstance().getApplication();
    }

    public Application getApplication() {
        return ActivityThread.currentApplication();
    }

    public Activity getActivity() {
        return activity;
    }

    public Activity getRootActivity() {
        return activity;
    }

    public String getApplicationId() {
        return getApplication().getPackageName();
    }

    public void runOnUiThread(Runnable action) {
        action.run();
    }

    public void ensureDirectoryExists(File directory) {
        ensureDirectoryExists(directory, true);
    }

    public void ensureDirectoryExists(File directory, boolean enforceSuccess) {
        if (!directory.isDirectory() && !directory.mkdirs() && enforceSuccess) {
            throw new RuntimeException("failed to create directory " + directory);
        }
    }
}
