package com.example.tencentnavigation.walknavidemo.poisearch;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.tencentnavigation.walknavidemo.util.MapSearchUtils;
import com.example.tencentnavigation.walknavidemo.util.StringUtils;
import com.google.gson.JsonObject;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
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
public class RoundSearch implements ISearch {

    private static final String LOG_TAG = "RoundSearch";

    private static String URL_FORMATER = "http://apis-tj.map.qq.com/ws/native/roundsearch/rpc/soso_map.LightMapService.RoundSearch?of=json&key=%s";

    private Context mContext;
    private JsonObject mJSONObject;
    private static String mReqUrl;
    private int mPoiCount = -1;

    public interface RoundSearchLoadCallback {
        void onRoundSearchLoaded();
    }

    public RoundSearch(Context context) {
        mContext = context.getApplicationContext();

        String key = MapSearchUtils.getMapKey(mContext);
        mReqUrl = String.format(URL_FORMATER, key);
        mJSONObject = new JsonObject();
        mJSONObject.addProperty("query", "*");
        mJSONObject.addProperty("radius", 500);
        mJSONObject.addProperty("num", 20);
        mJSONObject.addProperty("page", "0");
        //是否需求短地址
        mJSONObject.addProperty("ext_paras", "need_split_addr");
        mJSONObject.addProperty("user_id", MapSearchUtils.getIMei(mContext));
        mJSONObject.addProperty("service_id", 632);
        mJSONObject.addProperty("key", key);
        mJSONObject.addProperty("scene_type", 1);
    }

    @Override
    public void setSearchPosition(LatLng latLng) {
        if (latLng == null) {
            return;
        }
        mJSONObject.addProperty("point_x", latLng.longitude);
        mJSONObject.addProperty("point_y", latLng.latitude);
    }

    public void setPage(int pageIndex) {
        mJSONObject.addProperty("page", pageIndex);
    }

    /**
     * 设置检索关键字
     *
     * @param query
     */
    public void setQuery(String query) {
        if (StringUtils.isEmpty(query)) {
            return;
        }
        mJSONObject.addProperty("query", query);
    }

    @Override
    public void setIndoorInfo(String buildingId, String floor) {
        JsonObject jsonIndoorInfo = new JsonObject();
        jsonIndoorInfo.addProperty("Bld_ID", buildingId);
        jsonIndoorInfo.addProperty("FL_Seq", floor);
        mJSONObject.add("shinei_info", jsonIndoorInfo);
    }

    public void removeIndoorInfo() {
        mJSONObject.remove("shinei_info");
    }

    @Override
    public JsonObjectRequest getRequest(Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        Log.d(LOG_TAG, "url:" + mReqUrl + "\nbody:" + mJSONObject.toString());
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(mJSONObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JsonObjectRequest(Request.Method.POST,
                mReqUrl,
                jsonObject,
                responseListener,
                errorListener);
    }

    /**
     * 解析周边检索结果
     *
     * @param jsonObject
     * @return
     */
    @Override
    public List<PoiInfo> parseJson(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        int error = jsonObject.optInt("err_code", -1);
        if (error != 0) {
            Log.e(LOG_TAG,"round search failed with err_code:" + error);
            return null;
        }

        mPoiCount = jsonObject.optInt("total_poi_num");
        JSONArray jsonPois = jsonObject.optJSONArray("poilist");
        if (jsonPois == null) {
            Log.e(LOG_TAG,"round search failed with poi list get null, response data:" + jsonObject.toString());
            return null;
        }

        List<PoiInfo> pois = new LinkedList<>();
        for (int i = 0; i < jsonPois.length(); i++) {
            JSONObject jsonPoi = jsonPois.optJSONObject(i);
            if (jsonPoi == null) {
                break;
            }
            double latitude = jsonPoi.optDouble("latitude", 100);
            if (latitude > 90 || latitude < -90) {
                continue;
            }
            double longitude = jsonPoi.optDouble("longitude", 200);
            if (longitude > 180 || longitude < -180) {
                continue;
            }
            PoiInfo poiInfo = new PoiInfo();
            poiInfo.title = jsonPoi.optString("name");
            if (StringUtils.isEmpty(poiInfo.title)) {
                continue;
            }
            poiInfo.address = jsonPoi.optString("addr");
            if (StringUtils.isEmpty(poiInfo.address)) {
                continue;
            }
            poiInfo.position = new LatLng(latitude, longitude);

            //室内信息，楼层和buildingId获取
            getIndoorInfo(poiInfo, jsonPoi);

            pois.add(poiInfo);
        }

        return pois;
    }

    private void getIndoorInfo(PoiInfo poiInfo, JSONObject jsonPoi) {
        if (jsonPoi == null || poiInfo == null) {
            return;
        }
        JSONObject jsonReserve = jsonPoi.optJSONObject("reserve");
        if (jsonReserve == null) {
            return;
        }
        JSONObject jsonIndoorInfo = jsonReserve.optJSONObject("shinei_info");
        if (jsonIndoorInfo == null) {
            return;
        }

        String buildingId = jsonIndoorInfo.optString("Bld_ID");
        if (!StringUtils.isEmpty(buildingId)) {
            poiInfo.buildingId = buildingId;
        }

        String floorName = jsonIndoorInfo.optString("FL_Name");
        if (!StringUtils.isEmpty(floorName)) {
            poiInfo.floorName = floorName;
        }
    }
}
