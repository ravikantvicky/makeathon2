package com.stg.makeathon.agrohelper.config;

import com.stg.makeathon.agrohelper.domain.CheckupData;

import java.util.ArrayList;
import java.util.List;

public class AppData {
    private static final AppData ourInstance = new AppData();

    public static AppData getInstance() {
        return ourInstance;
    }

    private SavedContents savedContents;
    private List<CheckupData> checkupDataList = new ArrayList<>();
    private CheckupData selectedRecord;

    private AppData() {
    }

    public SavedContents getSavedContents() {
        return savedContents;
    }

    public void setSavedContents(SavedContents savedContents) {
        this.savedContents = savedContents;
    }

    public List<CheckupData> getCheckupDataList() {
        return checkupDataList;
    }

    public void setCheckupDataList(List<CheckupData> checkupDataList) {
        this.checkupDataList = checkupDataList;
    }

    public CheckupData getSelectedRecord() {
        return selectedRecord;
    }

    public void setSelectedRecord(CheckupData selectedRecord) {
        this.selectedRecord = selectedRecord;
    }
}
