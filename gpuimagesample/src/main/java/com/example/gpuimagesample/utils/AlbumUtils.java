package com.example.gpuimagesample.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.example.gpuimagesample.CameraApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图片保存相册工具类
 */

public class AlbumUtils
{
    /**
     * 保存Bitmap到相册
     * @param bitmap
     */
    public static void saveBitmapToAlbum(String albumName, Bitmap bitmap) {
        File appDir = new File(Environment.getExternalStorageDirectory(), albumName);
        if (!appDir.exists()) {
            appDir.mkdir();
        }

        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 保存后刷新相册
//        CameraApplication.instance().getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File("/sdcard/lh1025/"+fileName))));
    }
}
