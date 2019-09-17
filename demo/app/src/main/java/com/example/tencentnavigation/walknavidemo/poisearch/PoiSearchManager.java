package com.example.tencentnavigation.walknavidemo.poisearch;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * All rights Reserved, Designed By lbs.qq.com
 *
 * @version V1.0
 * @Description:
 * @author: wangxiaokun
 * @date: 2018/4/19
 * @Copyright: 2018 tencent Inc. All rights reserved.
 */
public class PoiSearchManager {

    private static volatile PoiSearchManager mInstance;

    private RequestQueue mRequestQueue;


    private PoiSearchManager(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static PoiSearchManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (PoiSearchManager.class) {
                if (mInstance == null) {
                    mInstance = new PoiSearchManager(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    public void addNewRequest(Request request) {
        mRequestQueue.add(request);
    }
}
