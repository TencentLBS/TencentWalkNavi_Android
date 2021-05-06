package com.example.tencentnavigation.walknavidemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationProvider;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tencentnavigation.walknavidemo.util.DemoUtil;
import com.example.tencentnavigation.walknavidemo.util.GPSRecordEngine;
import com.example.tencentnavigation.walknavidemo.util.GPSReplayEngine;
import com.tencent.map.location.ILocationSource;
import com.tencent.map.location.RealLocationSource;
import com.tencent.map.location.SimulateLocationSource;
import com.tencent.map.navi.IWalkNaviView;
import com.tencent.map.navi.NaviMode;
import com.tencent.map.navi.WalkNaviCallback;
import com.tencent.map.navi.WalkNaviManager;
import com.tencent.map.navi.data.AttachedLocation;
import com.tencent.map.navi.data.GpsLocation;
import com.tencent.map.navi.data.NaviTts;
import com.tencent.map.navi.data.WalkNaviData;
import com.tencent.map.navi.data.WalkRouteData;
import com.tencent.map.navi.walk.WalkNaviView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.IndoorInfo;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.OverlayLevel;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavigationActivity extends Activity {

    private static final String TAG = "integrated";

    private static final String FORMAT_DISTANCE_TIME = "剩余%s %s";

    //**************************导航过程中的状态提示**************************/
    private static final String OFF_ROUTE = "您已偏航";
    private static final String RECALCULATE_ROUTE_START = "正在重新规划路线";
    private static final String RECALCULATE_ROUTE_SUCCESS = "已为您重新规划路线";
    private static final String RECALCULATE_ROUTE_FAILURE = "重新规划路线失败";
    private static final String ARRIVED_DESTINATION = "到达目的地";
    //**************************导航过程中的状态提示**************************/

    private Dialog bottomDialog;
    private TextView info;


    private ILocationSource mLocationSource;

    private WalkNaviManager mWalkNaviManager;

    private WalkNaviView mNaviView; //默认导航界面，包括地图和导航面板
    private TextView mRoadNameView; //底部路名信息
    private View mNavigationView; //底部导航信息展示板
    private TextView mBtnSettings;
    private TextView mEtaView;
    private TextView mBtnExit;
    private TextView mLogTextView;

    private int mRouteIndex;
    private boolean mShowNaviPanel = false;
    private boolean mShowCrossingEnlarged = true;
    private NaviMode mNaviMode;
    private boolean mSimulateNavi;
    private int mGpsTrackType;
    private String mGpsTrackPath;

    //导航信息
    private int totalDistance;
    private int totalTime;
    private int kcal;
    private int light;
    private int crosswalk;
    private int overpass;
    private int underpass;

    private boolean mIsArrivedDestination = false;
    private boolean mStopWhenArrived = true;
    private boolean mRecordLocation = false;

    private TencentMap mTencentMap;
    private Marker mRealLocation;
    private GpsLocation mGpsLocation;

    private ArrayList<Marker> locationMarkers = new ArrayList<>();

    private int mLocationCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_navigation);
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initDatas();
        initViews();
        initTencentNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mNaviView != null) {
            mNaviView.onStart();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mNaviView != null) {
            mNaviView.onRestart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNaviView != null) {
            mNaviView.onResume();
        }
        if (!mWalkNaviManager.isNavigating() && !mIsArrivedDestination) {
            if (mNaviView != null) {
                mNaviView.setNaviPanelEnabled(mShowNaviPanel);
                mNaviView.setNaviMode(mNaviMode);
            }

            if (mSimulateNavi) {
                WalkRouteData walkRouteData = mWalkNaviManager.getRouteData(mRouteIndex);
                SimulateLocationSource locationSource = new SimulateLocationSource();
                locationSource.setRoute(walkRouteData);
                mLocationSource = locationSource;
            } else {
                Log.w(TAG, "gps track type: " + mGpsTrackType + ", file path: " + mGpsTrackPath);
                if (mGpsTrackType == 1 && !TextUtils.isEmpty(mGpsTrackPath)) {
                    MockLocationSource mockLocationSource = new MockLocationSource();
                    mockLocationSource.setGpsTrackPath(mGpsTrackPath);
                    mLocationSource = mockLocationSource;
                } else {
                    mLocationSource = new RealLocationSource(this);
                    if (mGpsTrackType == 0) {
                        GPSRecordEngine.getInstance().startRecordTencentLocation(this, mGpsTrackPath);
                    }
                }
            }
            if (mLocationSource != null) {
                mLocationSource.addListener(mLocationSourceListener);
                mLocationSource.startLocation();
            }
            mWalkNaviManager.setLocationSource(mLocationSource);
            mWalkNaviManager.startNavi(mRouteIndex);
        }
    }

    @Override
    protected void onPause() {
        if (mNaviView != null) {
            mNaviView.onPause();
        }

        if (mLocationSource != null) {
            mLocationSource.stopLocation();
            mLocationSource = null;
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mNaviView != null) {
            mNaviView.onStop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mWalkNaviManager.removeAllNaviViews();
        mWalkNaviManager.removeNaviView(mINaviView);
        mWalkNaviManager.removeNaviView(mNaviView);
        mWalkNaviManager.setLocationSource(null);
        mWalkNaviManager.setNaviCallback(null);
        mWalkNaviManager = null;
        if (mNaviView != null) {
            mNaviView.onDestroy();
            mNaviView = null;
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initDatas();
        initViews();
        initTencentNavigation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            mShowNaviPanel = data.getBooleanExtra("showNaviPanel", true);
            mShowCrossingEnlarged = data.getBooleanExtra("crossingEnlarge", true);
            mNaviMode = (NaviMode) data.getSerializableExtra("naviMode");
            if (mNaviView != null) {
                mNaviView.setNaviPanelEnabled(mShowNaviPanel);
                mNaviView.setNaviMode(mNaviMode);
            }
            int gpsStatus = data.getIntExtra("gpsStatus", LocationProvider.AVAILABLE);
            if (!mSimulateNavi && mGpsTrackType == 1 && !TextUtils.isEmpty(mGpsTrackPath)) {
                GPSReplayEngine.getInstance().setMockGpsStatus(gpsStatus);
                if (gpsStatus == LocationProvider.AVAILABLE) {
                    GPSReplayEngine.getInstance().resumeMockLocation();
                } else {
                    GPSReplayEngine.getInstance().pauseMockLocation();
                }
            }
            mStopWhenArrived = data.getBooleanExtra("arrivedStop", true);
            mRecordLocation = data.getBooleanExtra("recordLocation", false);
        }
    }

    private void initDatas() {
        Intent intent = getIntent();
        if (intent != null) {
            mRouteIndex = intent.getIntExtra("routeIndex", 0);
            mShowNaviPanel = intent.getBooleanExtra("showNaviPanel", true);
            mNaviMode = (NaviMode) intent.getSerializableExtra("naviMode");
            mSimulateNavi = intent.getBooleanExtra("simulateNavi", false);
            mGpsTrackType = intent.getIntExtra("gpsTrackType", 2);
            mGpsTrackPath = intent.getStringExtra("gpsTrackPath");
            mStopWhenArrived = intent.getBooleanExtra("arrivedStop", true);
            mRecordLocation = intent.getBooleanExtra("recordLocation", false);
            totalDistance = intent.getIntExtra("totalDistance", 0);
            totalTime = intent.getIntExtra("totalTime", 0);
            kcal = intent.getIntExtra("kcal", 0);
            light = intent.getIntExtra("light", 0);
            crosswalk = intent.getIntExtra("crosswalk", 0);
            overpass = intent.getIntExtra("overpass",0);
            underpass = intent.getIntExtra("underpass", 0);
        }
    }

    private void initViews() {
        mNaviView = findViewById(R.id.naviView);
        mNavigationView = findViewById(R.id.navigation);
        mRoadNameView = findViewById(R.id.roadName);
        mBtnSettings = findViewById(R.id.settings);
        mBtnSettings.setOnClickListener(onClickListener);
        mEtaView = findViewById(R.id.eta);
        mBtnExit = findViewById(R.id.exit);
        mBtnExit.setOnClickListener(onClickListener);

//        mNaviView.setVisibleRegionMargin(200, 200, 400, 200);
//        mNaviView.setNaviLineWidth(30);

        mTencentMap = mNaviView.getMap();
        mTencentMap.setIndoorEnabled(true);
        //
        int color, resIdRoad, resIdNavi;
        color = Color.parseColor("#111111");
        resIdRoad = R.drawable.navi_road_name_background_day;
        resIdNavi = R.drawable.navi_distance_time_background_day;
        mRoadNameView.setBackgroundResource(resIdRoad);
        mNavigationView.setBackgroundResource(resIdNavi);
        mBtnSettings.setTextColor(color);
        mBtnExit.setTextColor(color);

        initBottomDialog();

        mLogTextView = findViewById(R.id.logTextView);
        mLogTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mLogTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mLogTextView.setVisibility(View.INVISIBLE);
                return true;
            }
        });
        mTencentMap.setOnMapLongClickListener(new TencentMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLogTextView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void initTencentNavigation() {
        mWalkNaviManager = DemoUtil.getWalkNaviManager(this);
        mWalkNaviManager.setNaviCallback(mTencentNaviCallback);
        mWalkNaviManager.addNaviView(mINaviView);
        mWalkNaviManager.addNaviView(mNaviView);
        mWalkNaviManager.setInternalTtsEnabled(true);
    }

    private void stopNavigation() {
        if (mSimulateNavi) {
            if (mWalkNaviManager != null) {
                mWalkNaviManager.stopNavi();
            }
        } else {
            if (mGpsTrackType == 0) {
                GPSRecordEngine.getInstance().stopRecordTencentLocation(this);
            }
            if (mWalkNaviManager != null) {
                mWalkNaviManager.stopNavi();
            }
        }
    }

    private ILocationSource.LocationSourceListener mLocationSourceListener = new ILocationSource.LocationSourceListener() {
        @Override
        public void onLocationChanged(final GpsLocation location, int error, String reason) {
            updateRealLocation(location);
            if (mRecordLocation) {
                recordRealLocation(location);
            } else {
                clearLocationMarkers();
            }
            mLocationCount++;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLogTextView.append(mLocationCount + " 定位点(" + location.getLatitude() + ", "
                            + location.getLongitude() + "), 楼层:" + location.getFloorName() + "\n");
                    int offset = mLogTextView.getLineCount() * mLogTextView.getLineHeight();
                    if (offset > mLogTextView.getHeight()) {
                        mLogTextView.scrollTo(0, offset - mLogTextView.getHeight());
                    }
                }
            });
            if (mGpsTrackType == 0) {
                GPSRecordEngine.getInstance().recordGpsLocation(location);
            }
        }

        @Override
        public void onStatusUpdate(String name, int status, String desc) {

        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.settings: {
                    Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
                    intent.putExtra("isNavigating", true);
                    intent.putExtra("showNaviPanel", mShowNaviPanel);
                    intent.putExtra("crossingEnlarge", mShowCrossingEnlarged);
                    intent.putExtra("naviMode", mNaviMode);
                    intent.putExtra("arrivedStop", mStopWhenArrived);
                    intent.putExtra("recordLocation", mRecordLocation);
                    startActivityForResult(intent, 0);
                    break;
                }
                case R.id.exit: {
                    stopNavigation();
                    finish();
                    break;
                }
                default:
                    break;
            }
        }
    };

    private WalkNaviCallback mTencentNaviCallback = new WalkNaviCallback() {
        @Override
        public void onOffRoute() {
            showBottomDialog(OFF_ROUTE);
            Toast.makeText(getBaseContext(), "偏航了", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "偏航啦");
        }

        @Override
        public void onRecalculateRouteStarted(int type) {
            showBottomDialog(RECALCULATE_ROUTE_START);
            Toast.makeText(getBaseContext(), "开始重新算路", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "开始重新算路");
        }

        @Override
        public void onRecalculateRouteSuccess(int type,ArrayList<WalkRouteData> walkRouteDataList) {
            showBottomDialog(RECALCULATE_ROUTE_SUCCESS);
            //2s后取消弹窗
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    bottomDialog.cancel();
                } },2*1000);
            Toast.makeText(getBaseContext(), "重新算路成功", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "重新算路成功");
            mRouteIndex = 0;
        }

        @Override
        public void onRecalculateRouteFailure(int type, int errorCode, String errorMessage) {
            showBottomDialog(RECALCULATE_ROUTE_FAILURE);
        }

        @Override
        public void onRecalculateRouteCanceled() {
            bottomDialog.cancel();
        }

        @Override
        public int onVoiceBroadcast(NaviTts tts) {
            Log.e(TAG, "语音播报文案：" + tts.getText());
            return 1;
        }

        @Override
        public void onArrivedDestination() {
            Log.e(TAG, "到达目的地 mStopWhenArrived: " + mStopWhenArrived);
            mIsArrivedDestination = true;
            stopNavigation();
            StringBuilder summary = new StringBuilder(ARRIVED_DESTINATION);
            summary.append("\n\n" + "全程(m): " + totalDistance);
            summary.append("\n"  + "用时(min): " + totalTime);
            summary.append("\n" + "消耗热量(kcal): " + kcal);
            showBottomDialog(summary.toString());
            if (mStopWhenArrived) {
                finish();
            }
        }

        @Override
        public void onStartNavi() {
            mIsArrivedDestination = false;
        }

        @Override
        public void onStopNavi() {

        }

        @Override
        public void onLocationSwitched(String name, boolean on){
            if (!on){
                showBottomDialog(name.toUpperCase() + "未开启");
                Toast.makeText(getBaseContext(), name.toUpperCase() + "未开启", Toast.LENGTH_SHORT).show();
                Log.e(TAG, name.toUpperCase() + "未开启");
            }else{
                bottomDialog.cancel();
            }
        }
        @Override
        public void onLocationStatusChanged(String name, boolean isValid){
            if (!isValid){
                showBottomDialog(name.toUpperCase() + "信号弱");
                Toast.makeText(getBaseContext(), name.toUpperCase() + "信号弱", Toast.LENGTH_SHORT).show();
                Log.e(TAG, name.toUpperCase() + "信号弱");
            }else{
                bottomDialog.cancel();
            }
        }

        @Override
        public void onUpdateAttachedLocation(AttachedLocation attachedLocation) {

        }
    };

    private IWalkNaviView mINaviView = new IWalkNaviView() {

        @Override
        public void onUpdateNavigationData(WalkNaviData data) {
            Log.e(TAG, "navigationData：" + data.getLeftDistance() +"--" + data.getLeftTime());
            updateLeftDistanceTime(data.getLeftDistance(), data.getLeftTime());
            if (data.isIndoor()) {
                mRoadNameView.setVisibility(View.INVISIBLE);
            } else {
                mRoadNameView.setText(data.getCurrentRoadName());
                mRoadNameView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onGpsRssiChanged(int rssi) {

        }
    };

    private void updateLeftDistanceTime(int leftDistance, int leftTime) {
        int color;
        color = Color.parseColor("#111111");
        String distance = DemoUtil.distance2string(leftDistance, false);
        String time = DemoUtil.time2string(leftTime);
        String msg = String.format(Locale.getDefault(), FORMAT_DISTANCE_TIME, distance, time);
        SpannableString str = new SpannableString(msg);
        try {
            Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)");
            Matcher matcher = pattern.matcher(msg);
            int index = 0;
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                //非数字部分
                if (index < start) {
                    str.setSpan(new AbsoluteSizeSpan(15, true), index, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    str.setSpan(new ForegroundColorSpan(color), index, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    str.setSpan(new StyleSpan(Typeface.NORMAL), index, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //数字部分
                str.setSpan(new AbsoluteSizeSpan(22, true), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                str.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                str.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                //
                index = end;
            }
            //非数字部分
            if (index < msg.length()) {
                str.setSpan(new AbsoluteSizeSpan(15, true), index, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                str.setSpan(new ForegroundColorSpan(color), index, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                str.setSpan(new StyleSpan(Typeface.NORMAL), index, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mEtaView.setText(str);
    }

    private void updateRealLocation(GpsLocation gpsLocation) {
        if (mRealLocation == null) {
            LatLng latLng = new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions(latLng)
                    .anchor(0.5f, 0.5f)
                    .level(OverlayLevel.OverlayLevelAboveLabels)
                    .zIndex(1200);

            if (!TextUtils.isEmpty(gpsLocation.getBuildingId()) && !TextUtils.isEmpty(gpsLocation.getFloorName())) {
                markerOptions.indoorInfo(new IndoorInfo(gpsLocation.getBuildingId(), gpsLocation.getFloorName()));
            }

            markerOptions.icon(BitmapDescriptorFactory.fromAsset("navi_marker_location.png"));

            mRealLocation = mTencentMap.addMarker(markerOptions);
        } else {
            LatLng latLng = new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
            boolean sameBuilding = (TextUtils.isEmpty(gpsLocation.getBuildingId()) && TextUtils.isEmpty(mGpsLocation.getBuildingId())) ||
                    (!TextUtils.isEmpty(gpsLocation.getBuildingId()) && !TextUtils.isEmpty(mGpsLocation.getBuildingId()) && !gpsLocation.getBuildingId().equals(mGpsLocation.getBuildingId()));
            boolean sameFloor = (TextUtils.isEmpty(gpsLocation.getFloorName()) && TextUtils.isEmpty(mGpsLocation.getFloorName())) ||
                    (!TextUtils.isEmpty(gpsLocation.getFloorName()) && !TextUtils.isEmpty(mGpsLocation.getFloorName()) && !gpsLocation.getFloorName().equals(mGpsLocation.getFloorName()));
            if (!sameBuilding || !sameFloor) {
                mRealLocation.remove();

                MarkerOptions markerOptions = new MarkerOptions(latLng)
                        .anchor(0.5f, 0.5f)
                        .level(OverlayLevel.OverlayLevelAboveLabels)
                        .zIndex(1200);

                if (!TextUtils.isEmpty(gpsLocation.getBuildingId()) && !TextUtils.isEmpty(gpsLocation.getFloorName())) {
                    markerOptions.indoorInfo(new IndoorInfo(gpsLocation.getBuildingId(), gpsLocation.getFloorName()));
                }

                markerOptions.icon(BitmapDescriptorFactory.fromAsset("navi_marker_location.png"));

                mRealLocation = mTencentMap.addMarker(markerOptions);
            } else {
                mRealLocation.setPosition(latLng);
            }
        }
        mRealLocation.setRotation(gpsLocation.getDirection());

        mGpsLocation = gpsLocation;
    }

    private void recordRealLocation(GpsLocation gpsLocation){
        if (gpsLocation != null){
            LatLng latLng = new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions(latLng)
                    .anchor(0.5f, 0.5f)
                    .level(OverlayLevel.OverlayLevelAboveLabels)
                    .zIndex(1200);

            if (!TextUtils.isEmpty(gpsLocation.getBuildingId()) && !TextUtils.isEmpty(gpsLocation.getFloorName())) {
                markerOptions.indoorInfo(new IndoorInfo(gpsLocation.getBuildingId(), gpsLocation.getFloorName()));
            }

            markerOptions.icon(BitmapDescriptorFactory.fromAsset("record_location@2x.png"));

            Marker locationMarker  = mTencentMap.addMarker(markerOptions);
            locationMarkers.add(locationMarker);
        }
    }

    private void clearLocationMarkers(){
        if (locationMarkers != null && locationMarkers.size() > 0){
            for (Marker marker : locationMarkers){
                marker.remove();
            }
            locationMarkers.clear();
        }
    }

    private void initBottomDialog(){
        bottomDialog = new Dialog(this, R.style.BottomDialog);
        View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_content_circle, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels;
        params.bottomMargin = 0;
        contentView.setLayoutParams(params);
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        info = contentView.findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomDialog.isShowing()){
                    bottomDialog.cancel();
                }
            }
        });
    }

    private void showBottomDialog(String text) {
        info.setText(text);
        if (!bottomDialog.isShowing()){
            bottomDialog.show();
        }
    }

}
