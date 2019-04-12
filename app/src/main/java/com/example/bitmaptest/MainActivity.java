package com.example.bitmaptest;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *
 * https://blog.csdn.net/guolin_blog/article/details/50727753
 * https://blog.csdn.net/zhaokaiqiang1992/article/details/49787117
 *
 * 图片资源文件夹-相应的屏幕密度dpi
 * 不同的设备会根据自身的dpi找不同的drawable文件夹(例如智能电视dpi=240-->drawable-hdpi),如果在hdpi没有找到，在低密度文件夹中，需要比例放大-占用更大的内存！
 * 如果对应dpi的drawable文件夹不存在，会去优先高密度的文件夹，再到drawable-nodpi(保持原图分辨率),最后依次到低密度
 * setBackground和setBackgroundResource加载资源文件，占内存大小相同--分辨率*像素点占字节数
 * Bitmap是最简单的一种Drawable
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Bitmap bitmap;

    @BindView(R.id.view)
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        Log.i(TAG, "onCreate:maxMemory----> " + maxMemory);
        int dpi = getResources().getDisplayMetrics().densityDpi;
        Log.i(TAG, "onCreate:dpi----> " + dpi);

    }

    @OnClick(R.id.button1)void nativeBitmap(){
        //1. 使用BitmapFactory直接加载bitmap打印
        if (bitmap != null){
            bitmap.recycle();
        }
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gaosibg1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.i(TAG, "initBackground:bitmap size -- " + bitmap.getAllocationByteCount()+ "  " + bitmap.getByteCount());
            String text = ((bitmap.getAllocationByteCount() / 1024) / 1024) + "MB";
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }
        Log.i(TAG, "initBackground: resolution -- " + bitmap.getWidth() + "*" + bitmap.getHeight());
        Drawable drawable = new BitmapDrawable(bitmap);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        }else {
            view.setBackgroundDrawable(drawable);
        }
        printBackground();
    }

    @OnClick(R.id.button2)void compressBitmap(){
        if (bitmap != null){
            bitmap.recycle();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gaosibg2, options);
        String text = ((bitmap.getByteCount() / 1024) / 1024) + "MB";
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

        //加载压缩bitmap的方式--占内存1920*1080*2(缩小二倍)
        Drawable drawable = new BitmapDrawable(bitmap);
        view.setBackgroundDrawable(drawable);
        printBackground();
    }

    @OnClick(R.id.button3)void compressBitmap2(){
        //3. 使用压缩的bitmap(网上方式)
        if (bitmap != null){
            bitmap.recycle();
        }
        bitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.gaosibg1, 1080, 1920);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.i(TAG, "initBackground:bitmap size -- " + bitmap.getAllocationByteCount()+ "  " + bitmap.getByteCount());
            String text = ((bitmap.getAllocationByteCount() / 1024) / 1024) + "MB";
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }
        Log.i(TAG, "initBackground: resolution -- " + bitmap.getWidth() + "*" + bitmap.getHeight());

        //加载压缩bitmap的方式--占内存1920*1080*2(缩小二倍)
        Drawable drawable = new BitmapDrawable(bitmap);
        view.setBackgroundDrawable(drawable);
        printBackground();
    }

    /**
     * drawable-nodpi文件夹，这个文件夹是一个密度无关的文件夹，放在这里的图片系统就不会对它进行自动缩放
     * Drawable转Bitmap
     * 并输出view背景图占内存大小
     */
    private void printBackground(){
        Drawable drawable = view.getBackground();
        if (drawable != null){
            BitmapDrawable  bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Log.i(TAG, "background bitmap:size: " + bitmap.getByteCount() + " width: " + bitmap.getWidth() + " height: " + bitmap.getHeight());
        }
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                  int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        Log.e(TAG, "calculateInSampleSize: " + options.outWidth + "   " + options.outHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;

//        options.inScaled = false;//不缩放--保持原有的像素点个数
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        Log.i(TAG, "calculateInSampleSize: " + options.outWidth + "*" + options.outHeight);
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        Log.i(TAG, "calculateInSampleSize: " + inSampleSize);
        return inSampleSize;
    }

}
