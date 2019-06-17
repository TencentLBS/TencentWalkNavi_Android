package com.tencent.map.walkNaviDemo;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class StorageUtil {

    private static final String TAG = "StorageUtil";

    private static final String NAVIGATION_PATH = "/TencentMapSDK/navigation";

    public static String getStoragePath(Context context) {
        if (context == null) {
            return null;
        }
        String strFolder;
        boolean hasSdcard;
        try {
            hasSdcard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        } catch (Exception e) {
            Log.e(TAG, "getStoragePath Exception", e);
            e.printStackTrace();
            hasSdcard = false;
        }
        if (!hasSdcard) {
            strFolder = context.getFilesDir().getPath() + NAVIGATION_PATH;
            File file = new File(strFolder);
            if (!file.exists()) {
                file.mkdirs();
            }
        } else {
            strFolder = Environment.getExternalStorageDirectory().getPath() + NAVIGATION_PATH;
            File file = new File(strFolder);
            if (!file.exists()) { // 目录不存在，创建目录
                if (!file.mkdirs()) {
                    strFolder = context.getFilesDir().getPath() + NAVIGATION_PATH;
                    file = new File(strFolder);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                }
            } else { // 目录存在，创建文件测试是否有权限
                try {
                    String newFile = strFolder + "/.test";
                    File tmpFile = new File(newFile);
                    if (tmpFile.createNewFile()) {
                        tmpFile.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "getStoragePath Exception", e);
                    strFolder = context.getFilesDir().getPath() + NAVIGATION_PATH;
                    file = new File(strFolder);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                }
            }
        }
        return strFolder;
    }
}
