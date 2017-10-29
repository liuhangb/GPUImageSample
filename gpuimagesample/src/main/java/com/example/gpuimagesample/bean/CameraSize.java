/**
 * CameraSample
 * com.example.gpuimagesample.utils
 *
 * @author LiuHang
 * @Date 10/27/2017 4:18 PM
 * @Copright (c) 2017 tusdk.com. All rights reserved.
 */

package com.example.gpuimagesample.bean;

/**
 * 存储相机宽高的实体类
 */

public class CameraSize
{
    // 相机的宽
    public int width;

    // 相机的高
    public int height;

    public CameraSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    /**
     * 获得宽高间的最大值
     * @return
     */
    public int maxSide()
    {
        return (width - height >=0 ? width : height);
    }

    @Override
    public String toString()
    {
        return "width = " + width + ",heigt = "+ height;
    }
}
