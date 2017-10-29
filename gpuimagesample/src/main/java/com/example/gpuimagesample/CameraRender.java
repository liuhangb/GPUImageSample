/**
 * CameraSample
 * com.example.gpuimagesample
 *
 * @author LiuHang
 * @Date 10/27/2017 2:12 PM
 * @Copright (c) 2017 tusdk.com. All rights reserved.
 */

package com.example.gpuimagesample;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.gpuimagesample.camera.CameraManager;

import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by LiuHang on 10/27/2017.
 */

public class CameraRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener
{
    private CameraManager mCamera;

    private SurfaceTexture mSurfaceTexture;

    private DirectDrawer mDirectDrawer;

    private int mTextureID;

    private int mGLTextureId = -1;

    // 标记是否第一次启动相机
    private boolean isFirst = true;

    private CameraRenderDelegate mCameraRenderDelegate;

    public interface CameraRenderDelegate {
        void OnFrameAvailableListener(SurfaceTexture surfaceTexture);
    }

    public void setDelegate(CameraRenderDelegate delegate) {
        this.mCameraRenderDelegate = delegate;
    }

    /**
     * 设置相机
     *
     * @param camera
     */
    public void setUpCamera(CameraManager camera) {
        this.mCamera = camera;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
//        GLES20.glUseProgram(mFilter.getProgram());
        if (isFirst) {
            bindSurfaceTexture();
            isFirst = false;
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if (mCamera == null) return;

        if (mSurfaceTexture != null && mDirectDrawer != null) {
            mSurfaceTexture.updateTexImage();
            float[] mtx = new float[16];
            mSurfaceTexture.getTransformMatrix(mtx);
            // 将采集到的画面画出来
            mDirectDrawer.draw(mtx, mCamera.getCurrentCameraId());
        }
    }
    private void runAll(Queue<Runnable> queue)
    {
        synchronized (queue)
        {
            while (!queue.isEmpty())
            {
                queue.poll().run();
            }
        }
    }

    private void bindSurfaceTexture()
    {
        mTextureID = createOESTexture();
        // DirectDrawer和SurfaceTexture的mTextureID必须是同一个，否则画面显示不出来
        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mDirectDrawer = new DirectDrawer(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mCamera.getCamera().setPreviewCallback(mPreviewCallback);
        mCamera.setPreviewTexture(mSurfaceTexture);
        mCamera.setDrawer(mDirectDrawer);
        mCamera.startPreview();
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback()
    {
        @Override
        public void onPreviewFrame(final byte[] data, final Camera camera)
        {
        }
    };

    public int createOESTexture()
    {
        int[] textures = new int[1];

        GLES20.glGenTextures(1, textures, 0);

        int texId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId);

        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        // This is necessary for non-power-of-two textures
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        return texId;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture)
    {
        if (mCameraRenderDelegate != null)
            mCameraRenderDelegate.OnFrameAvailableListener(surfaceTexture);
    }
}
