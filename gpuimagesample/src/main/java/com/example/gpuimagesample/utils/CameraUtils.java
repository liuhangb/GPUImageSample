package com.example.gpuimagesample.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;

import com.example.gpuimagesample.bean.CameraSize;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 相机工具类
 */

public class CameraUtils
{
    // 标记前后摄像头
    public static final int CAMERA_FRONT = 1;
    public static final int CAMERA_BACK = 0;

    // 前后摄像头拍摄照片矫正的角度
    public static final int ROTATION_90 = 90;

    public static final int ROTATION_270 = 270;

    /**
     * 获取与设置相机预览尺寸最接近的Size
     *
     * @param param
     * @return
     */
    public static CameraSize getSuitablePreviewSizes(Camera.Parameters param, CameraSize previewSize)
    {
        List<Camera.Size> previewSizes = param.getSupportedPreviewSizes();
        // 对设置所支持的所有相机预览尺寸按从大到小排列
        Collections.sort(previewSizes, new Comparator<Camera.Size>()
        {

            @Override
            public int compare(Camera.Size size1, Camera.Size size2)
            {
                return size2.width - size1.width;
            }
        });
        CameraSize denstiSize = null;
        for (Camera.Size size : previewSizes)
        {
            if (previewSize == null) break;

            CameraSize currentSize = new CameraSize(size.width, size.height);
            // 选择与用户设置的相机预览尺寸最相近的Size
            if (Math.abs(currentSize.maxSide() - previewSize.maxSide()) > 10) continue;

            denstiSize = currentSize;
            break;
        }

        // 如果没有满足要求的Size,默认选择设备支持的最高相机预览尺寸
        if (denstiSize == null) denstiSize = new CameraSize(previewSizes.get(0).width, previewSizes.get(0).height);
        return denstiSize ;
    }

    /**
     * 获取与设置照片尺寸最接近的Size
     *
     * @param param
     * @return
     */
    public static CameraSize getSuitablePictureSizes(Camera.Parameters param, CameraSize pictureSize)
    {
        List<Camera.Size> pictureSizes = param.getSupportedPictureSizes();
        // 对设置所支持的所有输出照片尺寸按从大到小排列
        Collections.sort(pictureSizes, new Comparator<Camera.Size>()
        {

            @Override
            public int compare(Camera.Size size1, Camera.Size size2)
            {
                return size2.width - size1.width;
            }
        });

        CameraSize denstiSize = null;
        for (Camera.Size size : pictureSizes)
        {
            if (pictureSize == null) break;

            CameraSize currentSize = new CameraSize(size.width, size.height);
            // 选择与用户设置的输出照片尺寸最相近的Size
            if (Math.abs(currentSize.maxSide() - pictureSize.maxSide()) > 10) continue;

            denstiSize = currentSize;
            break;
        }

        // 如果没有满足要求的Size,默认选择设备支持的最高输出照片尺寸
        if (denstiSize == null) denstiSize = new CameraSize(pictureSizes.get(0).width, pictureSizes.get(0).height);
        return denstiSize ;
    }

    /**
     * 矫正照片的朝向
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int cameraId)
    {
        int degree = 0;
        if (cameraId == CAMERA_FRONT)
            degree = ROTATION_270;
        else
            degree = ROTATION_90;

        Matrix matrix = new Matrix();
        matrix.setRotate(degree, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() /2);

        try
        {
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return newBitmap;
        }
        catch (OutOfMemoryError ex)
        {
            LogUtils.i(""+ex);
        }

        return bitmap;
    }
}
