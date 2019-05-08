package com.example.tencentnavigation.walknavidemo;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.tencentnavigation.walknavidemo.util.DemoUtil;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.navi.RouteSearchCallback;
import com.tencent.map.navi.WalkNaviCallback;
import com.tencent.map.navi.WalkNaviManager;
import com.tencent.map.navi.data.NaviPoi;
import com.tencent.map.navi.data.RouteData;
import com.tencent.map.navi.walk.WalkNaviView;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class NaviRealActivity extends AppCompatActivity implements View.OnClickListener,EasyPermissions.PermissionCallbacks{

    private static final String TAG = "navisdk";

    private WalkNaviManager mWalkNaviManager;
    private WalkNaviView mWalkNaviView;

    private TencentLocationManager locationManager = null;

    private Button stopBtn;

    /**
     * 起点和终点位置信息
     */
    private double mRealFromLatitude = 39.979491;
    private double mRealFromLongitude = 116.313976;
    private String mRealFromBuildingId = "11000023805";
    private String mRealFromFloorName = "F3";
    private double mRealToLatitude = 39.958834;
    private double mRealToLongitude = 116.287988;
    private String mRealToBuildingId = "1100005175";
    private String mRealToFloorName = "F3";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);

        //所要申请的权限
        String[] perms = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH
        };

        if (EasyPermissions.hasPermissions(this, perms)) {//检查是否获取该权限
            Log.i(TAG, "已获取权限");
        } else {
            //第二个参数是被拒绝后再次申请该权限的解释
            //第三个参数是请求码
            //第四个参数是要申请的权限
            EasyPermissions.requestPermissions(this, "必要的权限", 0, perms);
        }
        initManager();
        initRoute();
    }

    private void initManager(){
        stopBtn = findViewById(R.id.navi_stop);
        stopBtn.setVisibility(View.VISIBLE);
        stopBtn.setOnClickListener(this);

        mWalkNaviManager = new WalkNaviManager(this);
        mWalkNaviView = findViewById(R.id.naviview);
        mWalkNaviManager.addNaviView(mWalkNaviView);
        mWalkNaviManager.setNaviCallback(mWalkNaviCallback);
    }

    /**
     * 请求路线规划
     */
    private void initRoute(){
        //构造起点
        NaviPoi fromPoi = new NaviPoi();
        fromPoi.setLatitude(mRealFromLatitude);
        fromPoi.setLongitude(mRealFromLongitude);
        fromPoi.setBuildingId(mRealFromBuildingId);
        fromPoi.setFloorName(mRealFromFloorName);
        //构造终点
        NaviPoi targetPoi = new NaviPoi();
        targetPoi.setLatitude(mRealToLatitude);
        targetPoi.setLongitude(mRealToLongitude);
        targetPoi.setBuildingId(mRealToBuildingId);
        targetPoi.setFloorName(mRealToFloorName);
        //发起路线规划
        mWalkNaviManager.searchRoute(fromPoi, targetPoi, mRouteSearchCallback);
    }

    private RouteSearchCallback mRouteSearchCallback = new RouteSearchCallback() {
        @Override
        public void onRouteSearchFailure(int errorCode, String errorMessage) {
            Log.e(TAG,"onRouteSearchFailure:"+ errorCode);
        }

        @Override
        public void onRouteSearchSuccess(ArrayList<RouteData> routes) {
            if (mWalkNaviManager != null) {
                mWalkNaviManager.startNavi(0, false);
            }
        }
    };

    /**
     * 导航状态回调WalkNaviCallback
     */
    private WalkNaviCallback mWalkNaviCallback = new WalkNaviCallback() {
        @Override
        public void onStartNavi() {
            //导航开始
        }

        @Override
        public void onStopNavi() {
            //导航结束
        }

        @Override
        public void onOffRoute() {
            //偏航啦
        }

        @Override
        public void onRecalculateRouteStarted(int type) {
            //开始重新路线规划
        }

        @Override
        public void onRecalculateRouteSuccess(int type,ArrayList<RouteData> routeDataList) {
            //重新路线规划成功
        }

        @Override
        public void onRecalculateRouteFailure(int type, int errorCode, String errorMessage) {
            //重新路线规划失败
        }

        @Override
        public void onRecalculateRouteCanceled() {
            //重新路线规划取消
        }

        @Override
        public void onArrivedDestination() {
            //到达目的地
            disableLocation();
            if (mWalkNaviManager != null) {
                mWalkNaviManager.stopNavi();
            }
        }

        @Override
        public void onLocationSwitched(String name, boolean on){
            //定位相关设备状态
        }
        @Override
        public void onLocationStatusChanged(String name, boolean isValid){
            //定位相关设备状态
        }
    };

    private TencentLocationListener mTencentLocationListener = new TencentLocationListener() {
        @Override
        public void onLocationChanged(final TencentLocation tencentLocation, int error, String reason) {
            if (mWalkNaviManager != null) {
                mWalkNaviManager.onLocationChanged(DemoUtil.convertToGpsLocation(tencentLocation), error, reason);
            }
        }

        @Override
        public void onStatusUpdate(String name, int status, String desc) {
            if (mWalkNaviManager != null) {
                mWalkNaviManager.onStatusUpdate(name, status, desc);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.navi_stop:
                disableLocation();
                if (mWalkNaviManager != null) {
                    mWalkNaviManager.stopNavi();
                }
                break;
        }
    }

    private int enableLocation(Context context) {
        Log.i(TAG, "enableLocation");
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setInterval(1000);
        request.setAllowCache(true);
        request.setAllowDirection(true);
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_GEO);
        locationManager = TencentLocationManager.getInstance(context);
        int error = locationManager.requestLocationUpdates(request, mTencentLocationListener);
        if (error == 0) {
            locationManager.startIndoorLocation();
        }
        Log.i(TAG, "enableLocation error: " + error);
        return error;
    }

    private void disableLocation() {
        Log.i(TAG, "disableLocation");
        if (locationManager != null) {
            locationManager.stopIndoorLocation();
            locationManager.removeUpdates(mTencentLocationListener);
            locationManager = null;
        }
    }

    /**
     * 地图生命周期管理
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mWalkNaviView != null) {
            mWalkNaviView.onStart();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mWalkNaviView != null) {
            mWalkNaviView.onRestart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWalkNaviView != null) {
            mWalkNaviView.onResume();
        }
        enableLocation(this);
    }

    @Override
    protected void onPause() {
        if (mWalkNaviView != null) {
            mWalkNaviView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mWalkNaviView != null) {
            mWalkNaviView.onStop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mWalkNaviManager.removeAllNaviViews();
        mWalkNaviManager.removeNaviView(mWalkNaviView);
        mWalkNaviManager.setNaviCallback(null);
        mWalkNaviManager = null;
        if (mWalkNaviView != null) {
            mWalkNaviView.onDestroy();
            mWalkNaviView = null;
        }
        super.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}
