package com.example.gpuimagesample.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 生成Bitmap工具类
 */

public class BitmapUtils
{
    public static Bitmap createBitmap(byte[] data)
    {
        InputStream is = new ByteArrayInputStream(data);
        return BitmapFactory.decodeStream(is, null, getDefaultOptions(true));
    }

    /**
     * 获取默认的选项
     *
     * @param needAlpha
     * @return
     */
    public static BitmapFactory.Options getDefaultOptions(boolean needAlpha)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 如果设置，那返回的位图将为空，但会保存数据源图像的宽度和高度
        options.inJustDecodeBounds = false;
        // 图片是否抖动，设置为false时会造成32位色偏色
        options.inDither = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            setPurgeable(options);
        }
        options.inPreferredConfig = needAlpha ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        return options;
    }

    /**设置图片内存管理方式*/
    @SuppressWarnings("deprecation")
    private static void setPurgeable(BitmapFactory.Options options)
    {
        // 自动回收内存
        options.inPurgeable = true;
        // 如果inPurgeable为false那该设置将被忽略，如果为true，那么它可以决定位图是否能够共享一个指向数据源的引用，或者是进行一份拷贝
        options.inInputShareable = true;
    }
}
