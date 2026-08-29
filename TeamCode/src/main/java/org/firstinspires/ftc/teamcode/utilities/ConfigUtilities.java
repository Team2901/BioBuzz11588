package org.firstinspires.ftc.teamcode.utilities;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;

import java.lang.reflect.Method;

public class ConfigUtilities {

    /** Returned when the active configuration cannot be determined at all. */
    public static final String UNKNOWN_CONFIGURATION = "Unknown Configuration";

    private static Context getContext() {
        try {
            final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            final Method method = activityThreadClass.getMethod("currentApplication");
            return (Application) method.invoke(null, (Object[]) null);
        } catch (final Throwable e) {
            throw new IllegalArgumentException("No context could be retrieved!");
        }
    }

    /**
     * The name of the hardware configuration currently active on the Robot Controller,
     * e.g. "coachbot 2901 24-25".
     * <p>
     * In the Virtual Robot simulator this reports the robot selected in the simulator's
     * Configuration dropdown instead, e.g. "Mecanum Bot" (or "No Configuration" before
     * one has been chosen), so configuration-dependent code behaves sensibly in both
     * places.
     * <p>
     * Never returns null, so callers can compare it directly; if the configuration
     * cannot be determined at all it reports {@link #UNKNOWN_CONFIGURATION}.
     */
    public static String getRobotConfigurationName() {
        try {
            Context context = getContext();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String objSerialized = preferences.getString("pref_hardware_config_filename", "");
            JSONObject jObject = new JSONObject(objSerialized);
            return jObject.getString("name");
        } catch (Throwable t) {
            t.printStackTrace();
            // Running somewhere with no Robot Controller settings to read at all.
        }
        return UNKNOWN_CONFIGURATION;
    }
}