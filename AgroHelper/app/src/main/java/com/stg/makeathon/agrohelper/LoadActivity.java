package com.stg.makeathon.agrohelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.stg.makeathon.agrohelper.config.AppConstants;
import com.stg.makeathon.agrohelper.config.AppData;
import com.stg.makeathon.agrohelper.config.SavedContents;
import com.stg.makeathon.agrohelper.domain.CheckupData;
import com.stg.makeathon.agrohelper.domain.Disease;
import com.stg.makeathon.agrohelper.service.CheckupHistory;
import com.stg.makeathon.agrohelper.service.DiseaseDataLoader;
import com.stg.makeathon.agrohelper.service.FileOperation;
import com.stg.makeathon.agrohelper.service.InitialDataSetup;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LoadActivity extends AppCompatActivity {

    private boolean isHistoryLoaded = false, isDiseaseLoaded = false, hasError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        checkAndCreateAppId();
        // Fetching History Data
        try {
            AppData.getInstance().getCheckupDataList().clear();
            CheckupHistory historyService = new CheckupHistory();
            historyService.getCheckupHistory(new CheckupHistory.OnServiceCompleteListener() {
                @Override
                public void onSuccess(List<CheckupData> checkupDataList) {
                    if (checkupDataList == null)
                        Log.e("Checkup History", "Null value in checkupDataList");
                    else {
                        Log.i("Checkup History", checkupDataList.size() + " records found.");
                        if (checkupDataList.size() > 0) {
                            AppData.getInstance().setCheckupDataList(checkupDataList);
                        }
                    }
                    isHistoryLoaded = true;
                    moveToMainActivity();
                }

                @Override
                public void onError(String errorMsg) {
                    Log.e("Checkup History", errorMsg);
                    isHistoryLoaded = true;
                    moveToMainActivity();
                }
            });

            // Initial Data Setup
            /*new Thread(new Runnable() {
                @Override
                public void run() {
                    InitialDataSetup.saveInitialData();
                    Log.i("InitialSetup", "Data Setup Completed.");
                }
            }).start();*/

            // Load Disease data
            DiseaseDataLoader diseaseDataLoader = new DiseaseDataLoader();
            diseaseDataLoader.loadDiseaseData(new DiseaseDataLoader.OnServiceCompleteListener() {
                @Override
                public void onSuccess(Map<Integer, Disease> diseaseData) {
                    if (diseaseData != null) {
                        AppData.getInstance().setAllDisease(diseaseData);
                        isDiseaseLoaded = true;
                        Log.i("DiseaseData", "Disease Data: " + AppData.getInstance().getAllDisease());
                        moveToMainActivity();
                    } else {
                        showErrorDialog("Error", "Error in loading data.");
                    }
                }

                @Override
                public void onError(String errorMsg) {
                    showErrorDialog("Error", "Error in loading data.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Error", "Error in loading data.");
        }
    }

    private void moveToMainActivity() {
        if (isDiseaseLoaded && isHistoryLoaded && !hasError) {
            Intent i = new Intent(this, HomeActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void showErrorDialog(String title, String message) {
        hasError = true;
        AlertDialog.Builder errorDialogBuilder = new AlertDialog.Builder(this);
        errorDialogBuilder.setTitle(title);
        errorDialogBuilder.setMessage(message);
        errorDialogBuilder.setCancelable(true);
        errorDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        errorDialogBuilder.create().show();
    }

    private void checkAndCreateAppId() {
        try {
            FileOperation.getDataFromFile(this, AppConstants.SAVED_CONFIG_FILE_NAME, new FileOperation.FileOperationCallback() {
                @Override
                public void onSuccess(Object response) {
                    if (response != null && response instanceof SavedContents)
                        AppData.getInstance().setSavedContents((SavedContents) response);
                    String appId = AppData.getInstance().getSavedContents().getAppId();
                    Log.i("getAppData", "Retrieved AppId: " + appId);
                    if (appId == null || appId.trim().length() == 0)
                        generateAppId();
                }

                @Override
                public void onError(String errMsg) {
                    generateAppId();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Error", "Unexpected Error Occurred.");
        }
    }

    private void generateAppId() {
        final String appId = UUID.randomUUID().toString();
        if (AppData.getInstance().getSavedContents() == null)
            AppData.getInstance().setSavedContents(new SavedContents());
        AppData.getInstance().getSavedContents().setAppId(appId);
        Log.i("generateAppId", "Generated AppId: " + appId);
        FileOperation.saveDataToFile(this, AppConstants.SAVED_CONFIG_FILE_NAME, AppData.getInstance().getSavedContents(), new FileOperation.FileOperationCallback() {
            @Override
            public void onSuccess(Object response) {
                Log.i("Save AppId", "Generated and Saved App Id: " + appId);
            }

            @Override
            public void onError(String errMsg) {
                Log.e("Save AppId", "Error in saving AppId: " + errMsg);
                showErrorDialog("Error", "Unexpected Error Occurred.");
            }
        });
    }
}
