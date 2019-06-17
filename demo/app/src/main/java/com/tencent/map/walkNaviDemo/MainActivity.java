package com.tencent.map.walkNaviDemo;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.*;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.walkNaviDemo.util.DemoUtil;
import com.tencent.map.navi.NaviMode;
import com.tencent.map.navi.RouteSearchCallback;
import com.tencent.map.navi.WalkNaviManager;
import com.tencent.map.navi.data.GpsLocation;
import com.tencent.map.navi.data.NaviPoi;
import com.tencent.map.navi.data.RouteData;
import com.tencent.map.navi.data.line.Line;
import com.tencent.map.navi.data.step.DoorStep;
import com.tencent.map.navi.data.step.ElevatorStep;
import com.tencent.map.navi.data.step.Step;
import com.tencent.map.ui.InfoWindowRelativeLayout;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.LocationSource;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.*;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;

public class MainActivity extends Activity implements TencentMap.OnMapPoiClickListener,
        RouteSearchCallback, View.OnClickListener, EasyPermissions.PermissionCallbacks,
        TencentLocationListener, LocationSource {
    //**************************常量**************************/
    private static final String TAG = "integrated";
    private static final int REQUEST_SETTING = 1;
    private static final int ROUTE_LINE_COLOR = Color.argb(255, 59, 105, 239);
    private static final int ROUTE_LINE_COLOR_GRAY = Color.argb(255, 133, 133, 133);
    private static final int ROUTE_LINE_COLOR_WHITE = Color.argb(255, 255, 255, 255);
    private static final int ROUTE_LINE_WIDTH = 20;
    private int mNaviLineWidthPattern = 10;
    private List<Integer> ROUTE_DOOR_LINE_PATTERN = new ArrayList<>();

    {
        //init DoorLine Pattern
        ROUTE_DOOR_LINE_PATTERN.add(mNaviLineWidthPattern);
        ROUTE_DOOR_LINE_PATTERN.add(mNaviLineWidthPattern * 2);
    }

    /**
     * 真实起点和终点的Z轴层级
     */
    private static final int MARKER_FROM_TO_ZINDEX = 8;
    /**
     * 导航路线上添加的marker的Z轴层级，包括路线规划起终点、门、电梯
     */
    private static final int MARKER_DYNAMIC_ZINDEX = 7;

    /**
     * 地图可视区域与屏幕默认边距
     */
    private int mTopMargin = 200;

    private int mBottomMargin = 200;

    private int mLeftMargin = 200;

    private int mRightMargin =50;
    //**************************常量**************************/


    //********************UI View***********************/
    private MapView mMapView;
    private RelativeLayout mTitleLayout;
    private LinearLayout mLocationLayout;
    private TabLayout mTBLayout;
    private LinearLayout mLlBottomBar;
    private TextView mTvTimeAndDistance;
    private TextView mTvStepDetail;
    private ImageView mIvStartPos;
    private TextView mTvStartPos;
    private TextView mTvEndPos;
    private CheckBox mCbSimulate;
    private Button mBtnStartNavi;
    private ImageView locationButton;
    //********************UI View***********************/


    //******************Map Relevant Obj***************/
    private TencentMap mMap;
    private WalkNaviManager mWalkNaviManager;
    private NaviPoi fromPoi = null;
    private NaviPoi targetPoi = null;

    private Marker fromMarker;
    private Marker targetMarker;
    private Marker realLocation;

    private Bitmap mRealFromBitmap;
    private Bitmap mRealFromBitmapGray;
    private Bitmap mRealToBitmap;
    private Bitmap mRealToBitmapGray;
    private Bitmap doorIn;
    private Bitmap doorInGray;
    private Bitmap doorOut;
    private Bitmap doorOutGray;

    /**
     * 起点和终点所在楼层信息
     */
    private String mRealFromBuildingId;
    private String mRealFromFloorName;
    private String mRealToBuildingId;
    private String mRealToFloorName;
    private String curIndoorBuildingId;
    private String curFloorName;
    private int activeLevelIndex;
    //******************Map Relevant Obj***************/


    //*************************************************/
    private TencentLocationManager mLocationManager;
    private GpsLocation mGpsLocation;
    //*************************************************/

    //*******************设置项*************************/
    private boolean mShowNaviPanel = true;
    private NaviMode mNaviMode = NaviMode.MODE_3DCAR_TOWARDS_UP;
    private boolean mStopWhenArrived = false;
    private int mGpsTrackType = 2;
    private String mGpsTrackPath;
    private boolean mRecordLocation = false;
    private String mFromAddress;
    private String mToAddress;
    //*******************设置项*************************/

    //**************************polyline和marker**************************/
    private ArrayList<Polyline> mRoutePolylines = new ArrayList<Polyline>();
    private ArrayList<Marker> mRouteMarkers = new ArrayList<Marker>();
    private ArrayList<DoorMarker> mDoorMarkers = new ArrayList<>();
    //**************************polyline和marker**************************/

    private RouteData curRouteData;
    private TencentLocation fistLocation;
    private TencentLocation mCurrentLocation;
    private Location location;
    private OnLocationChangedListener mLocationChangedListener;

    private BluetoothAdapter mBluetoothAdapter;
    private WifiManager mWifiManager;
    private boolean isBluetoothEnable = true;
    private boolean isWifiEnable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews();
        initListeners();
        checkPermissions();
        checkBluetoothAndWifi();
    }

    private void initViews() {
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.mapView);
        TabLayout tabLayout = findViewById(R.id.tbLayout);
        TabLayout.Tab tab = tabLayout.getTabAt(2);
        if (tab != null) {
            tab.select();
        }
        mTitleLayout = findViewById(R.id.titleLayout);
        mLocationLayout = findViewById(R.id.locationLayout);
        mTBLayout = findViewById(R.id.tbLayout);
        mLlBottomBar = findViewById(R.id.llBottomBar);
        mTvTimeAndDistance = findViewById(R.id.tvTimeAndDistance);
        mTvStepDetail = findViewById(R.id.tvStepDesc);
        mIvStartPos = findViewById(R.id.ivStartPos);
        mTvStartPos = findViewById(R.id.tvStartPos);
        mTvEndPos = findViewById(R.id.tvEndPos);
        mCbSimulate = findViewById(R.id.simulate);
        mBtnStartNavi = findViewById(R.id.btnStartNavi);

        //设置定位图标
        locationButton = (ImageView)findViewById(R.id.imageView);
        locationButton.setOnClickListener(this);

        mRealFromBitmap =  DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.NAVI_LINE_FROM_MARKER);
        mRealFromBitmapGray = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.NAVI_LINE_FROM_MARKER_GRAY);
        mRealToBitmap = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.NAVI_LINE_TO_MARKER);
        mRealToBitmapGray = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.NAVI_LINE_TO_MARKER_GRAY);
        doorIn = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.DOOR_IN);
        doorInGray = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.DOOR_IN_GRAY);
        doorOut = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.DOOR_OUT);
        doorOutGray = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.DOOR_OUT_GRAY);

        mMap = mMapView.getMap();
        mMap.setIndoorEnabled(true);
        mMap.enableMultipleInfowindow(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(19));
        mMap.setInfoWindowAdapter(mInfoWindowAdapter);
        mMap.setOnIndoorStateChangeListener(mOnIndoorStateChangeListener);
        mMap.setLocationSource(this);
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        Bitmap bitmapOrigin = BitmapFactory.decodeResource(getResources(), R.mipmap.my_location);
        Bitmap bitmap = changeBitmapSize(bitmapOrigin, 100, 100);
        myLocationStyle.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        myLocationStyle.myLocationType(LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        mMap.setMyLocationStyle(myLocationStyle);
        mMap.setMyLocationEnabled(true);
    }

    private void initListeners() {
        mMap.setOnMapPoiClickListener(this);
        mWalkNaviManager = DemoUtil.getWalkNaviManager(this);
        mIvStartPos.setOnClickListener(this);
    }

    private void checkPermissions() {
        //所要申请的权限
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= 28) {
            permissions.add("android.permission.ACCESS_BACKGROUND_LOCATION");
        }
        int size = permissions.size();
        String[] perms = permissions.toArray(new String[size]);
        if (EasyPermissions.hasPermissions(this, perms)) {//检查是否获取该权限
            Log.i(TAG, "已获取权限");
        } else {
            //第二个参数是被拒绝后再次申请该权限的解释
            //第三个参数是请求码
            //第四个参数是要申请的权限
            EasyPermissions.requestPermissions(this, "必要的权限", 0, perms);
        }
    }

    private void checkBluetoothAndWifi(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null){
            isBluetoothEnable = mBluetoothAdapter.isEnabled();
        }

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null){
            isWifiEnable = mWifiManager.isWifiEnabled();
        }

        if (isBluetoothEnable && isWifiEnable){
            return;
        }
        if (!isBluetoothEnable && !isWifiEnable){
            Toast.makeText(this, "请开启蓝牙和WIFI", Toast.LENGTH_LONG).show();
        }else if (!isBluetoothEnable){
            Toast.makeText(this, "请开启蓝牙", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "请开启WIFI", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 改变图片大小
     * @param bitmap
     * @param newWidth 新宽度
     * @param newHeight 新高度
     * @return
     */
    private Bitmap changeBitmapSize(Bitmap bitmap, int newWidth, int newHeight){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //计算压缩的比率
        float scaleWidth=((float)newWidth)/width;
        float scaleHeight=((float)newHeight)/height;

        //获取想要缩放的matrix
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);

        //获取新的bitmap
        bitmap=Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
        return bitmap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
        mBtnStartNavi.setEnabled(true);
        mBtnStartNavi.setBackgroundResource(R.drawable.btn_start_navi);
        startLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
        stopLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMapView != null) {
            mMapView.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mLocationChangedListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mLocationChangedListener = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_SETTING: {
                    mShowNaviPanel = data.getBooleanExtra("showNaviPanel", true);
                    mNaviMode = (NaviMode) data.getSerializableExtra("naviMode");
                    mStopWhenArrived = data.getBooleanExtra("arrivedStop", true);
                    mGpsTrackType = data.getIntExtra("gpsTrackType", 2);
                    mGpsTrackPath = data.getStringExtra("gpsTrackPath");
                    mRecordLocation = data.getBooleanExtra("recordLocation", false);
                    break;
                }
                default:
                    break;
            }
        }
    }

    @Override
    public void onClicked(MapPoi mapPoi) {
        //POI点击，选中起始和结束的导航点
        if ((fromMarker != null && targetMarker != null) || mapPoi == null) {
            return;
        }

        IndoorInfo indoorInfo = null;
        if (mapPoi instanceof IndoorMapPoi) {
            indoorInfo = new IndoorInfo(((IndoorMapPoi) mapPoi).getBuildingId()
                    , ((IndoorMapPoi) mapPoi).getFloorName());
        }
        if (targetMarker == null) {
            if (mCurrentLocation == null){
                Toast.makeText(this, "没有定位信息，请检查WIFI、GPS、蓝牙是否打开", Toast.LENGTH_LONG).show();
            }else{
                //设置起点为当前位置
                mRealFromBuildingId = mCurrentLocation.getIndoorBuildingId();
                mRealFromFloorName = mCurrentLocation.getIndoorBuildingFloor();
                MarkerOptions fromMarkerOptions = new MarkerOptions(new LatLng(location.getLatitude(), location.getLongitude()));
                fromMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(mRealFromBitmap))
                        .anchor(0.5f, 0.5f)
                        .level(OverlayLevel.OverlayLevelAboveLabels)
                        .title(mRealFromFloorName)
                        .zIndex(MARKER_FROM_TO_ZINDEX);
                fromMarker = mMap.addMarker(fromMarkerOptions);
                fromMarker.setClickable(false);
                fromMarker.hideInfoWindow();
                fromPoi = location2NaviPoi(mCurrentLocation);
                mTvStartPos.setText("我的位置");

                //设置终点为点选poi
                if (indoorInfo != null){
                    mRealToBuildingId = indoorInfo.getBuildingId();
                    mRealToFloorName = indoorInfo.getFloorName();
                }
                MarkerOptions targetMarkerOptions = new MarkerOptions(mapPoi.position).title(mapPoi.getName());
                targetMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(mRealToBitmap))
                        .anchor(0.5f, 0.5f)
                        .level(OverlayLevel.OverlayLevelAboveLabels)
                        .title(mRealToFloorName)
                        .zIndex(MARKER_FROM_TO_ZINDEX);
                targetMarker = mMap.addMarker(targetMarkerOptions);
                targetMarker.setClickable(false);
                targetMarker.hideInfoWindow();
                targetPoi = mapPoi2NaviPoi(mapPoi);
                mTvEndPos.setText(mapPoi.getName());

                searchRoutes(fromPoi, targetPoi);
            }
        }
    }

    private NaviPoi mapPoi2NaviPoi(MapPoi mapPoi) {
        if (mapPoi == null) {
            return null;
        }

        NaviPoi naviPoi = new NaviPoi();
        naviPoi.setWord(mapPoi.getName());
        naviPoi.setLatitude(mapPoi.getPosition().latitude);
        naviPoi.setLongitude(mapPoi.getPosition().longitude);
        if (mapPoi instanceof IndoorMapPoi) {
            IndoorMapPoi indoorMapPoi = (IndoorMapPoi) mapPoi;
            naviPoi.setBuildingId(indoorMapPoi.getBuildingId());
            naviPoi.setFloorName(indoorMapPoi.getFloorName());
        }

        return naviPoi;
    }

    private NaviPoi location2NaviPoi(TencentLocation location) {
        if (location == null) {
            return null;
        }

        NaviPoi naviPoi = new NaviPoi();
        naviPoi.setLatitude(location.getLatitude());
        naviPoi.setLongitude(location.getLongitude());
        if (location.getIndoorBuildingId() != null){
            naviPoi.setBuildingId(location.getIndoorBuildingId());
        }
        if (location.getIndoorBuildingFloor() != null){
            naviPoi.setFloorName(location.getIndoorBuildingFloor());
        }

        return naviPoi;
    }

    @Override
    public void onRouteSearchFailure(int i, String s) {
        Toast.makeText(this, "算路失败! \n errorCode:" + i + ",errorMsg:" + s, Toast.LENGTH_LONG).show();
        fromPoi = null;
        targetPoi = null;
        mMap.clear();
    }

    @Override
    public void onRouteSearchSuccess(ArrayList<RouteData> routes) {
        if (routes == null || routes.size() == 0) {
            return;
        }

        //选择一条路线，虽然目前检索只返回一条
        curRouteData = routes.get(0);
        Log.d(TAG, "points num: " + curRouteData.getRoutePoints().size());
        Log.d(TAG, "points: " + curRouteData.getRoutePoints());
        drawRoute(curRouteData);
        drawRouteMarkers(curRouteData);
        fillBottomRouteInfoPanel(curRouteData);
        zoomMapToSpan(curRouteData);
        changeMarkerState();
    }

    private void searchRoutes(NaviPoi from, NaviPoi to) {
        mWalkNaviManager.searchRoute(from, to, this);
    }

    /**
     * 绘制导航路线
     *
     * @param routeData
     */
    private void drawRoute(RouteData routeData) {
        ArrayList<LatLng> points = routeData.getRoutePoints();
        ArrayList<Line> lines = routeData.getRenderLines();
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            PolylineOptions polylineOptions = new PolylineOptions()
                    .latLngs(points.subList(line.getFromIndex(), line.getToIndex() + 1))
                    .width(ROUTE_LINE_WIDTH)
                    .color(ROUTE_LINE_COLOR)
                    .arrow(true)
                    .arrowTexture(BitmapDescriptorFactory.fromAsset(NaviConfig.ARROW_TEXTURE))
                    .lineCap(false);
            String bid = line.getBuildingId();
            String floorName = line.getFloorName();
            if (!TextUtils.isEmpty(bid) && !TextUtils.isEmpty(floorName)) {
                IndoorInfo indoorInfo = new IndoorInfo(bid, floorName);
                polylineOptions.indoorInfo(indoorInfo);
            }
            Polyline polyline = mMap.addPolyline(polylineOptions);
            mRoutePolylines.add(polyline);
        }
        if (fromMarker != null && !fromMarker.getPosition().equals(points.get(0))) {
            ArrayList<LatLng> latlngs = new ArrayList<>();
            latlngs.add(fromMarker.getPosition());
            latlngs.add(points.get(0));
            Polyline polyline = mMap.addPolyline(new PolylineOptions()
                    .color(Color.BLACK)
                    .pattern(ROUTE_DOOR_LINE_PATTERN)
                    .latLngs(latlngs)
                    .lineCap(false));
            mRoutePolylines.add(polyline);
        }
        if (targetMarker != null && !targetMarker.getPosition().equals(points.get(points.size() - 1))) {
            ArrayList<LatLng> latlngs = new ArrayList<>();
            latlngs.add(targetMarker.getPosition());
            latlngs.add(points.get(points.size() - 1));
            Polyline polyline = mMap.addPolyline(new PolylineOptions()
                    .color(Color.BLACK)
                    .pattern(ROUTE_DOOR_LINE_PATTERN)
                    .latLngs(latlngs)
                    .lineCap(false));
            mRoutePolylines.add(polyline);
        }
    }

    /**
     * 绘制路线上的出口、电梯等Marker
     */
    private void drawRouteMarkers(RouteData routeData) {
        ArrayList<LatLng> points = routeData.getRoutePoints();
        ArrayList<Step> steps = routeData.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            if (step instanceof ElevatorStep) {
                int elevatorType = ((ElevatorStep) step).getType();
                if (elevatorType <= ElevatorStep.TYPE_NONE || elevatorType > ElevatorStep.TYPE_STAIRS) {
                    continue;
                }
                String buildingId = ((ElevatorStep) step).getBuildingId();
                int startFloor = ((ElevatorStep) step).getStartFloor();
                int targetFloor = ((ElevatorStep) step).getEndFloor();
                String startFloorName = ((ElevatorStep) step).getStartFloorName();
                String targetFloorName = ((ElevatorStep) step).getEndFloorName();
                LatLng startPosition = points.get(step.getFromIndex());
                LatLng targetPosition = points.get(step.getToIndex());
                Bitmap downBitmap = null;
                Bitmap upBitmap = null;
                String introduce = "";
                if (startFloor > targetFloor) {
                    introduce = "下行至" + targetFloorName;
                } else if (startFloor < targetFloor) {
                    introduce = "上行至" + targetFloorName;
                }
                switch (elevatorType) {
                    case ElevatorStep.TYPE_ELEVATOR:
                        downBitmap = DemoUtil.readAssetsImg(this, NaviConfig.ELEVATOR_DOWN);
                        upBitmap = DemoUtil.readAssetsImg(this, NaviConfig.ELEVATOR_UP);
                        introduce = "乘电梯 " + introduce;
                        break;
                    case ElevatorStep.TYPE_ESCALATOR:
                        downBitmap = DemoUtil.readAssetsImg(this, NaviConfig.ESCALATOR_DOWN);
                        upBitmap = DemoUtil.readAssetsImg(this, NaviConfig.ESCALATOR_UP);
                        introduce = "乘扶梯 " + introduce;
                        break;
                    case ElevatorStep.TYPE_PASSENGER_ELEVATOR:
                        downBitmap = DemoUtil.readAssetsImg(this, NaviConfig.ELEVATOR_DOWN);
                        upBitmap = DemoUtil.readAssetsImg(this, NaviConfig.ELEVATOR_UP);
                        introduce = "乘客梯 " + introduce;
                        break;
                    case ElevatorStep.TYPE_FREIGHT_ELEVATOR:
                        downBitmap = DemoUtil.readAssetsImg(this, NaviConfig.ELEVATOR_DOWN);
                        upBitmap = DemoUtil.readAssetsImg(this, NaviConfig.ELEVATOR_UP);
                        introduce = "乘货梯 " + introduce;
                        break;
                    case ElevatorStep.TYPE_STAIRS:
                        downBitmap = DemoUtil.readAssetsImg(this, NaviConfig.STAIR_DOWN);
                        upBitmap = DemoUtil.readAssetsImg(this, NaviConfig.STAIR_UP);
                        introduce = "走楼梯 " + introduce;
                        break;
                    default:
                        break;
                }

                MarkerOptions startMarkerOption = new MarkerOptions(startPosition)
                        .indoorInfo(new IndoorInfo(buildingId, startFloorName))
                        .anchor(0.5f, 0.5f)
                        .title(introduce)
                        .level(OverlayLevel.OverlayLevelAboveLabels)
                        .zIndex(MARKER_DYNAMIC_ZINDEX);
                MarkerOptions targetMarkerOption = new MarkerOptions(targetPosition)
                        .indoorInfo(new IndoorInfo(buildingId, targetFloorName))
                        .anchor(0.5f, 0.5f)
                        .level(OverlayLevel.OverlayLevelAboveLabels)
                        .zIndex(MARKER_DYNAMIC_ZINDEX);

                if (startFloor > targetFloor) {
                    startMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(downBitmap));
                    targetMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(downBitmap));
                } else {
                    startMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(upBitmap));
                    targetMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(upBitmap));
                }
                Marker startMarker = mMap.addMarker(startMarkerOption);
                startMarker.showInfoWindow();
                Marker targetMarker = mMap.addMarker(targetMarkerOption);
                mRouteMarkers.add(startMarker);
                mRouteMarkers.add(targetMarker);
            } else if (step instanceof DoorStep) {
                String buildingId = ((DoorStep) step).getBuildingId();
                String floorName = ((DoorStep) step).getFloorName();
                String name = ((DoorStep) step).getName();
                int type = ((DoorStep) step).getType();
                LatLng doorPosition = ((DoorStep) step).getLocation();
                MarkerOptions markerOptions = new MarkerOptions(doorPosition)
                        .anchor(0.5f, 0.5f)
                        .level(OverlayLevel.OverlayLevelAboveLabels)
                        .zIndex(MARKER_DYNAMIC_ZINDEX);

                if (type == DoorStep.DOOR_TYPE_ENTER) {
                    Bitmap doorIn = DemoUtil.readAssetsImg(this, NaviConfig.DOOR_IN);
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(doorIn));
                    markerOptions.title("进" + name);
                }

                if (type == DoorStep.DOOR_TYPE_EXIT) {
                    Bitmap doorOut = DemoUtil.readAssetsImg(this, NaviConfig.DOOR_OUT);
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(doorOut));
                    markerOptions.title("出" + name);
                }

                Marker marker = mMap.addMarker(markerOptions);
                marker.showInfoWindow();
                mRouteMarkers.add(marker);
                DoorMarker doorMarker = new DoorMarker();
                doorMarker.setMarker(marker);
                doorMarker.setBuildingId(buildingId);
                doorMarker.setFloorName(floorName);
                mDoorMarkers.add(doorMarker);
            }
        }
    }

    private void fillBottomRouteInfoPanel(RouteData routeData) {
        if (routeData == null) {
            return;
        }

        /*******************时间及距离信息*******************/
        mTvTimeAndDistance.setText(null);
        StringBuffer routeInfo = new StringBuffer();

        //时长
        String time = DemoUtil.time2string(routeData.getTime());
        routeInfo.append(time + " ");

        //距离
        String distance = DemoUtil.distance2string(routeData.getDistance(), false);
        routeInfo.append(distance);

        if (!TextUtils.isEmpty(mTvTimeAndDistance.toString().trim())) {
            mTvTimeAndDistance.setText(routeInfo.toString());
            mTvTimeAndDistance.setVisibility(View.VISIBLE);
        } else {
            mTvTimeAndDistance.setVisibility(View.GONE);
        }
        /*******************时间及距离信息*******************/


        /*******************Step具体信息*******************/
        mTvStepDetail.setText(null);
        StringBuffer stepInfoDetail = new StringBuffer();
        if (routeData.getLight() > 0) {
            stepInfoDetail.append("红绿灯")
                    .append(Integer.toString(routeData.getLight()));
        }

        if (routeData.getCrosswalk() > 0) {
            stepInfoDetail.append(" 人行横道")
                    .append(Integer.toString(routeData.getCrosswalk()));
        }

        if (routeData.getOverpass() > 0) {
            stepInfoDetail.append(" 天桥")
                    .append(Integer.toString(routeData.getOverpass()));
        }

        if (routeData.getUnderpass() > 0) {
            stepInfoDetail.append(" 地下通道")
                    .append(Integer.toString(routeData.getUnderpass()));
        }

        if (!TextUtils.isEmpty(stepInfoDetail.toString().trim())) {
            mTvStepDetail.setText(stepInfoDetail.toString());
            mTvStepDetail.setVisibility(View.VISIBLE);
        } else {
            mTvStepDetail.setVisibility(View.GONE);
        }
        /*******************路线具体信息*******************/

        mLlBottomBar.setVisibility(View.VISIBLE);
    }

    private void zoomMapToSpan(final RouteData mCurrentRoute) {
        if (mCurrentRoute == null) {
            return;
        }

        mLlBottomBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                mMap.setIndoorFloor(mRealFromBuildingId, mRealFromFloorName);
                mMap.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(new LatLngBounds.Builder()
                                .include(mCurrentRoute.getRoutePoints()).include(fromMarker.getPosition()).include(targetMarker.getPosition()).build(),
                        mLeftMargin, mRightMargin, mTopMargin, mBottomMargin));

                mLlBottomBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnExchangeNaviPos:
                if (targetPoi == null) {
                    return;
                }

                swapNaviPos();
                break;
            case R.id.reset:
                if (mMap != null) {
                    mMap.clear();
                    fromMarker = null;
                    targetMarker = null;
                    realLocation = null;
                    mTvStartPos.setText("我的位置");
                    mTvEndPos.setText("点击底图POI设置终点");
                }

                if (mLlBottomBar != null) {
                    mLlBottomBar.setVisibility(View.GONE);
                }

                mBtnStartNavi.setEnabled(true);
                mBtnStartNavi.setBackgroundResource(R.drawable.btn_start_navi);
                break;
            case R.id.btnStartNavi: {
                mBtnStartNavi.setEnabled(false);
                mBtnStartNavi.setBackgroundResource(R.drawable.btn_start_navi_disable);
                mFromAddress = mTvStartPos.getText().toString();
                mToAddress = mTvEndPos.getText().toString();
                if (mGpsTrackType == 0 && !mGpsTrackPath.endsWith(".gps")) {
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS", Locale.CHINA);
                    String time = timeFormatter.format(System.currentTimeMillis());
                    mGpsTrackPath = mGpsTrackPath + File.separator + "{" + mFromAddress + "}至{" + mToAddress + "}_" + time + ".gps";
                }
                Intent intent = new Intent(this, NavigationActivity.class);
                intent.putExtra("routeIndex", 0);
                intent.putExtra("totalDistance", curRouteData.getDistance());
                intent.putExtra("totalTime", curRouteData.getTime());
                intent.putExtra("kcal", curRouteData.getKcal());
                intent.putExtra("light", curRouteData.getLight());
                intent.putExtra("crosswalk", curRouteData.getCrosswalk());
                intent.putExtra("overpass",curRouteData.getOverpass());
                intent.putExtra("underpass", curRouteData.getUnderpass());
                intent.putExtra("showNaviPanel", mShowNaviPanel);
                intent.putExtra("naviMode", mNaviMode);
                intent.putExtra("simulateNavi", mCbSimulate.isChecked());
                intent.putExtra("gpsTrackType", mGpsTrackType);
                intent.putExtra("gpsTrackPath", mGpsTrackPath);
                intent.putExtra("arrivedStop", mStopWhenArrived);
                intent.putExtra("recordLocation", mRecordLocation);
                startActivity(intent);

                break;
            }
            case R.id.settings: {
                Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
                intent.putExtra("showNaviPanel", mShowNaviPanel);
                intent.putExtra("naviMode", mNaviMode);
                intent.putExtra("gpsTrackType", mGpsTrackType);
                intent.putExtra("gpsTrackPath", mGpsTrackPath);
                intent.putExtra("arrivedStop", mStopWhenArrived);
                intent.putExtra("recordLocation", mRecordLocation);
                startActivityForResult(intent, REQUEST_SETTING);
                break;
            }
            case R.id.imageView:
                if (mCurrentLocation != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));
                    mMap.setIndoorFloor(mCurrentLocation.getIndoorBuildingId(), mCurrentLocation.getIndoorBuildingFloor());
                }
                break;
            default:
                break;
        }
    }

    /**
     * 交换导航起终点
     */
    private void swapNaviPos() {
        String temp = mTvStartPos.getText().toString();
        mTvStartPos.setText(mTvEndPos.getText().toString());
        mTvEndPos.setText(temp);

        NaviPoi tempPoi = fromPoi;
        fromPoi = targetPoi;
        targetPoi = tempPoi;

        mMap.clear();

        //将起终点的Marker Icon互换
        MarkerOptions fromMarkerOptions = fromMarker.getOptions();
        fromMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.navi_marker_end));

        MarkerOptions targetMarkerOptions = targetMarker.getOptions();
        targetMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.navi_marker_start));

        targetMarker = mMap.addMarker(fromMarkerOptions);
        targetMarker.showInfoWindow();
        fromMarker = mMap.addMarker(targetMarkerOptions);
        fromMarker.showInfoWindow();

        //重新检索路线
        searchRoutes(fromPoi, targetPoi);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        if (TencentLocation.ERROR_OK == error) {
            //首次定位成功时，设置楼层信息
            if (fistLocation == null){
                fistLocation = mLocationManager.getLastKnownLocation();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(fistLocation.getLatitude(), fistLocation.getLongitude())));
                mMap.setIndoorFloor(fistLocation.getIndoorBuildingId(), fistLocation.getIndoorBuildingFloor());
            }

            // 定位成功
            StringBuilder sb = new StringBuilder();
            sb.append("来源=").append(location.getProvider())
                    .append(", 纬度=").append(location.getLatitude())
                    .append(", 经度=").append(location.getLongitude())
                    .append(", 精度=").append(location.getAccuracy())
                    .append(", BuildingId=").append(location.getIndoorBuildingId())
                    .append(", FloorName=").append(location.getIndoorBuildingFloor());
            Log.d("integrated", sb.toString());

            mCurrentLocation = location;
            updateRealLocation();
        }
    }

    @Override
    public void onStatusUpdate(String name, int status, String desc) {

    }

    private void updateRealLocation() {
        if (mCurrentLocation == null) {
            return;
        }
        location = new Location(mCurrentLocation.getProvider());
        location.setLatitude(mCurrentLocation.getLatitude());
        location.setLongitude(mCurrentLocation.getLongitude());
        location.setAccuracy(mCurrentLocation.getAccuracy());
        location.setBearing((float)mCurrentLocation.getDirection());
        if (mLocationChangedListener != null && mMap != null) {
//            mMap.setIndoorFloor(mCurrentLocation.getIndoorBuildingId(), mCurrentLocation.getIndoorBuildingFloor());
            mLocationChangedListener.onLocationChanged(location);
        }
    }

    private void startLocation() {
        Log.i(TAG, "startLocation");
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setInterval(1000);
        request.setAllowCache(true);
        request.setAllowDirection(true);
        //request.setIndoorLocationMode(true);
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_GEO);
        mLocationManager = TencentLocationManager.getInstance(this);
        int error = mLocationManager.requestLocationUpdates(request, this);
        if (error == 0) {
            mLocationManager.startIndoorLocation();
        }
        Log.i(TAG, "startLocation error: " + error);
    }

    private void stopLocation() {
        Log.i(TAG, "stopLocation");
        if (mLocationManager != null) {
            mLocationManager.stopIndoorLocation();
            mLocationManager.removeUpdates(this);
            mLocationManager = null;
        }
    }

    /**
     * 自定义InfoWindow
     */
    private TencentMap.InfoWindowAdapter mInfoWindowAdapter = new TencentMap.InfoWindowAdapter() {

        @Override
        public View getInfoWindow(Marker marker) {
            InfoWindowRelativeLayout infoWindowView = new InfoWindowRelativeLayout(getApplicationContext());
            infoWindowView.setBackgroundColor(Color.TRANSPARENT);

            TextView textView = new TextView(getApplicationContext());
            textView.setBackgroundColor(Color.TRANSPARENT);
            textView.setText(marker.getTitle());
            textView.setPadding(10,0,10,0);

            if (marker == fromMarker || marker == targetMarker) {
                infoWindowView.setFillColor(ROUTE_LINE_COLOR_GRAY);
                textView.setTextColor(ROUTE_LINE_COLOR_WHITE);
            }else{
                infoWindowView.setFillColor(ROUTE_LINE_COLOR_WHITE);
                textView.setTextColor(ROUTE_LINE_COLOR);
            }

            for (DoorMarker doorMarker : mDoorMarkers){
                //当前建筑非当前楼层，置灰marker
                if (marker == doorMarker.getMarker()
                        && !TextUtils.isEmpty(curIndoorBuildingId) && !TextUtils.isEmpty(curFloorName)
                        && !TextUtils.isEmpty(doorMarker.getBuildingId()) && !TextUtils.isEmpty(doorMarker.getFloorName())
                        && curIndoorBuildingId.equals(doorMarker.getBuildingId()) && !curFloorName.equals(doorMarker.getFloorName())){
                    infoWindowView.setFillColor(ROUTE_LINE_COLOR_GRAY);
                    textView.setTextColor(ROUTE_LINE_COLOR_WHITE);
                }
            }

            infoWindowView.addView(textView);
            infoWindowView.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            infoWindowView.setBubbleParams(InfoWindowRelativeLayout.BubbleOrientation.BOTTOM, (float) infoWindowView.getMeasuredWidth()/2);

            return infoWindowView;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    };

    /**
     * 室内图状态变化监听回调，用于起点和终点marker的状态处理
     */
    private TencentMap.OnIndoorStateChangeListener mOnIndoorStateChangeListener = new TencentMap.OnIndoorStateChangeListener() {
        @Override
        public boolean onIndoorBuildingFocused() {
            return false;
        }

        @Override
        public boolean onIndoorLevelActivated(IndoorBuilding indoorBuilding) {
            //记录当前室内信息
            curIndoorBuildingId = indoorBuilding.getBuidlingId();
            activeLevelIndex = indoorBuilding.getActiveLevelIndex();
            String[] activedIndoorFloorNames = mMap.getActivedIndoorFloorNames();
            if (curIndoorBuildingId == null || activedIndoorFloorNames == null || activeLevelIndex < 0
                    || activedIndoorFloorNames.length == 0 || activeLevelIndex >= activedIndoorFloorNames.length) {
                return false;
            }
            curFloorName = activedIndoorFloorNames[activeLevelIndex];

            changeMarkerState();

            //显示室内图时，仅在同建筑同楼层显示定位图标
            if (mCurrentLocation != null
                    && curIndoorBuildingId.equals(mCurrentLocation.getIndoorBuildingId())
                    && curFloorName.equals(mCurrentLocation.getIndoorBuildingFloor())) {
                mMap.setMyLocationEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
            }

            return true;
        }

        @Override
        public boolean onIndoorBuildingDeactivated() {
            curIndoorBuildingId = null;
            curFloorName = null;

            changeMarkerState();

            //不显示室内图时显示定位图标
            mMap.setMyLocationEnabled(true);

            return true;
        }
    };

    /**
     * 起点和终点marker的状态
     */
    private void changeMarkerState() {
        if (!TextUtils.isEmpty(curIndoorBuildingId) && !TextUtils.isEmpty(curFloorName)){
            //当前建筑非当前楼层，置灰起点marker
            if (!TextUtils.isEmpty(mRealFromBuildingId) && !TextUtils.isEmpty(mRealFromFloorName)
                    && curIndoorBuildingId.equals(mRealFromBuildingId) && !curFloorName.equals(mRealFromFloorName)){
                realFromMarkerGray();
            }else{
                realFromMarkerHigh();
            }

            //当前建筑非当前楼层，置灰终点marker
            if (!TextUtils.isEmpty(mRealToBuildingId) && !TextUtils.isEmpty(mRealToFloorName)
                    && curIndoorBuildingId.equals(mRealToBuildingId) && !curFloorName.equals(mRealToFloorName)){
                realToMarkerGray();
            }else{
                realToMarkerHigh();
            }

            //当前建筑非当前楼层，置灰门marker
            for (DoorMarker doorMarker : mDoorMarkers){
                String doorBuildingId = doorMarker.getBuildingId();
                String doorFloorName = doorMarker.getFloorName();
                if (!TextUtils.isEmpty(doorBuildingId) && !TextUtils.isEmpty(doorFloorName)
                        && doorBuildingId.equals(curIndoorBuildingId) &&  !doorFloorName.equals(curFloorName)) {
                    changeDoorMarkerState(doorMarker.getMarker(), true);
                } else {
                    changeDoorMarkerState(doorMarker.getMarker(), false);
                }
            }
        }else{
            //当前室内图未激活时，高亮marker
            realFromMarkerHigh();
            realToMarkerHigh();
            for (DoorMarker doorMarker : mDoorMarkers){
                changeDoorMarkerState(doorMarker.getMarker(), false);
            }
        }
    }

    private void realFromMarkerHigh() {
        if (fromMarker != null){
            fromMarker.setIcon(BitmapDescriptorFactory.fromBitmap(mRealFromBitmap));
            fromMarker.hideInfoWindow();
        }
    }

    private void realFromMarkerGray() {
        if (fromMarker != null){
            fromMarker.setIcon(BitmapDescriptorFactory.fromBitmap(mRealFromBitmapGray));
            fromMarker.showInfoWindow();
        }
    }

    private void realToMarkerHigh() {
        if (targetMarker != null){
            targetMarker.setIcon(BitmapDescriptorFactory.fromBitmap(mRealToBitmap));
            targetMarker.hideInfoWindow();
        }
    }

    private void realToMarkerGray() {
        if (targetMarker != null){
            targetMarker.setIcon(BitmapDescriptorFactory.fromBitmap(mRealToBitmapGray));
            targetMarker.showInfoWindow();
        }
    }

    private void changeDoorMarkerState(Marker doorMarker, boolean isGray){
        if (doorMarker.getOptions().getTitle().contains("进")){
            //进门
            if (isGray){
                doorMarker.setIcon(BitmapDescriptorFactory.fromBitmap(doorInGray));
                doorMarker.refreshInfoWindow();
            }else{
                doorMarker.setIcon(BitmapDescriptorFactory.fromBitmap(doorIn));
                doorMarker.refreshInfoWindow();
            }
        }else{
            //出门
            if (isGray){
                doorMarker.setIcon(BitmapDescriptorFactory.fromBitmap(doorOutGray));
                doorMarker.refreshInfoWindow();
            }else{
                doorMarker.setIcon(BitmapDescriptorFactory.fromBitmap(doorOut));
                doorMarker.refreshInfoWindow();
            }
        }
    }
}
