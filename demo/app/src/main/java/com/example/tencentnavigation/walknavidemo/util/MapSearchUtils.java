package com.example.tencentnavigation.walknavidemo.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

/**
 * All rights Reserved, Designed By lbs.qq.com
 *
 * @version V1.0
 * @Description:
 * @author: wangxiaokun
 * @date: 2018/4/19
 * @Copyright: 2018 tencent Inc. All rights reserved.
 */
public class MapSearchUtils {

    private static final String TENCENTMAP_META_KEY = "TencentMapSDK";

    public static String getIMei(Context mContext) {
        if (mContext == null) {
            return "";
        }
        TelephonyManager telManager = (TelephonyManager)mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telManager == null) {
            return "";
        }
        // String strImsi=telManager.getSubscriberId();
        String imei = null;
        int permissionType = 0;
        if (Build.VERSION.SDK_INT > 22) {
            permissionType = mContext.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE);
            if (permissionType != PackageManager.PERMISSION_GRANTED) {
                return  "";
            }
        }
        imei = telManager.getDeviceId();
        if (imei == null) {
            imei = "";
        }

        telManager = null;
        return imei;
    }

    public static String getMapKey(Context mContex) {
        if (mContex == null) {
            return "";
        }
        String strPackagename = mContex.getPackageName();
        ApplicationInfo appInfo = null;
        try {
            appInfo = mContex.getPackageManager().getApplicationInfo(strPackagename,
                    PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (appInfo == null) {
            return "";
        }
        if (appInfo.metaData == null) {
            return "";
        }
        String strKey = appInfo.metaData.getString(TENCENTMAP_META_KEY);
        strPackagename = null;
        appInfo = null;
        return strKey;
    }
}
