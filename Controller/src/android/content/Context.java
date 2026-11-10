package android.content;

import android.content.res.AssetManager;

public class Context {
    Resources none;
    public Resources getResources(){
        return none;
    }
    public String getPackageName(){
        return "virtual_robot";
    }
    /** The app's packaged assets: TeamCode/src/main/assets, as on the robot. */
    public AssetManager getAssets(){
        return AssetManager.getSystem();
    }
    public Context getApplicationContext(){
        return this;
    }
}
