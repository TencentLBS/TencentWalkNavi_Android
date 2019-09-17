package com.example.tencentnavigation.walknavidemo.util;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * All rights Reserved, Designed By lbs.qq.com
 *
 * @version V5.0.0
 * @Description:常量管理
 * @author: daxiazhang
 * @date: 2018/3/7
 * @Copyright: 2018 tencent Inc. All rights reserved.
 */

public class NaviConfig {

    public static final String NAVI_LINE_FROM_MARKER = "route_ic_marker_start.png";
    public static final String NAVI_LINE_TO_MARKER = "route_ic_marker_end.png";
    public static final String NAVI_LINE_FROM_MARKER_GRAY = "route_ic_marker_start_gray.png";
    public static final String NAVI_LINE_TO_MARKER_GRAY = "route_ic_marker_end_gray.png";
    public static final String ARROW_TEXTURE = "color_arrow_texture.png";

    //门和电梯icon
    public static final String DOOR_IN = "ic_door_in.png";
    public static final String DOOR_IN_GRAY = "ic_door_in_gray.png";
    public static final String DOOR_OUT = "ic_door_out.png";
    public static final String DOOR_OUT_GRAY = "ic_door_out_gray.png";
    public static final String ELEVATOR_DOWN = "ic_elevator_down.png";
    public static final String ELEVATOR_UP = "ic_elevator_up.png";
    public static final String ESCALATOR_DOWN = "ic_escalator_down.png";
    public static final String ESCALATOR_UP = "ic_escalator_up.png";
    public static final String STAIR_DOWN = "ic_stair_down.png";
    public static final String STAIR_UP = "ic_stair_up.png";

    public static final int ROUTE_LINE_COLOR = Color.argb(255, 59, 105, 239);
    public static final int ROUTE_LINE_COLOR_GRAY = Color.argb(255, 133, 133, 133);
    public static final int ROUTE_LINE_COLOR_WHITE = Color.argb(255, 255, 255, 255);
    public static final int ROUTE_LINE_WIDTH = 20;
    public static final List<Integer> ROUTE_DOOR_LINE_PATTERN = new ArrayList<>();
    {
        //init DoorLine Pattern
        ROUTE_DOOR_LINE_PATTERN.add(10);
        ROUTE_DOOR_LINE_PATTERN.add(20);
    }

    /**
     * 真实起点和终点的Z轴层级
     */
    public static final int MARKER_FROM_TO_ZINDEX = 8;
    /**
     * 导航路线上添加的marker的Z轴层级，包括路线规划起终点、门、电梯
     */
    public static final int MARKER_DYNAMIC_ZINDEX = 7;
}
