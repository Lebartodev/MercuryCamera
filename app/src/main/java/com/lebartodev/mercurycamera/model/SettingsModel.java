package com.lebartodev.mercurycamera.model;

/**
 * Created by Александр on 12.12.2016.
 */

public class SettingsModel {
    private boolean autoFlash;
    private int cameraId;

    public SettingsModel() {
    }

    public SettingsModel(boolean autoFlash, int cameraId) {
        this.autoFlash = autoFlash;
        this.cameraId = cameraId;
    }

    public boolean isAutoFlash() {
        return autoFlash;
    }

    public void setAutoFlash(boolean autoFlash) {
        this.autoFlash = autoFlash;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }
}
