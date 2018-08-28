package com.stg.makeathon.agrohelper.service;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stg.makeathon.agrohelper.config.AppConstants;
import com.stg.makeathon.agrohelper.config.AppData;
import com.stg.makeathon.agrohelper.domain.CheckupData;

public class FireBaseServices {
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private OnCompletion onCompleteListener;

    public void processImage(Uri imageUri, OnCompletion onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
        try {
            mStorageRef = FirebaseStorage.getInstance().getReference();
            String imageFileName = "/images/image-" + System.currentTimeMillis() + ".jpg";
            final StorageReference riversRef = mStorageRef.child(imageFileName);
            riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            Log.i("processImage", "Image Uploaded with URL: " + downloadUrl.toString());
                            CheckupData data = new CheckupData();
                            data.setAppId(AppData.getInstance().getSavedContents().getAppId());
                            data.setObjType("Normal");
                            data.setDisease("NA");
                            data.setInfectedArea("0%");
                            data.setRemedy("Test");
                            data.setImageUri(downloadUrl.toString());
                            saveCheckupData(data);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Image Upload", "Error in Image Upload: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveCheckupData(CheckupData data) {
        db = FirebaseFirestore.getInstance();
        db.collection(AppConstants.FB_CHECKUP_COLLECTION_NAME)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i("saveData", "Data Saved to Firestore");
                        if (onCompleteListener != null)
                            onCompleteListener.onComplete();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("saveData", "Error in saving data to firestore: " + e.getMessage());
                        e.printStackTrace();
                        if (onCompleteListener != null)
                            onCompleteListener.onComplete();
                    }
                });
    }

    public interface OnCompletion {
        void onComplete();
    }
}
