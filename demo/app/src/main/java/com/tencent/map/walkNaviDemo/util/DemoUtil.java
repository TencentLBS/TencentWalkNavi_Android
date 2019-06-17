
package com.tencent.map.walkNaviDemo.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.navi.WalkNaviManager;
import com.tencent.map.navi.data.GpsLocation;

import java.io.InputStream;
import java.util.Locale;

public class DemoUtil {

    private static WalkNaviManager sWalkNaviManager = null;
    /**
     * 用来进行资源适配，从xh分辨率，适配到当前机器的屏幕分辨率
     */
    private static float fDensityXH = 1;

    public static WalkNaviManager getWalkNaviManager(Context context) {

        if (sWalkNaviManager == null) {
            sWalkNaviManager = new WalkNaviManager(context);
        }
        return sWalkNaviManager;
    }

    public static GpsLocation convertToGpsLocation(TencentLocation tencentLocation) {
        if (tencentLocation == null) {
            return null;
        }
        GpsLocation location = new GpsLocation();
        location.setDirection(tencentLocation.getBearing());
        location.setAccuracy(tencentLocation.getAccuracy());
        location.setLatitude(tencentLocation.getLatitude());
        location.setLongitude(tencentLocation.getLongitude());
        location.setAltitude(tencentLocation.getAltitude());
        location.setProvider(tencentLocation.getProvider());
        location.setVelocity(tencentLocation.getSpeed());
        location.setTime(tencentLocation.getTime());
        location.setRssi(tencentLocation.getGPSRssi());
        location.setBuildingId(tencentLocation.getIndoorBuildingId());
        location.setFloorName(tencentLocation.getIndoorBuildingFloor());

        return location;
    }

    public static GpsLocation convertToGpsLocation(Location location) {
        if (location == null) {
            return null;
        }
        GpsLocation gpsLocation = new GpsLocation();
        gpsLocation.setDirection(location.getBearing());
        gpsLocation.setAccuracy(location.getAccuracy());
        gpsLocation.setLatitude(location.getLatitude());
        gpsLocation.setLongitude(location.getLongitude());
        gpsLocation.setAltitude(location.getAltitude());
        gpsLocation.setProvider(location.getProvider());
        gpsLocation.setVelocity(location.getSpeed());
        gpsLocation.setTime(location.getTime());
        gpsLocation.setRssi(4);

        return gpsLocation;
    }

    public static Bitmap readAssetsImg(Context context, String name) {
        String imgPath = name;
        AssetManager assetManager = context.getAssets();
        Bitmap iBitmap = null;
        InputStream is;
        try {
            is = assetManager.open(imgPath);
            iBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            iBitmap = null;
            e.printStackTrace();
        }
        iBitmap = adaptFromXhResource(context, iBitmap);
        return iBitmap;
    }

    private static Bitmap adaptFromXhResource(Context context, Bitmap mapRes) {
        if (fDensityXH == 1) {
            float fDestiDpi = context.getApplicationContext().getResources().getDisplayMetrics().densityDpi;
            float iXHDpi = 320f;// DisplayMetrics.DENSITY_XHIGH;
            fDensityXH = iXHDpi / fDestiDpi;
        }
        if (mapRes != null && fDensityXH != 1) {
            float fXHBmpWidth = mapRes.getWidth();
            float fXHBmpHigh = mapRes.getHeight();
            int iNewWidht = (int) (fXHBmpWidth / fDensityXH);
            int iNewHigh = (int) (fXHBmpHigh / fDensityXH);
            mapRes = Bitmap.createScaledBitmap(mapRes, iNewWidht, iNewHigh, true);
        }
        return mapRes;
    }

    public static String distance2string(int distance, boolean useKilometers) {
        if (distance < 1000 && !useKilometers) {
            return distance + "米";
        }
        String disStr = String.format(Locale.getDefault(), "%.1f", ((double)distance / 1000));
        if (disStr.endsWith(".0")) {
            disStr = disStr.substring(0, disStr.length() - 2);
        }
        return disStr + "公里";
    }

    public static String time2string(int minutes) {
        if (minutes <= 60) {
            return minutes + "分钟";
        }
        int hours = minutes / 60;
        minutes = minutes % 60;
        if (minutes <= 0) {
            minutes = 1;
        }
        return hours + "小时" + minutes + "分钟";
    }
}
