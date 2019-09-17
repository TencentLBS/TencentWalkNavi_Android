package com.example.tencentnavigation.walknavidemo.poisearch;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.tencentnavigation.walknavidemo.util.MapSearchUtils;
import com.example.tencentnavigation.walknavidemo.util.StringUtils;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

import org.json.JSONArray;
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
public class PoiSuggestionManager {

    private static final String TAG = "PoiSuggestionManager";

    private static final String DOMAIN_INDOOR = "https://apis.map.qq.com/ws/indoor/v1/suggestion?";

    public interface PoiSuggestionCallback {
        void onPoiSuggestionFinished(List<PoiInfo> poiInfos);
    }

    private Context mContext;
    private RequestQueue mRequestQueue;

    private PoiSuggestionCallback mPoiSuggestionCallback;

    public PoiSuggestionManager(Context context) {
        mContext = context.getApplicationContext();
        mRequestQueue = Volley.newRequestQueue(mContext);
    }

    public void setPoiSuggestionCallback(PoiSuggestionCallback poiSuggestionCallback) {
        mPoiSuggestionCallback = poiSuggestionCallback;
    }

    public void suggestion(String keyword, String buildingId, String region) {
        String key = MapSearchUtils.getMapKey(mContext);
        StringBuilder sb = new StringBuilder();
        sb.append(DOMAIN_INDOOR);
        sb.append("&key=RJWBZ-GXELX-M7V4F-7AASW-OR2TV-JIBS6");//.append(key);
        sb.append("&keyword=").append(keyword);
        sb.append("&building_id=").append(buildingId);
        sb.append("&region=").append(region);
        String url = sb.toString();
        Log.e(TAG,"poi suggestion url: " + url);
        mRequestQueue.add(new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.e(TAG, "suggestion response: " + jsonObject);
                        if (mPoiSuggestionCallback != null) {
                            List<PoiInfo> poiInfos = parseJson(jsonObject);
                            if (poiInfos != null && poiInfos.size() > 0) {
                                mPoiSuggestionCallback.onPoiSuggestionFinished(poiInfos);
                            } else {
                                mPoiSuggestionCallback.onPoiSuggestionFinished(null);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(TAG, "suggestion failed with: " + volleyError.getMessage());
                        if (mPoiSuggestionCallback != null) {
                            mPoiSuggestionCallback.onPoiSuggestionFinished(null);
                        }
                    }
                }));
    }

    private List<PoiInfo> parseJson(JSONObject jsonObject) {
        if (jsonObject == null) {
            Log.e(TAG,"poi suggestion json object is null!!!");
            return null;
        }

        int error = jsonObject.optInt("status", -1);
        String msg = jsonObject.optString("message");
        if (error != 0) {
            Log.e(TAG,"poi suggestion failed with err_code: " + error + ", message: " + msg);
            return null;
        }

        int count = jsonObject.optInt("count");
        Log.d(TAG,"poi suggestion count: " + count);

        JSONArray jsonPois = jsonObject.optJSONArray("data");
        if (jsonPois == null) {
            Log.e(TAG,"poi suggestion failed with poi list get null, response data:" + jsonObject.toString());
            return null;
        }

        List<PoiInfo> pois = new LinkedList<>();
        for (int i = 0; i < jsonPois.length(); i++) {
            JSONObject jsonPoi = jsonPois.optJSONObject(i);
            if (jsonPoi == null) {
                continue;
            }

            JSONObject jsonLocation = jsonPoi.optJSONObject("location");
            if (jsonLocation == null) {
                continue;
            }

            double latitude = jsonLocation.optDouble("lat", 100);
            if (latitude > 90 || latitude < -90) {
                continue;
            }
            double longitude = jsonLocation.optDouble("lng", 200);
            if (longitude > 180 || longitude < -180) {
                continue;
            }

            PoiInfo poiInfo = new PoiInfo();
            poiInfo.uid = jsonPoi.optString("id");
            poiInfo.title = jsonPoi.optString("title");
            if (StringUtils.isEmpty(poiInfo.title)) {
                continue;
            }
            poiInfo.address = jsonPoi.optString("address");
            if (StringUtils.isEmpty(poiInfo.address)) {
                continue;
            }
            poiInfo.position = new LatLng(latitude, longitude);

            String buildingId = jsonPoi.optString("building_id");
            if (!StringUtils.isEmpty(buildingId)) {
                poiInfo.buildingId = buildingId;
            }

            String floorName = jsonPoi.optString("floor_name");
            if (!StringUtils.isEmpty(floorName)) {
                poiInfo.floorName = floorName;
            }

            pois.add(poiInfo);
        }

        return pois;
    }
}
