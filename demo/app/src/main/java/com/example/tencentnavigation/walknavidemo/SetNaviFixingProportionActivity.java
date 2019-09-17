package com.example.tencentnavigation.walknavidemo;


import android.os.Bundle;
import android.support.annotation.Nullable;

public class SetNaviFixingProportionActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mWalkNaviView!=null){
            //设置导航过程中3D车头朝上模式下自车点位于地图宽高的比例，默认x坐标为0.5 ，y坐标为0.75。
            mWalkNaviView.setNaviFixingProportion3D(0.5f,0.5f);

            //设置导航过程中2D模式下，自车点位于地图宽高的比例，默认x坐标为0.5 ，y坐标为0.75
            mWalkNaviView.setNaviFixingProportion2D(0.5f,0.5f);
        }
    }
}
