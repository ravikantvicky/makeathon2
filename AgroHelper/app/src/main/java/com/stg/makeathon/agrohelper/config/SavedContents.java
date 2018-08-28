package com.stg.makeathon.agrohelper.config;

import java.io.Serializable;

public class SavedContents implements Serializable {
    private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
