package com.stg.makeathon.agrohelper.service;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stg.makeathon.agrohelper.config.AppConstants;
import com.stg.makeathon.agrohelper.domain.Disease;

import java.util.HashMap;
import java.util.Map;

public class DiseaseDataLoader {
    public interface OnServiceCompleteListener {
        void onSuccess(Map<Integer, Disease> diseaseData);

        void onError(String errorMsg);
    }

    public void loadDiseaseData(final OnServiceCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(AppConstants.FB_DISEASE_COLLECTION_NAME)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<Integer, Disease> diseaseData = new HashMap<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("loadDiseaseData", document.getId() + " => " + document.getData());
                                Disease data = document.toObject(Disease.class);
                                if (data != null) {
                                    diseaseData.put(data.getId(), data);
                                }
                            }
                            listener.onSuccess(diseaseData);
                        } else {
                            Log.e("loadDiseaseData", "Error getting documents: ", task.getException());
                            listener.onError("Error in fetching data.");
                        }
                    }
                });
    }
}
