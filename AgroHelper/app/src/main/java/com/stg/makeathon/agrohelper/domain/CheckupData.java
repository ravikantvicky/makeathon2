package com.stg.makeathon.agrohelper.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckupData {
    private String appId, objType, disease, infectedArea, remedy, imageUri, thumbUri, latitude, longitude, updateTime;

    public CheckupData() {
        this.updateTime = getCurrentTime();
    }

    public CheckupData(String appId, String objType, String disease, String infectedArea, String remedy, String imageUri, String thumbUri, String latitude, String longitude) {
        this.appId = appId;
        this.objType = objType;
        this.disease = disease;
        this.infectedArea = infectedArea;
        this.remedy = remedy;
        this.imageUri = imageUri;
        this.thumbUri = thumbUri;
        this.latitude = latitude;
        this.longitude = longitude;
        this.updateTime = getCurrentTime();
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getObjType() {
        return objType;
    }

    public void setObjType(String objType) {
        this.objType = objType;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getInfectedArea() {
        return infectedArea;
    }

    public void setInfectedArea(String infectedArea) {
        this.infectedArea = infectedArea;
    }

    public String getRemedy() {
        return remedy;
    }

    public void setRemedy(String remedy) {
        this.remedy = remedy;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getThumbUri() {
        return thumbUri;
    }

    public void setThumbUri(String thumbUri) {
        this.thumbUri = thumbUri;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
