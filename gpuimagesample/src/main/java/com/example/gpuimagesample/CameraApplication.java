package com.example.gpuimagesample;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2017/10/28 0028.
 */

public class CameraApplication extends Application
{
    private  Context mContext;
    private static CameraApplication mCameraApplication;

    public static CameraApplication instance()
    {
        if (mCameraApplication == null)
        {
            mCameraApplication = new CameraApplication();
        }
        return mCameraApplication;
    }
    public  Context getContext()
    {
        if (mContext == null)
        {
            mContext = mCameraApplication.getContext();
        }
        return mContext;
    }
}
