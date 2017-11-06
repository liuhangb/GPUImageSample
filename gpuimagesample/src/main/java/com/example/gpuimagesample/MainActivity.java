package com.example.gpuimagesample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.gpuimagesample.bean.CameraSize;
import com.example.gpuimagesample.camera.CameraManager;
import com.example.gpuimagesample.utils.CameraUtils;
import com.example.gpuimagesample.views.CameraSurfaceView;

import jp.co.cyberagent.android.gpuimage.GPUImageGammaFilter;

public class MainActivity extends Activity implements View.OnClickListener {
    private CameraSurfaceView mGLSurfaceView;

    private CameraManager mCamera;

    private Button mTakenPhotoBtn;
    private Button mSwitchCameraBtn;
    private Button mFlashBtn;
    private Button addFilterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView()
    {
        mGLSurfaceView = findViewById(R.id.lsq_gl_surface_view);
        mGLSurfaceView.initConfig(getCamera());
        mGLSurfaceView.setOnClickListener(this);

        mFlashBtn = findViewById(R.id.lsq_flash_btn);
        mSwitchCameraBtn = findViewById(R.id.lsq_switch_camera_btn);
        mTakenPhotoBtn = findViewById(R.id.lsq_take_photo_btn);
        mFlashBtn.setOnClickListener(this);
        mSwitchCameraBtn.setOnClickListener(this);
        mTakenPhotoBtn.setOnClickListener(this);
        addFilterBtn = findViewById(R.id.lsq_add_filter_btn);
        addFilterBtn.setOnClickListener(this);
    }

    private CameraManager getCamera()
    {
        mCamera = new CameraManager(this);
        mCamera.setCameraId(CameraUtils.CAMERA_BACK);
        mCamera.setPictureSize(new CameraSize(720, 1080));
        mCamera.setPreviewSize(new CameraSize(720, 1080));
        mCamera.openCamera();
        return mCamera;
    }

    @Override
    public void onClick(View view)
    {
     if (view == mSwitchCameraBtn)
     {
         if (mCamera.getCurrentCameraId() == CameraUtils.CAMERA_FRONT)
            mCamera.switchCamera(CameraUtils.CAMERA_BACK);
         else
             mCamera.switchCamera(CameraUtils.CAMERA_FRONT);
     }
     else if (view == mFlashBtn)
     {
         mCamera.switchFlashMode();
     }
     else if (view == mTakenPhotoBtn)
     {
         mCamera.takePicture();
     }
     else if (view == mGLSurfaceView)
     {
        mCamera.autoFoucus();
     }
     else if (view == addFilterBtn)
     {
         GPUImageGammaFilter gammaFilter = new GPUImageGammaFilter();
         gammaFilter.setGamma(3.0f);
         mGLSurfaceView.setFilter(gammaFilter);
     }
    }
}
