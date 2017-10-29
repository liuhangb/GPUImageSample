/**
 * CameraSample
 * com.example.tutu.camerasample.utils
 *
 * @author LiuHang
 * @Date 10/25/2017 8:36 PM
 * @Copright (c) 2017 tusdk.com. All rights reserved.
 */

package com.example.tutu.camerasample.utils;

import android.util.Log;

/**
 * Log工具类
 */

public class LogUtils
{
    private static final String TAG = "camerasample";

    public static void i(String msg)
    {
        Log.i(TAG, msg);
    }

    public static void e(String msg)
    {
        Log.e(TAG, msg);
    }

    public static void d(String msg)
    {
        Log.d(TAG, msg);
    }

    public static void w(String msg)
    {
        Log.w(TAG, msg);
    }

    public static void v(String msg)
    {
        Log.v(TAG, msg);
    }
}
