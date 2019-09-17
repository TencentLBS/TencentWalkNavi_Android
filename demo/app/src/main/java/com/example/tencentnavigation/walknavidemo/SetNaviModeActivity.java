package com.example.tencentnavigation.walknavidemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RadioGroup;

import com.tencent.map.navi.NaviMode;

public class SetNaviModeActivity extends BaseActivity {

    private RadioGroup naviMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        naviMode = findViewById(R.id.navi_mode);

        naviMode.setVisibility(View.VISIBLE);
        naviMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    //设置导航模式
                    case R.id.three_d_mode:
                        mWalkNaviView.setNaviMode(NaviMode.MODE_3DCAR_TOWARDS_UP);
                        break;
                    case R.id.two_d_mode:
                        mWalkNaviView.setNaviMode(NaviMode.MODE_2DMAP_TOWARDS_NORTH);
                        break;
                    case R.id.over_mode:
                        mWalkNaviView.setNaviMode(NaviMode.MODE_OVERVIEW);
                        break;
                    case R.id.over_mode_left:
                        mWalkNaviView.setNaviMode(NaviMode.MODE_REMAINING_OVERVIEW);
                        //设置导航路线显示区域距离屏幕四周的边距
                        mWalkNaviView.setVisibleRegionMargin(400,200,600,300);
                        break;
                }
            }
        });
    }


}
