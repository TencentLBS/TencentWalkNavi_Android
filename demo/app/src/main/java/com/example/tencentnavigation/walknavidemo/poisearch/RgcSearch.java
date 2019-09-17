package com.example.tencentnavigation.walknavidemo.poisearch;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.tencentnavigation.walknavidemo.util.MapSearchUtils;
import com.example.tencentnavigation.walknavidemo.util.StringUtils;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * All rights Reserved, Designed By lbs.qq.com
 *
 * @version V1.0
 * @Description:
 * @author: wangxiaokun
 * @date: 2018/4/19
 * @Copyright: 2018 tencent Inc. All rights reserved.
 */
public class RgcSearch implements ISearch {

    private static final String LOG_TAG = "RgcSearch";

    private static final String DOMAIN_WITHOUT_INDOOR = "http://apis.map.qq.com/ws/geocoder/v1/?location?";
    private static final String DOMAIN_WITH_INDOOR = "http://loc.map.qq.com/loc?";

    private String mReferer;
    private String mKey;

    private LatLng mPosition;
    private String mBuildingId;
    private String mFloor;

    public RgcSearch(Context context) {
        mReferer = context.getPackageName();
        mKey = MapSearchUtils.getMapKey(context);
    }

    private String getUrlWithoutIndoor() {
        StringBuilder sb = new StringBuilder();
        sb.append(DOMAIN_WITHOUT_INDOOR);
        sb.append("&key=" + mKey);
        sb.append("&referer=" + mReferer);
        sb.append("&get_poi=0");
        if (mPosition != null) {
            sb.append("&location=" + mPosition.latitude + "," + mPosition.longitude);
        }
        return sb.toString();
    }

    private String getUrlWithIndoor() {
        if (!isIndoor()) {
            return getUrlWithoutIndoor();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(DOMAIN_WITH_INDOOR);
        sb.append("key=" + mKey);
        sb.append("&referer=" + mReferer);
        sb.append("&scene_type=shinei_poi&shinei_poi_radius=10&npois=1");
        if (mPosition != null) {
            sb.append("&px=" + mPosition.longitude);
            sb.append("&py=" + mPosition.latitude);
        }
        sb.append("&poi_floor=" + mFloor);
        return sb.toString();
    }

    private boolean isIndoor() {
        return !(StringUtils.isEmpty(mBuildingId) || StringUtils.isEmpty(mFloor));
    }

    @Override
    public void setSearchPosition(LatLng latLng) {
        mPosition = latLng;
    }

    @Override
    public void setIndoorInfo(String buildingId, String floor) {
        mBuildingId = buildingId;
        mFloor = floor;
    }

    public void removeIndoorInfo() {
        mBuildingId = null;
        mFloor = null;
    }

    @Override
    public JsonObjectRequest getRequest(Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        Log.d(LOG_TAG, "url:" + getUrlWithIndoor());
        return new JsonObjectRequest(
                Request.Method.GET,
                getUrlWithIndoor(),
                null,
                responseListener,
                errorListener);
    }

    @Override
    public List<PoiInfo> parseJson(JSONObject jsonObject) {
        if (!isIndoor()) {
            return parseWithOutIndoor(jsonObject);
        } else {
            return parseWithIndoor(jsonObject);
        }
    }

    private List<PoiInfo> parseWithOutIndoor(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        int status = jsonObject.optInt("status", -1);
        if (status != 0) {
            Log.e(LOG_TAG,"rgc without indoor failed with status:" + status);
            return null;
        }
        JSONObject jsonResult = jsonObject.optJSONObject("result");
        if (jsonResult == null) {
            Log.e(LOG_TAG,"rgc without indoor failed with no result found");
            return null;
        }

        JSONObject jsonLocation = jsonResult.optJSONObject("location");
        if (jsonResult == null) {
            Log.e(LOG_TAG,"rgc without indoor failed with no location found");
            return null;
        }
        double latitude = jsonLocation.optDouble("lat", 100);
        if (latitude > 90 || latitude < -90) {
            Log.e(LOG_TAG,"rgc without indoor failed with invalid latitude:" + latitude);
            return null;
        }
        double longitude = jsonLocation.optDouble("lng", 200);
        if (longitude > 180 || longitude < -180) {
            Log.e(LOG_TAG,"rgc without indoor failed with invalid longitude:" + longitude);
            return null;
        }
        JSONObject jsonFormatedAddr = jsonResult.optJSONObject("formatted_addresses");
        if (jsonFormatedAddr == null) {
            Log.e(LOG_TAG,"rgc without indoor failed with no formatted_addresses found");
            return null;
        }
        String recommendAddr = jsonFormatedAddr.optString("recommend");
        if (StringUtils.isEmpty(recommendAddr)) {
            Log.e(LOG_TAG,"rgc without indoor failed with no recommend found");
            return null;
        }
        String address = jsonResult.optString("address");
        if (StringUtils.isEmpty(address)) {
            Log.e(LOG_TAG,"rgc without indoor failed with no address found");
            return null;
        }

        List<PoiInfo> poiInfos = new ArrayList<>();
        PoiInfo poiInfo = new PoiInfo();
        poiInfo.position = new LatLng(latitude, longitude);
        poiInfo.title = recommendAddr;
        poiInfo.address = address;
        poiInfos.add(poiInfo);

        return poiInfos;
    }

    private List<PoiInfo> parseWithIndoor(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        JSONObject jsonInfo = jsonObject.optJSONObject("info");
        if (jsonInfo == null) {
            return null;
        }
        int error = jsonInfo.optInt("error", -1);
        if (error != 0) {
            Log.e(LOG_TAG,"rgc without indoor failed with error:" + error);
            return null;
        }
        JSONObject jsonDetail = jsonObject.optJSONObject("detail");
        if (jsonDetail == null) {
            Log.e(LOG_TAG,"rgc without indoor failed with no detail found");
            return null;
        }
        JSONArray jsonResults = jsonDetail.optJSONArray("results");
        if (jsonResults == null || jsonResults.length() == 0) {
            return parsePoiList(jsonDetail);
        } else {
            return parseResults(jsonResults);
        }
    }

    private List<PoiInfo> parsePoiList(JSONObject detail) {
        JSONArray jsonPois = detail.optJSONArray("poilist");
        if (jsonPois == null || jsonPois.length() == 0) {
            Log.e(LOG_TAG,"rgc without indoor parsePoiList failed with no poilist found");
            return null;
        }

        JSONObject jsonPoi = jsonPois.optJSONObject(0);
        double latitude = jsonPoi.optDouble("pointy", 100);
        if (latitude > 90 || latitude < -90) {
            Log.e(LOG_TAG,"rgc without indoor parsePoiList failed with invalid latitude:" + latitude);
            return null;
        }
        double longitude = jsonPoi.optDouble("pointx", 200);
        if (longitude > 180 || longitude < -180) {
            Log.e(LOG_TAG,"rgc without indoor parsePoiList failed with invalid longitude:" + longitude);
            return null;
        }

        String name = jsonPoi.optString("name");
        if (StringUtils.isEmpty(name)) {
            Log.e(LOG_TAG,"rgc without indoor parsePoiList failed with no recommend found");
            return null;
        }
        String address = jsonPoi.optString("addr");
        if (StringUtils.isEmpty(address)) {
            Log.e(LOG_TAG,"rgc without indoor parsePoiList failed with no address found");
            return null;
        }

        List<PoiInfo> poiInfos = new ArrayList<>();
        PoiInfo poiInfo = new PoiInfo();
        poiInfo.position = new LatLng(latitude, longitude);
        poiInfo.title = name;
        poiInfo.address = address;
        poiInfos.add(poiInfo);

        return poiInfos;
    }

    private List<PoiInfo> parseResults(JSONArray results) {
        JSONObject jsonResult = results.optJSONObject(0);
        double latitude = jsonResult.optDouble("pointy", 100);
        if (latitude > 90 || latitude < -90) {
            Log.e(LOG_TAG,"rgc without indoor parseResults failed with invalid latitude:" + latitude);
            return null;
        }
        double longitude = jsonResult.optDouble("pointx", 200);
        if (longitude > 180 || longitude < -180) {
            Log.e(LOG_TAG,"rgc without indoor parseResults failed with invalid longitude:" + longitude);
            return null;
        }

        String name = jsonResult.optString("name");
        if (StringUtils.isEmpty(name)) {
            Log.e(LOG_TAG,"rgc without indoor parseResults failed with no recommend found");
            return null;
        }
        String address = jsonResult.optString("addr");
        if (StringUtils.isEmpty(address)) {
            Log.e(LOG_TAG,"rgc without indoor parseResults failed with no address found");
            return null;
        }

        List<PoiInfo> poiInfos = new ArrayList<>();
        PoiInfo poiInfo = new PoiInfo();
        poiInfo.position = new LatLng(latitude, longitude);
        poiInfo.title = name;
        poiInfo.address = address;
        poiInfos.add(poiInfo);

        return poiInfos;
    }
}
