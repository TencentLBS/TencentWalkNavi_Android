package com.example.tencentnavigation.walknavidemo.poisearch;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * All rights Reserved, Designed By lbs.qq.com
 * <p>
 * 同时维护一个 roundSearch 和 rgc 请求
 *
 * @version V1.0
 * @Description:
 * @author: wangxiaokun
 * @date: 2018/4/19
 * @Copyright: 2018 tencent Inc. All rights reserved.
 */
public class PoiSearch {

    private static final int SEARCH_TYPE_RGC = 0;
    private static final int SEARCH_TYPE_ROUND = 1;

    private PoiSearchManager mPoiSearchManager;
    private RoundSearch mRoundSearch;
    private RgcSearch mRgcSearch;
    private List<PoiInfo> mPoiInfos;
    private volatile int mCallbackCount;

    private PoiSearchCallback mPoiSearchCallback;

    public interface PoiSearchCallback {
        void onPoiSearchFinished(List<PoiInfo> poiInfos);
    }

    public PoiSearch(Context context, PoiSearchCallback poiSearchCallback) {
        mPoiSearchManager = PoiSearchManager.getInstance(context);
        mPoiSearchCallback = poiSearchCallback;

        mRoundSearch = new RoundSearch(context);
        mRgcSearch = new RgcSearch(context);
        mPoiInfos = new LinkedList<>();
    }

    public void setPosition(LatLng latLng) {
        if (latLng == null) {
            return;
        }
        mRoundSearch.setSearchPosition(latLng);
        mRgcSearch.setSearchPosition(latLng);
    }

    public void setPage(int pageIndex) {
        mRoundSearch.setPage(pageIndex);
    }

    /**
     * 设置室内信息
     *
     * @param buildingId
     * @param floor
     */
    public void setIndoorInfo(String buildingId, String floor) {
        mRoundSearch.setIndoorInfo(buildingId, floor);
        mRgcSearch.setIndoorInfo(buildingId, floor);
    }

    /**
     * 去除室内信息
     */
    public void removeIndoorInfo() {
        mRoundSearch.removeIndoorInfo();
        mRgcSearch.removeIndoorInfo();
    }

    /**
     * rgc 检索
     *
     * @return
     */
    private Request getRgcRequest() {
        return mRgcSearch.getRequest(
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if (jsonObject == null) {
                            getResult(SEARCH_TYPE_RGC, null);
                            return;
                        }
                        getResult(SEARCH_TYPE_RGC, mRgcSearch.parseJson(jsonObject));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        getError(volleyError);
                    }
                });
    }

    /**
     * 周边检索
     *
     * @return
     */
    private Request getRoundRequest() {
        return mRoundSearch.getRequest(
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if (jsonObject == null) {
                            getResult(SEARCH_TYPE_ROUND, null);
                            return;
                        }
                        getResult(SEARCH_TYPE_ROUND, mRoundSearch.parseJson(jsonObject));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        getError(volleyError);
                    }
                });
    }

    public void start() {
        mCallbackCount = 2;
        mPoiSearchManager.addNewRequest(getRgcRequest());
        mPoiSearchManager.addNewRequest(getRoundRequest());
        if (mPoiInfos == null) {
            mPoiInfos = new LinkedList<>();
        } else {
            mPoiInfos.clear();
        }
    }

    private synchronized void getResult(int searchType, List<PoiInfo> infos) {
        mCallbackCount--;
        if (searchType == SEARCH_TYPE_RGC) {
            if (infos != null && infos.size() > 0) {
                mPoiInfos.add(0, infos.get(0));
            }
        }
        if (searchType == SEARCH_TYPE_ROUND) {
            if (infos != null) {
                mPoiInfos.addAll(infos);
            }
        }
        if (mCallbackCount > 0) {
            return;
        }
        if (mPoiSearchCallback != null) {
            mPoiSearchCallback.onPoiSearchFinished(mPoiInfos);
        }
    }

    private void getError(VolleyError volleyError) {
        synchronized (this) {
            if (--mCallbackCount > 0) {
                return;
            }
            mPoiSearchCallback.onPoiSearchFinished(mPoiInfos);
        }
    }
}
