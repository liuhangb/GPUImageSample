package com.example.gpuimagesample.views;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.gpuimagesample.CameraRender;
import com.example.gpuimagesample.camera.CameraManager;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by Administrator on 2017/10/27 0027.
 */

public class CameraSurfaceView extends GLSurfaceView
{
    private CameraRender mCameraRender;

    public CameraSurfaceView(Context context)
    {
        super(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     * GLSurfaceView初始化设置
     */
    public void initConfig(CameraManager camera)
    {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        mCameraRender = new CameraRender();
        mCameraRender.setUpCamera(camera);
        mCameraRender.setDelegate(mRenderDelegate);
        setRenderer(mCameraRender);
        // 这个必须放在setRenderer(mCameraRender);之后
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        requestRender();
    }

    private CameraRender.CameraRenderDelegate mRenderDelegate = new CameraRender.CameraRenderDelegate()
    {
        @Override
        public void OnFrameAvailableListener(SurfaceTexture surfaceTexture)
        {
            requestRender();
        }
    };

    public void setFilter(GPUImageFilter filter)
    {
        mCameraRender.setFilter(filter);
    }
}
