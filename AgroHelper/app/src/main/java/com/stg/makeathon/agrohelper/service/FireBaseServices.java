package com.stg.makeathon.agrohelper.service;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FireBaseServices {
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private OnCompletion onCompleteListener;

    public void processImage(final Context context, final Uri imageUri, final String latitude, final String longitude, final OnCompletion onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
        try {
            mStorageRef = FirebaseStorage.getInstance().getReference();
            final long time = System.currentTimeMillis();
            String imageFileName = "/images/image-" + time + ".jpg";
            final StorageReference riversRef = mStorageRef.child(imageFileName);
            riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final Uri downloadUrl = uri;
                            Log.i("processImage", "Image Uploaded with URL: " + downloadUrl.toString());
                            // Creating Thumbnail
                            final String thumbFileName = "/images/thumb-" + time + ".png";
                            try {
                                Bitmap thumbImage = ThumbnailUtils.extractThumbnail(MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri), AppConstants.THUMBNAIL_IMG_SIZE, AppConstants.THUMBNAIL_IMG_SIZE);
                                final StorageReference riversRefThumb = mStorageRef.child(thumbFileName);
                                riversRefThumb.putFile(getImageUri(context, thumbImage)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        riversRefThumb.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                CheckupData data = new CheckupData();
                                                data.setAppId(AppData.getInstance().getSavedContents().getAppId());
                                                data.setObjType("Normal");
                                                data.setDisease("NA");
                                                data.setInfectedArea("0%");
                                                data.setRemedy("Test");
                                                data.setImageUri(downloadUrl.toString());
                                                data.setThumbUri(uri.toString());
                                                data.setLatitude(latitude);
                                                data.setLongitude(longitude);

                                                // Applying ML
                                                try {
                                                    Bitmap mlImg = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                                                    FireBaseMLService.startMLService(mlImg, data, new FireBaseMLService.OnCompleteCallback() {
                                                        @Override
                                                        public void onSuccess(CheckupData data) {
                                                            saveCheckupData(data);
                                                            // onCompleteListener.onComplete(data);
                                                        }

                                                        @Override
                                                        public void onFailure(String error) {
                                                            onCompleteListener.onComplete(null);
                                                        }
                                                    });
                                                    /*Bitmap scalledBM = Bitmap.createScaledBitmap(mlImg, AppConstants.ML_KIT_DIM_IMG_SIZE_X, AppConstants.ML_KIT_DIM_IMG_SIZE_Y,
                                                            true);
                                                    ImageClassifier imageClassifier = new ImageClassifier((Activity) context);
                                                    String val = imageClassifier.classifyFrame(scalledBM);
                                                    Log.i("ML Data", "Return Val: " + val);*/
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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

    private void saveCheckupData(final CheckupData data) {
        db = FirebaseFirestore.getInstance();
        db.collection(AppConstants.FB_CHECKUP_COLLECTION_NAME)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i("saveData", "Data Saved to Firestore");
                        if (onCompleteListener != null)
                            onCompleteListener.onComplete(data);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("saveData", "Error in saving data to firestore: " + e.getMessage());
                        e.printStackTrace();
                        if (onCompleteListener != null)
                            onCompleteListener.onComplete(null);
                    }
                });
    }

    public interface OnCompletion {
        void onComplete(CheckupData data);
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
