/**
 * CameraSample
 * com.example.gpuimagesample.utils
 *
 * @author LiuHang
 * @Date 10/27/2017 4:07 PM
 * @Copright (c) 2017 tusdk.com. All rights reserved.
 */

package com.example.gpuimagesample.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Surface;

import com.example.gpuimagesample.DirectDrawer;
import com.example.gpuimagesample.bean.CameraSize;
import com.example.gpuimagesample.utils.AlbumUtils;
import com.example.gpuimagesample.utils.BitmapUtils;
import com.example.gpuimagesample.utils.CameraUtils;
import com.example.gpuimagesample.utils.LogUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by LiuHang on 10/27/2017.
 */

public class CameraManager
{
    // 相机的摄像头编号(前置为1， 后置为 0)
    private int mCameraId = CameraUtils.CAMERA_BACK;

    private Camera mCamera;

    private Activity mActivity;

    // 用户设置的相机预览尺寸
    private CameraSize mPreviewSize;

    // 用户设置的输出照片尺寸
    private CameraSize mPictureSize;

    // 标记相机是否在预览
    private boolean isPreviewing = false;

    // 接收相机采集的数据
    private SurfaceTexture mSurfaceTexture;

    // 负责将相机的采集到的数据绘制到GLSurfaceView
    private DirectDrawer mDirectDrawer;

    public CameraManager(Activity activity)
    {
        this.mActivity = activity;
    }

    public void setCameraId(int cameraId)
    {
        this.mCameraId = cameraId;
    }

    /**
     * 设置相机预览尺寸
     * @param previewSize
     */
    public void setPreviewSize(CameraSize previewSize)
    {
        this.mPreviewSize = previewSize;
    }

    /**
     * 设置输出照片尺寸
     * @param pictureSize
     */
    public void setPictureSize(CameraSize pictureSize)
    {
        this.mPictureSize = pictureSize;
    }

    /**
     * 打开相机
     */
    public void openCamera()
    {
        if (mCamera == null)
        {
            mCamera = Camera.open(mCameraId);
        }
        rotateDisplayOrientation();
        Camera.Parameters param = mCamera.getParameters();
        CameraSize previewSize = CameraUtils.getSuitablePreviewSizes(param, mPreviewSize);
        CameraSize pictureSize = CameraUtils.getSuitablePictureSizes(param, mPictureSize);
        param.setPictureSize(pictureSize.width, pictureSize.height);
        param.setPreviewSize(previewSize.width, previewSize.height);
        mCamera.setParameters(param);
//        mCamera.setPreviewCallback(mPreviewCallback);
    }

    public Camera getCamera()
    {
        return mCamera;
    }

    /**
     * 打开相机预览
     */
    public void startPreview()
    {
        if (mCamera == null) return;
        mCamera .startPreview();
        isPreviewing = true;
    }

    /**
     * 设置相机预览纹理
     * @param surfaceTexture
     */
    public void setPreviewTexture(SurfaceTexture surfaceTexture)
    {
        this.mSurfaceTexture = surfaceTexture;
        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止相机预览
     */
    public void stopPreview()
    {
        if (mCamera == null) return;

        mCamera.stopPreview();
        isPreviewing = false;
    }

    /**
     * 调整相机预览画面的方向
     */
    public void rotateDisplayOrientation()
    {
        try
        {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();

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

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            mCamera.setDisplayOrientation(result);
        } catch (Exception e) {
        }
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback()
    {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera)
        {

        }
    };

    /**
     * 获取当前的摄像头
     * @return
     */
    public int getCurrentCameraId()
    {
        return mCameraId;
    }

    /**
     * 切换相机摄像头
     * @param cameraId
     */
    public void switchCamera(int cameraId)
    {
       if (!isPreviewing || cameraId == mCameraId) return;

        try
        {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.setPreviewTexture(null);
            mCamera.release();
            mCamera = null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        mCamera = Camera.open(cameraId);
        try
        {
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.startPreview();
            mCameraId = cameraId;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 切换闪光灯模式
     */
    public void switchFlashMode()
    {
        if (getCurrentCameraId() == CameraUtils.CAMERA_FRONT) return;
        Camera.Parameters parameter = mCamera.getParameters();
        String flashMode =  parameter.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH) ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_TORCH;
        parameter.setFlashMode(flashMode);
        mCamera.setParameters(parameter);
    }

    /**
     * 拍照
     */
    public void takePicture()
    {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera)
            {
                if (success)
                    mCamera.takePicture(null, null, mPictureCallback);
            }
        });
    }

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera)
        {
            // 拍照后重新打开预览
            mCamera.startPreview();
            Bitmap takenBitmap = BitmapUtils.createBitmap(bytes);
            Bitmap rotatedBitmap = CameraUtils.rotateBitmap(takenBitmap, getCurrentCameraId());
            AlbumUtils.saveBitmapToAlbum("lh1028", rotatedBitmap);;
        }
    };

    /**
     * 设置画笔，负责绘制
     * @param directDrawer
     */
    public void setDrawer(DirectDrawer directDrawer)
    {
        this.mDirectDrawer = directDrawer;
    }

    /**
     * 返回纹理
     * @return
     */
    public SurfaceTexture getSurfaceTexture()
    {
        return mSurfaceTexture;
    }

    /**
     * 返回画笔
     * @return
     */
    public DirectDrawer getDrawer()
    {
        return mDirectDrawer;
    }

    /**
     * 自动聚焦
     */
    public void autoFoucus()
    {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera)
            {

            }
        });
    }
}
