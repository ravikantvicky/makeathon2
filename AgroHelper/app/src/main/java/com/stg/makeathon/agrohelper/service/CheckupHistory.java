package com.stg.makeathon.agrohelper.service;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.stg.makeathon.agrohelper.config.AppConstants;
import com.stg.makeathon.agrohelper.config.AppData;
import com.stg.makeathon.agrohelper.domain.CheckupData;

import java.util.ArrayList;
import java.util.List;

public class CheckupHistory {
    public interface OnServiceCompleteListener {
        void onSuccess(List<CheckupData> checkupDataList);

        void onError(String errorMsg);
    }

    public void getCheckupHistory(final OnServiceCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstants.FB_CHECKUP_COLLECTION_NAME)
                .whereEqualTo(AppConstants.FB_CHECKUP_DATA_COL_APP_ID, AppData.getInstance().getSavedContents().getAppId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<CheckupData> checkupDataList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("GetData", document.getId() + " => " + document.getData());
                                CheckupData data = document.toObject(CheckupData.class);
                                checkupDataList.add(data);
                            }
                            listener.onSuccess(checkupDataList);
                        } else {
                            Log.e("GetData", "Error getting documents: ", task.getException());
                            listener.onError("Error in fetching data.");
                        }
                    }
                });
    }
}
