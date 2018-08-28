package com.stg.makeathon.agrohelper.domain;

public class CheckupData {
    private String appId, objType, disease, infectedArea, remedy, imageUri;

    public CheckupData() {
    }

    public CheckupData(String appId, String objType, String disease, String infectedArea, String remedy, String imageUri) {
        this.appId = appId;
        this.objType = objType;
        this.disease = disease;
        this.infectedArea = infectedArea;
        this.remedy = remedy;
        this.imageUri = imageUri;
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
}
