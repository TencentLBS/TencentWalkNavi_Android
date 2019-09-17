package com.example.tencentnavigation.walknavidemo;

import com.example.tencentnavigation.walknavidemo.util.DemoUtil;
import com.example.tencentnavigation.walknavidemo.util.GPSReplayEngine;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.location.ILocationSource;
import com.tencent.map.navi.data.GpsLocation;

/**
 * @author yongqingli
 * @version 1.0.0
 * @date 2019-05-06
 * @copyright 2019 Tencent Inc. All rights reserved.
 */
public final class MockLocationSource implements ILocationSource {

    private String mGpsTrackPath;
    private LocationSourceListener mLocationSourceListener;

    private TencentLocationListener mTencentLocationListener = new TencentLocationListener() {
        @Override
        public void onLocationChanged(final TencentLocation tencentLocation, int error, String reason) {
            GpsLocation gpsLocation = DemoUtil.convertToGpsLocation(tencentLocation);
            if (mLocationSourceListener != null) {
                mLocationSourceListener.onLocationChanged(gpsLocation, 0, "");
            }
        }

        @Override
        public void onStatusUpdate(String name, int status, String desc) {
            if (mLocationSourceListener != null) {
                mLocationSourceListener.onStatusUpdate(name, status, desc);
            }
        }
    };

    public void setGpsTrackPath(String trackPath) {
        mGpsTrackPath = trackPath;
    }

    @Override
    public void startLocation() {
        GPSReplayEngine.getInstance().addTencentLocationListener(mTencentLocationListener);
        GPSReplayEngine.getInstance().startMockTencentLocation(mGpsTrackPath);
    }

    @Override
    public void stopLocation() {
        GPSReplayEngine.getInstance().removeTencentLocationListener(mTencentLocationListener);
        GPSReplayEngine.getInstance().stopMockTencentLocation();
    }

    @Override
    public void pauseLocation() {
        GPSReplayEngine.getInstance().pauseMockLocation();
    }

    @Override
    public void resumeLocation() {
        GPSReplayEngine.getInstance().resumeMockLocation();
    }

    @Override
    public void addListener(LocationSourceListener listener) {
        mLocationSourceListener = listener;
    }

    @Override
    public void removeListener(LocationSourceListener listener) {
        if (mLocationSourceListener == listener) {
            mLocationSourceListener = null;
        }
    }
}
