package com.lebartodev.mercurycamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.firetrap.permissionhelper.action.OnDenyAction;
import com.firetrap.permissionhelper.action.OnGrantAction;
import com.firetrap.permissionhelper.helper.PermissionHelper;
import com.lebartodev.mercurycamera.model.SettingsModel;
import com.lebartodev.mercurycamera.util.SharePrefer;
import com.transitionseverywhere.ArcMotion;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    private PermissionHelper.PermissionBuilder permissionRequest;
    private boolean isCapturing = false;
    private int currentCameraId = 0;
    private boolean autoFlash = false;

    private FrameLayout light_layout;
    private SurfaceView cameraView;
    private FrameLayout captureLayout;
    private RotatingImageView captureButton;
    private RotatingImageView checkButton;
    private File photo;
    private FrameLayout frontCameraFlash;
    private float lastBrightness = 0;
    private boolean isFrontCameraExist = false;
    private boolean isBackCameraExist = false;


    private Camera camera;
    private RotatingImageView changeCamera, changeFlash;
    SurfaceHolder holder;
    private HolderCallback holderCallback;
    private OnDenyAction onDenyAction = new OnDenyAction() {
        @Override
        public void call(int requestCode, boolean shouldShowRequestPermissionRationale) {
            if (shouldShowRequestPermissionRationale) {
            }
        }
    };
    private OnGrantAction onGrantAction = new OnGrantAction() {
        @Override
        public void call(int requestCode) {

            cameraView = (SurfaceView) findViewById(R.id.camera_view);
            camera = Camera.open(currentCameraId);
            setStartParams();


            holder = cameraView.getHolder();

            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            holderCallback = new HolderCallback();
            holder.addCallback(holderCallback);

        }
    };

    private void setCameraTypes() {
        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            isBackCameraExist = true;
            if(SharePrefer.getSettings().getCameraId()==-1){
                currentCameraId= Camera.CameraInfo.CAMERA_FACING_BACK;
            }
        }
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            isFrontCameraExist = true;
            if(SharePrefer.getSettings().getCameraId()==-1) {
                currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        setCameraTypes();

        autoFlash = SharePrefer.getSettings().isAutoFlash();
        currentCameraId = SharePrefer.getSettings().getCameraId();

        FrameLayout checkLayout = (FrameLayout) findViewById(R.id.check_button_layout);
        lastBrightness = getWindow().getAttributes().screenBrightness;
        changeFlash = (RotatingImageView) findViewById(R.id.change_flash_state);
        changeCamera = (RotatingImageView) findViewById(R.id.change_camera);
        FrameLayout changeCameraLayout = (FrameLayout) findViewById(R.id.change_camera_layout);
        FrameLayout changeFlashLayout = (FrameLayout) findViewById(R.id.change_flash_layout);
        captureLayout = (FrameLayout) findViewById(R.id.capture_layout);
        frontCameraFlash = (FrameLayout) findViewById(R.id.front_flash_layout);

        changeCamera.setTransitionContainer(changeCameraLayout);
        changeFlash.setTransitionContainer(changeFlashLayout);
        captureButton = (RotatingImageView) findViewById(R.id.capture_photo);

        checkButton = (RotatingImageView) findViewById(R.id.check_button);
        light_layout = (FrameLayout) findViewById(R.id.light_layout);
        captureButton.setTransitionContainer(captureLayout);
        checkButton.setTransitionContainer(checkLayout);

        if (!autoFlash) {
            changeFlash.setAlpha(0.5f);
        } else {
            changeFlash.setAlpha(1f);
        }

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isCapturing) {
                    capturePhoto();
                } else
                    reCapturePhoto();

            }
        });
        changeFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (autoFlash) {
                    changeFlash.setAlpha(0.5f);
                    autoFlash = false;

                } else {
                    autoFlash = true;
                    changeFlash.setAlpha(1f);
                }
                setRuntimeParameters();
            }
        });
        changeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.release();
                camera = null;

                //swap the id of the camera to be used
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                camera = Camera.open(currentCameraId);
                setStartParams();
                try {
                    //this step is critical or preview on new camera will no know where to render to
                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
                setCameraDisplayOrientation(currentCameraId);
                camera.startPreview();


            }
        });
        if(!isFrontCameraExist||!isBackCameraExist){
            changeCamera.setVisibility(View.INVISIBLE);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }


    private void initCamera() {
        permissionRequest = PermissionHelper.with(this)
                .build(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .onPermissionsDenied(onDenyAction)
                .onPermissionsGranted(onGrantAction)
                .request(3);
    }


    private void capturePhoto() {

        TransitionManager.beginDelayedTransition(captureLayout,
                new ChangeBounds().setPathMotion(new ArcMotion()).setDuration(300).addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {

                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {

                        RotateAnimation rotateAnimation = new RotateAnimation(captureButton.getAngle(), captureButton.getAngle() - 180,
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                                0.5f);
                        rotateAnimation.setDuration(300);
                        captureButton.startAnimation(rotateAnimation);
                        captureButton.setImageResource(R.drawable.ic_cancel);
                        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setFillAfter(true);
                        fadeOut.setDuration(300);
                        if(isFrontCameraExist&&isBackCameraExist)
                        changeCamera.startAnimation(fadeOut);
                        changeFlash.startAnimation(fadeOut);

                        AlphaAnimation fadeTo = new AlphaAnimation(0, 1);
                        fadeTo.setFillAfter(true);
                        checkButton.startAnimation(fadeTo);

                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                }));


        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) captureButton.getLayoutParams();
        params.gravity = (Gravity.CENTER_VERTICAL | Gravity.LEFT);

        captureButton.setLayoutParams(params);
        isCapturing = true;


        light_layout.setVisibility(View.VISIBLE);


        AlphaAnimation fade = new AlphaAnimation(1, 0);
        fade.setFillAfter(true);
        fade.setDuration(500);
        doPhoto();
        light_layout.startAnimation(fade);


    }

    private void doPhoto() {
        final int angle = changeFlash.getAngle();
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                camera.stopPreview();
                checkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reCapturePhoto();

                        Toast.makeText(CameraActivity.this, "Photo saving...", Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                long name = System.currentTimeMillis();
                                photo = new File(String.format(MediaUtil.getVideosDirectory(CameraActivity.this) + "/%d.jpg", name));
                                FileOutputStream os = null;
                                try {
                                    os = new FileOutputStream(photo);
                                    os.write(data);
                                    os.close();
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    Bitmap bmp = BitmapFactory.decodeFile(photo.getAbsolutePath(), options);
                                    PhotoUtil.getRotatedBitmap(CameraActivity.this, currentCameraId, bmp, name, angle);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(CameraActivity.this, "Photo saved", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } catch (FileNotFoundException e) {
                                    Log.e(TAG, e.getLocalizedMessage());
                                } catch (IOException e) {
                                    Log.e(TAG, e.getLocalizedMessage());
                                }
                            }
                        }).start();


                    }
                });


            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void reCapturePhoto() {
        //photo.delete();
        TransitionManager.beginDelayedTransition(captureLayout,
                new ChangeBounds().setPathMotion(new ArcMotion()).setDuration(300).addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {

                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
                        RotateAnimation rotateAnimation = new RotateAnimation(captureButton.getAngle() - 180, captureButton.getAngle(),
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                                0.5f);
                        rotateAnimation.setDuration(300);
                        captureButton.startAnimation(rotateAnimation);
                        captureButton.setImageResource(R.drawable.ic_capture);
                        AlphaAnimation fadeTo = new AlphaAnimation(0, 1);
                        fadeTo.setFillAfter(true);
                        fadeTo.setDuration(300);
                        changeFlash.startAnimation(fadeTo);
                        if(isFrontCameraExist&&isBackCameraExist)
                        changeCamera.startAnimation(fadeTo);
                        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setFillAfter(true);
                        checkButton.startAnimation(fadeOut);


                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                }));


        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) captureButton.getLayoutParams();
        params.gravity = (Gravity.CENTER);

        captureButton.setLayoutParams(params);
        isCapturing = false;
        camera.startPreview();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            if (!isCapturing)
                capturePhoto();
            else
                reCapturePhoto();
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    void setPreviewSize() {

        // получаем размеры экрана
        Display display = getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();

        // определяем размеры превью камеры
        Camera.Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();
        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());
        if (widthIsMax) {
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        matrix.setRectToRect(rectPreview, rectDisplay,
                Matrix.ScaleToFit.START);

        matrix.mapRect(rectPreview);

        // установка размеров surface из получившегося преобразования
        cameraView.getLayoutParams().height = (int) (rectPreview.bottom);
        cameraView.getLayoutParams().width = (int) (rectPreview.right);
    }

    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // получаем инфо по камере cameraId
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // задняя камера
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // передняя камера
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
        setPreviewSize();
    }

    class HolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            setCameraDisplayOrientation(currentCameraId);
            camera.startPreview();

        }


        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            setCameraDisplayOrientation(currentCameraId);
            camera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }

    }

    private void setStartParams() {
        Camera.Parameters params = camera.getParameters();
        params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        params.setPictureFormat(ImageFormat.JPEG);
        params.setJpegQuality(50);
        List<String> supportedFocusModes = camera.getParameters().getSupportedFocusModes();
        boolean hasAutoFocus = supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size size;
        try {

            size = sizes.get(5);
        }
        catch (Exception e){
            size = sizes.get(0);
        }

        /*for (int i = 0; i < sizes.size(); i++) {
            if (sizes.get(i).width > size.width)
                size = sizes.get(i);
        }*/
        params.setPictureSize(size.width, size.height);
        if (hasAutoFocus) {
            try {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
        if (autoFlash && currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            frontCameraFlash.setVisibility(View.VISIBLE);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = 1f;
            getWindow().setAttributes(lp);
        } else {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = lastBrightness;
            getWindow().setAttributes(lp);
            frontCameraFlash.setVisibility(View.INVISIBLE);
        }
        camera.setParameters(params);
        setRuntimeParameters();
        SharePrefer.setSettings(new SettingsModel(autoFlash, currentCameraId));
    }

    private void setRuntimeParameters() {
        Camera.Parameters params = camera.getParameters();

        if (autoFlash && currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        } else
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

        if (autoFlash && currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            frontCameraFlash.setVisibility(View.VISIBLE);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = 1f;
            getWindow().setAttributes(lp);

        } else {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = lastBrightness;
            getWindow().setAttributes(lp);
            frontCameraFlash.setVisibility(View.INVISIBLE);
        }
        SharePrefer.setSettings(new SettingsModel(autoFlash, currentCameraId));

        camera.setParameters(params);
    }

    public void openFolder() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(MediaUtil.getVideosDirectory(this));
        intent.setDataAndType(uri, "text/csv");
        startActivity(Intent.createChooser(intent, "Open folder"));
    }
}
