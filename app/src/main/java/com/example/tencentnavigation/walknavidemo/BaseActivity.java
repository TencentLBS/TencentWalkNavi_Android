package com.example.tencentnavigation.walknavidemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.tencent.map.navi.INaviView;
import com.tencent.map.navi.RouteSearchCallback;
import com.tencent.map.navi.WalkNaviCallback;
import com.tencent.map.navi.WalkNaviManager;
import com.tencent.map.navi.data.NaviPoi;
import com.tencent.map.navi.data.NavigationData;
import com.tencent.map.navi.data.RouteData;
import com.tencent.map.navi.walk.WalkNaviView;

import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity {
    /**
     * 基类
     */
    public WalkNaviManager mWalkNaviManager;
    public WalkNaviView mWalkNaviView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);

        this.initManager();
        this.initRoute();

    }

    private void initManager(){
        //创建WalkNaviManager实例
        mWalkNaviManager = new WalkNaviManager(this);
        mWalkNaviView = findViewById(R.id.naviview);

        //添加WalkNaviView、INaviView、WalkNaviCallback
        mWalkNaviManager.setNaviCallback(mTencentNaviCallback);
        mWalkNaviManager.addNaviView(mWalkNaviView);
        mWalkNaviManager.addNaviView(mINaviView);


    }

    /**
     * 发起路线规划
     */
    private void initRoute(){
        //构造起点
        NaviPoi fromPoi = new NaviPoi();
        fromPoi.setLatitude(39.979491);
        fromPoi.setLongitude(116.313976);
        fromPoi.setBuildingId("11000023805");
        fromPoi.setFloorName("F3");
        //构造终点
        NaviPoi targetPoi = new NaviPoi();
        targetPoi.setLatitude(39.958834);
        targetPoi.setLongitude(116.287988);
        targetPoi.setBuildingId("1100005175");
        targetPoi.setFloorName("F3");
        //发起路线规划
        mWalkNaviManager.searchRoute(fromPoi, targetPoi, mRouteSearchCallback);

    }

    private RouteSearchCallback mRouteSearchCallback = new RouteSearchCallback() {
        @Override
        public void onRouteSearchFailure(int errorCode, String errorMessage) {
            //路线规划失败回调

        }

        @Override
        public void onRouteSearchSuccess(ArrayList<RouteData> routes) {
            //路线规划成功回调
            if (mWalkNaviManager != null){
                mWalkNaviManager.startNavi(0, true);
            }
        }
    };

    /**
     * 创建导航面板更新协议INaviView.用户若需要自定义导航面板,可以实现该协议从而获取面板数据.
     */
    private INaviView mINaviView = new INaviView() {

        @Override
        public void onUpdateNavigationData(NavigationData data) {
            //获取实时导航数据：速度、距离、时间、路名、导航操作及转向箭头、是否室内等
        }

        @Override
        public void onGpsRssiChanged(int rssi) {
            //通知GPS信号变化.可用于绘制卫星信号
        }
    };

    /**
     * 导航状态回调WalkNaviCallback
     */
    private WalkNaviCallback mTencentNaviCallback = new WalkNaviCallback() {
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
        mWalkNaviManager.removeNaviView(mINaviView);
        mWalkNaviManager.removeNaviView(mWalkNaviView);
        mWalkNaviManager.setNaviCallback(null);
        mWalkNaviManager = null;
        if (mWalkNaviView != null) {
            mWalkNaviView.onDestroy();
            mWalkNaviView = null;
        }
        super.onDestroy();
    }

}
