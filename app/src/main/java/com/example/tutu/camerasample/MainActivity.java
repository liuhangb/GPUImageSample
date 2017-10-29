package com.example.tutu.camerasample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.tutu.camerasample.utils.LogUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener
{
    // 照片旋转的角度（按顺时针计算）
    private final int ROTATION_RIGHT = 90;
    private final int ROTATION_LEFT = -90;

    // 标记前后摄像头
    private final int CAMERA_FRONT = 1;
    private final int CAMERA_BACK = 0;

    // 相机对象
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private RelativeLayout mCameraView;
    private Button mTakenPhotoBtn;
    private Button mSwitchCameraBtn;
    private Button mFlashBtn;
    //  相机是否已打开预览
    private boolean isPreviewing = false;
    // 相机的摄像头编号(前置为1， 后置为 0)
    private int mCameraId = CAMERA_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraView = (RelativeLayout) findViewById(R.id.lsq_camera_view);
        mSurfaceView = new SurfaceView(this);
        mCameraView.addView(mSurfaceView);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.setOnClickListener(this);

        mTakenPhotoBtn = (Button) findViewById(R.id.lsq_take_photo_btn);
        mTakenPhotoBtn.setOnClickListener(this);

        mSwitchCameraBtn = (Button) findViewById(R.id.lsq_switch_camera_btn);
        mSwitchCameraBtn.setOnClickListener(this);
        mFlashBtn = (Button) findViewById(R.id.lsq_flash_btn);
        mFlashBtn.setOnClickListener(this);

        initCamera(mCameraId);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LogUtils.i("onResume======");
        // SurfaceView创建好后再开启相机
        if (mSurfaceView.getHolder().getSurface().isValid())
            startCamera();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        stopCamera();
        LogUtils.i("onPause======");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        destroyCamera();
    }

    /**
     * 初始化相机参数
     *
     * @param cameraId
     */
    private void initCamera(int cameraId)
    {
        if (mCamera == null)
        {
            mCamera = Camera.open(cameraId);
        }

        Camera.Parameters param = mCamera.getParameters();
        param.setPreviewSize(getSuitablePreviewSizes(param).get(0).width, getSuitablePreviewSizes(param).get(0).height);
        param.setPictureSize(getSuitablePictureSizes(param).get(5).width, getSuitablePictureSizes(param).get(5).height);
        mCamera.setParameters(param);


        // 矫正预览的方向
        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewCallback(mPreviewCallback);
    }

    /**
     * 获取与设置相机预览尺寸最接近的Size
     *
     * @param param
     * @return
     */
    private List<Camera.Size> getSuitablePreviewSizes(Camera.Parameters param)
    {
        List<Camera.Size> previewSizes = param.getSupportedPreviewSizes();
        Collections.sort(previewSizes, new Comparator<Camera.Size>()
        {

            @Override
            public int compare(Camera.Size size1, Camera.Size size2)
            {
                return size2.width - size1.width;
            }
        });
        return previewSizes;
    }

    /**
     * 获取与设置照片尺寸最接近的Size
     *
     * @param param
     * @return
     */
    private List<Camera.Size> getSuitablePictureSizes(Camera.Parameters param)
    {
        List<Camera.Size> pictureSizes = param.getSupportedPictureSizes();
        Collections.sort(pictureSizes, new Comparator<Camera.Size>()
        {

            @Override
            public int compare(Camera.Size size1, Camera.Size size2)
            {
                return size2.width - size1.width;
            }
        });
        return pictureSizes;
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback()
    {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera)
        {
        }
    };

    /**
     * 开启相机预览
     */
    private void startCamera()
    {
        if (mCamera == null || isPreviewing) return;

        mCamera.startPreview();
        isPreviewing = true;
    }

    /**
     * 停止相机预览
     */
    private void stopCamera()
    {
        if (mCamera == null || !isPreviewing) return;

        mCamera.stopPreview();
        isPreviewing = false;
    }

    /**
     * 将相机与SurfaceView绑定
     */
    private void bindSurfaceView()
    {
        if (mSurfaceView == null || mCamera == null) return;

        try
        {
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder)
    {
        LogUtils.i("surfaceCreated=========");

        // 需要放在surfaceCreated方法里执行，才能正常显示
        bindSurfaceView();

        startCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2)
    {
        LogUtils.i("surfaceChanged=========");
        startCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        LogUtils.i("surfaceDestroyed=========");
    }

    @Override
    public void onClick(View view)
    {
        if (view == mSurfaceView)
        {
            if (mCamera == null) return;
            mCamera.autoFocus(new Camera.AutoFocusCallback()
            {
                @Override
                public void onAutoFocus(boolean success, Camera camera)
                {
                    LogUtils.i("onAutoFocus success---"+success);
                }
            });
        }
        else if (view == mTakenPhotoBtn)
        {
            if (mCamera == null) return;

            mCamera.takePicture(null, null, mPictureCallback);
        }
        else if (view == mSwitchCameraBtn)
        {
            switchCamera();
        }
        else if (view == mFlashBtn)
        {
            switchFlashMode();
        }
    }

    /**
     * 切换闪光灯模式
     */
    private void switchFlashMode()
    {
        if (mCamera == null || mCameraId == CAMERA_FRONT) return;

        Camera.Parameters parameters = mCamera.getParameters();
        LogUtils.i("parameters.getFlashMode---"+parameters.getFlashMode());
        String flashMode = (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) ? Camera.Parameters.FLASH_MODE_OFF:Camera.Parameters.FLASH_MODE_TORCH;
        LogUtils.i("flashMode---"+flashMode);
       parameters.setFlashMode(flashMode);
        mCamera.setParameters(parameters);
    }

    /**
     * 切换摄像头
     */
    private void switchCamera()
    {
        mCameraId = (mCameraId == CAMERA_BACK ? CAMERA_FRONT : CAMERA_BACK);
        destroyCamera();
        initCamera(mCameraId);
        bindSurfaceView();
        startCamera();
    }

    /**
     * 销毁相机
     */
    private void destroyCamera()
    {
        try
        {
            stopCamera();

            // 必须把之前设置的回调全部设为空
            // 必须加，否则会提示相机释放后仍被占用
            mCamera.setPreviewCallback(null);
            mCamera.setPreviewDisplay(null);
            mCamera.release();
            mCamera = null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera)
        {
            // 拍照后重新打开相机
            mCamera.startPreview();
            // btye[]转成Bitmap
            InputStream is = new ByteArrayInputStream(bytes);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, getDefaultOptions(true));
            // 将Bitmap保存到相册
            int degree = mCameraId == CAMERA_BACK ? ROTATION_RIGHT : ROTATION_LEFT;
            saveBitmapToAlbum(rotateBitmap(bitmap, degree));
        }
    };

    /**
     * 保存Bitmap到相册
     * @param bitmap
     */
    private void saveBitmapToAlbum(Bitmap bitmap)
    {
        File appDir = new File(Environment.getExternalStorageDirectory(), "lh1025");
        if (!appDir.exists())
        {
            appDir.mkdir();
        }

        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);

        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // 保存后刷新相册
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File("/sdcard/lh1025/"+fileName))));
    }

    /**
     * 矫正照片的朝向
     */
    private Bitmap rotateBitmap(Bitmap bitmap, int degree)
    {
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
