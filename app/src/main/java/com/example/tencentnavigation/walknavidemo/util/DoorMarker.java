package com.example.tencentnavigation.walknavidemo.util;

import com.tencent.tencentmap.mapsdk.maps.model.Marker;

public class DoorMarker {
    private Marker marker;
    private String buildingId;
    private String floorName;

    public void setMarker(Marker marker){
        this.marker = marker;
    }

    public Marker getMarker(){
        return marker;
    }

    public void setBuildingId(String buildingId){
        this.buildingId = buildingId;
    }

    public String getBuildingId(){
        return buildingId;
    }

    public void setFloorName(String floorName){
        this.floorName = floorName;
    }

    public String getFloorName(){
        return floorName;
    }

}
