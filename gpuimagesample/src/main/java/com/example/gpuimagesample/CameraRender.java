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
import com.example.gpuimagesample.utils.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;

/**
 * Created by LiuHang on 10/27/2017.
 */

public class CameraRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    public static final float TEXTURE_NO_ROTATION[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };


    private int mOutputWidth;
    private int mOutputHeight;

    private CameraManager mCamera;

    private SurfaceTexture mSurfaceTexture;

    private DirectDrawer mDirectDrawer;

    private int mTextureID;

    private int mGLTextureId = -1;

    // 标记是否第一次启动相机
    private boolean isFirst = true;

    private GPUImageFilter mFilter = new GPUImageFilter();

    private final FloatBuffer mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
    ;
    private final FloatBuffer mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
    ;

    private final Queue<Runnable> mRunOnDraw = new LinkedList<Runnable>();
    private final Queue<Runnable> mRunOnDrawEnd = new LinkedList<Runnable>();
    ;
    private IntBuffer mGLRgbBuffer;

    private CameraRenderDelegate mCameraRenderDelegate;

    public void setFilter(final GPUImageFilter filter)
    {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                final GPUImageFilter oldFilter = mFilter;
                mFilter = filter;
                if (oldFilter != null) {
                    oldFilter.destroy();
                }
                mFilter.init();
                GLES20.glUseProgram(mFilter.getProgram());
                mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight);
            }
        });
    }

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
        mFilter.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mOutputWidth = width;
        mOutputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(mFilter.getProgram());
        if (isFirst) {
            bindSurfaceTexture();
            isFirst = false;
        }

        GLES20.glUseProgram(mFilter.getProgram());
        mFilter.onOutputSizeChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        runAll(mRunOnDraw);
        mFilter.onDraw(mTextureID, mGLCubeBuffer, mGLTextureBuffer);
        runAll(mRunOnDrawEnd);
        if (mCamera == null) return;

        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
//            float[] mtx = new float[16];
//            mSurfaceTexture.getTransformMatrix(mtx);
//            // 将采集到的画面画出来
//            mDirectDrawer.draw(mtx, mCamera.getCurrentCameraId());
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
        initFilterConfig();
    }

    private void initFilterConfig()
    {
        mGLCubeBuffer.put(CUBE).position(0);
        mGLTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback()
    {
        @Override
        public void onPreviewFrame(final byte[] data, final Camera camera)
        {
            final Camera.Size previewSize = camera.getParameters().getPreviewSize();
            if (mGLRgbBuffer == null) {
                mGLRgbBuffer = IntBuffer.allocate(previewSize.width * previewSize.height);
            }
            if (mRunOnDraw.isEmpty())
            {
                runOnDraw(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        GPUImageNativeLibrary.YUVtoRBGA(data, previewSize.width, previewSize.height,
                                mGLRgbBuffer.array());
                        mGLTextureId = OpenGlUtils.loadTexture(mGLRgbBuffer, previewSize, mGLTextureId);
                        camera.addCallbackBuffer(data);

                    }
                });
            }
        }
    };

    protected void runOnDraw(final Runnable runnable)
    {
        synchronized (mRunOnDraw)
        {
            mRunOnDraw.add(runnable);
        }
    }

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
