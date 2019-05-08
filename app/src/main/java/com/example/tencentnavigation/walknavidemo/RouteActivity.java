package com.example.tencentnavigation.walknavidemo;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tencentnavigation.walknavidemo.util.DemoUtil;
import com.example.tencentnavigation.walknavidemo.util.DoorMarker;
import com.example.tencentnavigation.walknavidemo.util.NaviConfig;
import com.tencent.map.navi.RouteSearchCallback;
import com.tencent.map.navi.WalkNaviManager;
import com.tencent.map.navi.data.NaviPoi;
import com.tencent.map.navi.data.RouteData;
import com.tencent.map.navi.data.line.Line;
import com.tencent.map.navi.data.step.DoorStep;
import com.tencent.map.navi.data.step.ElevatorStep;
import com.tencent.map.navi.data.step.Step;
import com.tencent.map.ui.InfoWindowRelativeLayout;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.SupportMapFragment;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.IndoorBuilding;
import com.tencent.tencentmap.mapsdk.maps.model.IndoorInfo;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.OverlayLevel;
import com.tencent.tencentmap.mapsdk.maps.model.Polyline;
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions;

import java.util.ArrayList;

import static com.example.tencentnavigation.walknavidemo.util.NaviConfig.MARKER_DYNAMIC_ZINDEX;
import static com.example.tencentnavigation.walknavidemo.util.NaviConfig.MARKER_FROM_TO_ZINDEX;
import static com.example.tencentnavigation.walknavidemo.util.NaviConfig.ROUTE_LINE_COLOR;
import static com.example.tencentnavigation.walknavidemo.util.NaviConfig.ROUTE_LINE_COLOR_GRAY;
import static com.example.tencentnavigation.walknavidemo.util.NaviConfig.ROUTE_LINE_COLOR_WHITE;

/**
 * 步行路线规划
 */
public class RouteActivity extends AppCompatActivity {

    private WalkNaviManager mWalkNaviManager;
    private TencentMap mTencentMap;
    //定义起终点
    private NaviPoi fromPoi;
    private NaviPoi targetPoi;
    //路线规划结果
    private RouteData mRouteData;

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
    private String curIndoorBuildingId;
    private String curFloorName;
    private int activeLevelIndex;

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

    //**************************polyline和marker**************************/
    private ArrayList<Polyline> mRoutePolylines = new ArrayList<Polyline>();
    private ArrayList<Marker> mRouteMarkers = new ArrayList<Marker>();
    private ArrayList<DoorMarker> mDoorMarkers = new ArrayList<>();
    //**************************polyline和marker**************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        initmap();
        initResource();
        initRoute();
    }

    /**
     * 初始化地图
     * 使用SupportMapFragment加载地图
     */
    private void initmap(){
        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment)fm.findFragmentById(R.id.map_frag);
        mTencentMap = mapFragment.getMap();
        mTencentMap.setIndoorEnabled(true);
        mTencentMap.enableMultipleInfowindow(true);
        mTencentMap.setInfoWindowAdapter(mInfoWindowAdapter);
        mTencentMap.setOnIndoorStateChangeListener(mOnIndoorStateChangeListener);
    }

    private void initResource(){
        mRealFromBitmap =  DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.NAVI_LINE_FROM_MARKER);
        mRealFromBitmapGray = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.NAVI_LINE_FROM_MARKER_GRAY);
        mRealToBitmap = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.NAVI_LINE_TO_MARKER);
        mRealToBitmapGray = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.NAVI_LINE_TO_MARKER_GRAY);
        doorIn = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.DOOR_IN);
        doorInGray = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.DOOR_IN_GRAY);
        doorOut = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.DOOR_OUT);
        doorOutGray = DemoUtil.readAssetsImg(getApplicationContext(), NaviConfig.DOOR_OUT_GRAY);
    }

    /**
     * 请求路线规划
     */
    private void initRoute(){
        mWalkNaviManager = new WalkNaviManager(this);
        //构造起点
        fromPoi = new NaviPoi();
        fromPoi.setLatitude(mRealFromLatitude);
        fromPoi.setLongitude(mRealFromLongitude);
        fromPoi.setBuildingId(mRealFromBuildingId);
        fromPoi.setFloorName(mRealFromFloorName);
        //构造终点
        targetPoi = new NaviPoi();
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
            Log.e("route","error:"+ errorCode);
        }

        @Override
        public void onRouteSearchSuccess(ArrayList<RouteData> routes) {
            if (routes == null || routes.size() == 0) {
                return;
            }

            //选择一条路线，虽然目前检索只返回一条
            if (routes != null && routes.size() > 0){
                mRouteData = routes.get(0);
                drawFromAndToMarker();
                drawRoute(mRouteData);
                drawRouteMarkers(mRouteData);
                zoomMapToSpan(mRouteData);
                changeMarkerState();
            }
        }
    };

    /**
     * 绘制真实起终点marker
     */
    private void drawFromAndToMarker(){
        //添加起点marker
        MarkerOptions fromMarkerOptions = new MarkerOptions(new LatLng(mRealFromLatitude, mRealFromLongitude));
        fromMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(mRealFromBitmap))
                .anchor(0.5f, 0.5f)
                .level(OverlayLevel.OverlayLevelAboveLabels)
                .title(mRealFromFloorName)
                .zIndex(MARKER_FROM_TO_ZINDEX);
        fromMarker = mTencentMap.addMarker(fromMarkerOptions);
        fromMarker.setClickable(false);
        fromMarker.hideInfoWindow();

        //添加终点marker
        MarkerOptions targetMarkerOptions = new MarkerOptions(new LatLng(mRealToLatitude, mRealToLongitude));
        targetMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(mRealToBitmap))
                .anchor(0.5f, 0.5f)
                .level(OverlayLevel.OverlayLevelAboveLabels)
                .title(mRealToFloorName)
                .zIndex(MARKER_FROM_TO_ZINDEX);
        targetMarker = mTencentMap.addMarker(targetMarkerOptions);
        targetMarker.setClickable(false);
        targetMarker.hideInfoWindow();
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
                    .width(NaviConfig.ROUTE_LINE_WIDTH)
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
            Polyline polyline = mTencentMap.addPolyline(polylineOptions);
            mRoutePolylines.add(polyline);
        }
        if (fromMarker != null && !fromMarker.getPosition().equals(points.get(0))) {
            ArrayList<LatLng> latlngs = new ArrayList<>();
            latlngs.add(fromMarker.getPosition());
            latlngs.add(points.get(0));
            Polyline polyline = mTencentMap.addPolyline(new PolylineOptions()
                    .color(Color.BLACK)
                    .pattern(NaviConfig.ROUTE_DOOR_LINE_PATTERN)
                    .latLngs(latlngs)
                    .lineCap(false));
            mRoutePolylines.add(polyline);
        }
        if (targetMarker != null && !targetMarker.getPosition().equals(points.get(points.size() - 1))) {
            ArrayList<LatLng> latlngs = new ArrayList<>();
            latlngs.add(targetMarker.getPosition());
            latlngs.add(points.get(points.size() - 1));
            Polyline polyline = mTencentMap.addPolyline(new PolylineOptions()
                    .color(Color.BLACK)
                    .pattern(NaviConfig.ROUTE_DOOR_LINE_PATTERN)
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
                Marker startMarker = mTencentMap.addMarker(startMarkerOption);
                startMarker.showInfoWindow();
                Marker targetMarker = mTencentMap.addMarker(targetMarkerOption);
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

                Marker marker = mTencentMap.addMarker(markerOptions);
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

    private void zoomMapToSpan(RouteData routeData) {
        if (routeData == null) {
            return;
        }

        int marginLeft = getResources().getDimensionPixelSize(R.dimen.navigation_line_margin_left);
        int marginTop = getResources().getDimensionPixelSize(R.dimen.navigation_line_margin_top);
        int marginRight = getResources().getDimensionPixelSize(R.dimen.navigation_line_margin_right);
        int marginBottom = getResources().getDimensionPixelSize(R.dimen.navigation_line_margin_bottom);

        mTencentMap.setIndoorFloor(mRealFromBuildingId, mRealFromFloorName);
        mTencentMap.moveCamera(CameraUpdateFactory.newLatLngBoundsRect(new LatLngBounds.Builder()
                        .include(routeData.getRoutePoints()).include(fromMarker.getPosition()).include(targetMarker.getPosition()).build(),
                marginLeft, marginRight, marginTop, marginBottom));
    }

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
            curFloorName = indoorBuilding.getLevels().get(activeLevelIndex).getName();

            changeMarkerState();

            return true;
        }

        @Override
        public boolean onIndoorBuildingDeactivated() {
            curIndoorBuildingId = null;
            curFloorName = null;

            changeMarkerState();

            return true;
        }
    };

    @Override
    protected void onDestroy() {
        mTencentMap = null;
        mWalkNaviManager = null;
        super.onDestroy();
    }
}
