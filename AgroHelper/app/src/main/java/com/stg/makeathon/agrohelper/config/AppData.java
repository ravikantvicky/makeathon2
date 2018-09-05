package com.stg.makeathon.agrohelper.config;

import com.stg.makeathon.agrohelper.domain.CheckupData;
import com.stg.makeathon.agrohelper.domain.Disease;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppData {
    private static final AppData ourInstance = new AppData();

    public static AppData getInstance() {
        return ourInstance;
    }

    private SavedContents savedContents;
    private List<CheckupData> checkupDataList = new ArrayList<>();
    private CheckupData selectedRecord;
    private Map<Integer, Disease> allDisease = new HashMap<>();
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

    public Map<Integer, Disease> getAllDisease() {
        return allDisease;
    }

    public void setAllDisease(Map<Integer, Disease> allDisease) {
        this.allDisease = allDisease;
    }
}
