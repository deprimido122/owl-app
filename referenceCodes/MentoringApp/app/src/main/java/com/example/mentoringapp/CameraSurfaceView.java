package com.example.mentoringapp;

import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera camera = null;

    public CameraSurfaceView(Context context) {
        super(context);

        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void getCameraInstance(){
        try {
            camera = Camera.open();
        } catch (Exception e){
            Toast.makeText(getContext().getApplicationContext(), "카메라가 다른 앱에서 사용중입니다.", Toast.LENGTH_LONG);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        getCameraInstance();
        setCameraOrientation();

        try {
            camera.setPreviewDisplay(mHolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            camera.startPreview();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            camera.stopPreview();
            camera.release();
            camera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean capture(Camera.PictureCallback handler) {
        if (camera != null) {
            try {
                camera.takePicture(null, null, handler);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    public void setCameraOrientation() {
        if (camera == null) {
            return;
        }

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);

        WindowManager manager = (WindowManager) getContext().
                getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = manager.getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }

        camera.setDisplayOrientation(result);
    }

}
