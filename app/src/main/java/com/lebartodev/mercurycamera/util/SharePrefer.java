package com.lebartodev.mercurycamera.util;

import android.hardware.Camera;
import android.util.Log;

import com.lebartodev.mercurycamera.model.SettingsModel;

/**
 * Created by Александр on 12.12.2016.
 */

public class SharePrefer extends BaseSharedPrefer{

    public static void setSettings(SettingsModel settings) {

        get().put("settings_camera", settings.getCameraId());
        get().put("settings_flash", settings.isAutoFlash());
    }
    public static SettingsModel getSettings() {
        SettingsModel model = new SettingsModel();
        model.setAutoFlash(get().get("settings_flash", false));
        model.setCameraId(get().get("settings_camera", Camera.CameraInfo.CAMERA_FACING_BACK));
        Log.d("SettingsModel", String.valueOf(model.isAutoFlash()));
        return model;
    }

}
