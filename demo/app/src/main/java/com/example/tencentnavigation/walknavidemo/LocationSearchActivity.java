package com.example.tencentnavigation.walknavidemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.tencentnavigation.walknavidemo.poisearch.PoiInfo;
import com.example.tencentnavigation.walknavidemo.poisearch.PoiSuggestionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 地点搜索
 * 输入提示
 */
public class LocationSearchActivity extends AppCompatActivity {

    private static final String TAG = "integrated";

    private static final int REFRESH_UI = 1;

    private ResultAdapter resultAdapter;

    private ArrayList<PoiInfo> mPoiInfos = new ArrayList<>();

    private String mBuildingId;
    private String mFloorName;
    private String mCity;
    private String mCityCode;

    /**
     * 更新搜索结果列表
     */
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case REFRESH_UI:
                    resultAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_location_search);

        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        mBuildingId = intent.getStringExtra("buildingId");
        mFloorName = intent.getStringExtra("floorName");
        mCity = intent.getStringExtra("city");
        mCityCode = intent.getStringExtra("cityCode");

        View back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        //搜索框
        SearchView searchView = findViewById(R.id.searchView);
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(mOnQueryTextListener);

        ListView listView = findViewById(R.id.listView);
        resultAdapter = new ResultAdapter(this, R.id.listView, mPoiInfos);
        listView.setAdapter(resultAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PoiInfo data = resultAdapter.getItem(position);
                if (data != null) {
                    Intent intent = new Intent();
                    intent.putExtra("uid", data.uid);
                    intent.putExtra("title", data.title);
                    intent.putExtra("address", data.address);
                    intent.putExtra("floorName", data.floorName);
                    intent.putExtra("buildingId", data.buildingId);
                    intent.putExtra("latitude", data.position.latitude);
                    intent.putExtra("longitude", data.position.longitude);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }


    //搜索框监听
    SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            getResult(newText, mBuildingId, mCity);
            return false;
        }
    };

    /**
     * 获取poi信息，解析
     */
    private void getResult(String query, String buildingId, String region) {
        Log.e(TAG, "sug query:" + query + ", buildingId: " + buildingId + ", region: " + region);
        PoiSuggestionManager poiSuggestionManager = new PoiSuggestionManager(this);
        poiSuggestionManager.setPoiSuggestionCallback(new PoiSuggestionManager.PoiSuggestionCallback() {
            @Override
            public void onPoiSuggestionFinished(List<PoiInfo> poiInfos) {
                mPoiInfos.clear();
                if (poiInfos != null) {
                    mPoiInfos.addAll(poiInfos);
                }
                handler.sendEmptyMessage(REFRESH_UI);
            }
        });
        poiSuggestionManager.suggestion(query, buildingId, region);
    }

    /**
     * 定义适配器
     */
    private class ResultAdapter extends ArrayAdapter<PoiInfo> {

        public ResultAdapter(Context context, int resourceId, ArrayList<PoiInfo> poiInfos) {
            super(context, resourceId, poiInfos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PoiInfo item = getItem(position);
            if (item == null) {
                return convertView;
            }
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_item_search, null);
            }
            TextView title = convertView.findViewById(R.id.search_item_name);
            TextView address = convertView.findViewById(R.id.search_item_address);
            title.setText(item.title);
            address.setText(item.address);
            return convertView;
        }
    }

}
