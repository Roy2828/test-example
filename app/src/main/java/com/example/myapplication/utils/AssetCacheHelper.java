package com.example.myapplication.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssetCacheHelper {

    /**
     * 将assets目录下的图片文件拷贝到应用缓存目录
     *
     * @param context  上下文对象
     * @param fileName assets中的文件名（如 "images/photo.jpg"）
     * @return 缓存文件的绝对路径，如果失败则返回null
     */
    public static String copyImageFromAssetsToCache(Context context, String fileName) {
        // 获取应用缓存目录
        File cacheDir = context.getCacheDir();
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        // 创建输出文件对象
        File outFile = new File(cacheDir, fileName);
        // 确保输出文件的父目录存在
        File parentDir = outFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 如果目标文件已存在，则删除旧文件
        if (outFile.exists()) {
            outFile.delete();
        }

        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            // 1. 从assets打开输入流
            inputStream = context.getAssets().open(fileName);
            // 2. 创建指向缓存文件的输出流
            outputStream = new FileOutputStream(outFile);

            // 3. 缓冲区拷贝数据
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // 4. 刷新输出流确保数据写入磁盘
            outputStream.flush();

            // 返回最终缓存文件的路径
            return outFile.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            // 5. 最后确保关闭所有流
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从缓存文件路径加载Bitmap
     *
     * @param filePath 缓存文件的完整路径
     * @return Bitmap对象
     */
    public static Bitmap loadBitmapFromCache(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }
}
