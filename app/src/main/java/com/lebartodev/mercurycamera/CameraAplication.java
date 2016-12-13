package com.lebartodev.mercurycamera;

import android.app.Application;

/**
 * Created by Александр on 12.12.2016.
 */

public class CameraAplication extends Application {
    protected static CameraAplication instance;

    public static CameraAplication getInstance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
