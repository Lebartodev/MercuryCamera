package com.lebartodev.mercurycamera;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by Александр on 05.12.2016.
 */

public class MediaUtil {
    public static String getVideosDirectory(Context context) {
        File ext;
        if (isExternalStorageWritable() && isExternalStorageReadable()) {
            ext = new File(context.getExternalFilesDir(null).getAbsolutePath());
            if (ext.exists()) {
                return ext.getAbsolutePath();
            }
            return context.getExternalFilesDir(null).getAbsolutePath();


        } else {

            ext = new File(context.getFilesDir().getAbsolutePath());
            if (ext.exists()) {

                return ext.getAbsolutePath();
            }

            return context.getFilesDir().getAbsolutePath();
        }


    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
