package com.lebartodev.mercurycamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Александр on 05.12.2016.
 */

public class PhotoUtil {
    public static void getRotatedBitmap(Context context, int type, Bitmap bm, long name, int angle) {

        int degree = -angle + 90;
        if (type == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            degree = 270;
        }
        Log.d("PhotoUtil", "Start rotate");
        Bitmap newBit;
        //InputStream image_stream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = bm;
        newBit = rotateImage(bitmap, degree);
        //image_stream.close();
        //File abs = new File(String.format(MediaUtil.getVideosDirectory(context) + "/%d.jpg", name));
            /*if (type == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Matrix m = new Matrix();
                m.preScale(-1, 1);
                newBit = Bitmap.createBitmap(newBit, 0, 0, newBit.getWidth(), newBit.getHeight(), m, false);
            }*/
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(String.format(MediaUtil.getVideosDirectory(context) + "/%d.jpg", name)));
            newBit.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (Exception e) {
            Log.e("PhotoUtil", e.getMessage());
        }

        Log.d("PhotoUtil", "Finish rotate");


    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }
}
