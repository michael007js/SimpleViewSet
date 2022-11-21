package com.sss.michael.simpleview.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import androidx.annotation.NonNull;

public class AssetsUtil {

    /**
     * 获取 assets 目录下的文件
     *
     * @param path 文件在 assets 文件夹中的路径
     * @return 文件内容
     */
    public static String getFileFromAssets(Context context, @NonNull String path) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bf = null;
        InputStreamReader isr = null;
        try {
            AssetManager assetManager = context.getResources().getAssets();
            isr = new InputStreamReader(assetManager.open(path));
            bf = new BufferedReader(isr);
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bf != null) {
                    bf.close();
                }
                if (isr != null) {
                    isr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 获取 assets 目录下的图片
     *
     * @param path 图片在 assets 文件夹中的路径
     * @return
     */
    public static Bitmap getImageFromAssets(Context context, @NonNull String path) {
        Bitmap image = null;
        InputStream is = null;
        AssetManager am = context.getResources().getAssets();
        try {
            is = am.open(path);
            image = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return image;
    }
}

