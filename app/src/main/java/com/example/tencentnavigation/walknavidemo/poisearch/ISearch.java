package com.example.tencentnavigation.walknavidemo.poisearch;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

import org.json.JSONObject;

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
public interface ISearch {
    void setSearchPosition(LatLng latLng);

    void setIndoorInfo(String buildingId, String floor);

    JsonObjectRequest getRequest(Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener);

    List<PoiInfo> parseJson(JSONObject jsonObject);
}
