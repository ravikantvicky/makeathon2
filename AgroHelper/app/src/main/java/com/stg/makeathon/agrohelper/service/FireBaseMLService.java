package com.stg.makeathon.agrohelper.service;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.ml.custom.model.FirebaseCloudModelSource;
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource;
import com.google.firebase.ml.custom.model.FirebaseModelDownloadConditions;
import com.stg.makeathon.agrohelper.config.AppConstants;
import com.stg.makeathon.agrohelper.config.AppData;
import com.stg.makeathon.agrohelper.domain.CheckupData;
import com.stg.makeathon.agrohelper.domain.Disease;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

public class FireBaseMLService {
    public interface OnCompleteCallback {
        void onSuccess(CheckupData data);

        void onFailure(String error);
    }
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private static final int[] intValues = new int[AppConstants.ML_KIT_DIM_IMG_SIZE_X * AppConstants.ML_KIT_DIM_IMG_SIZE_Y];

    public static void startMLService(Bitmap img, final CheckupData data, final OnCompleteCallback callback) {
        try {

            FirebaseModelDownloadConditions.Builder conditionsBuilder =
                    new FirebaseModelDownloadConditions.Builder().requireWifi();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Enable advanced conditions on Android Nougat and newer.
                conditionsBuilder = conditionsBuilder
                        .requireCharging()
                        .requireDeviceIdle();
            }
            FirebaseModelDownloadConditions conditions = conditionsBuilder.build();

            FirebaseCloudModelSource cloudSource = new FirebaseCloudModelSource.Builder(AppConstants.ML_KIT_TF_MODEL_NAME)
                    .enableModelUpdates(true)
                    .setInitialDownloadConditions(conditions)
                    .setUpdatesDownloadConditions(conditions)
                    .build();
            FirebaseModelManager.getInstance().registerCloudModelSource(cloudSource);

            FirebaseLocalModelSource localSource = new FirebaseLocalModelSource.Builder(AppConstants.ML_KIT_TF_MODEL_NAME)
                    .setAssetFilePath(AppConstants.ML_KIT_TF_FILE_NAME)  // Or setFilePath if you downloaded from your host
                    .build();
            FirebaseModelManager.getInstance().registerLocalModelSource(localSource);

            FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                    .setCloudModelName(AppConstants.ML_KIT_TF_MODEL_NAME)
                    .setLocalModelName(AppConstants.ML_KIT_TF_MODEL_NAME)
                    .build();
            FirebaseModelInterpreter firebaseInterpreter =
                    FirebaseModelInterpreter.getInstance(options);

            FirebaseModelInputOutputOptions inputOutputOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{AppConstants.ML_KIT_DIM_BATCH_SIZE,
                                    AppConstants.ML_KIT_DIM_IMG_SIZE_X, AppConstants.ML_KIT_DIM_IMG_SIZE_Y,
                                    AppConstants.ML_KIT_DIM_PIXEL_SIZE})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{AppConstants.ML_KIT_DIM_BATCH_SIZE, AppConstants.ML_KIT_OUTPUT_SIZE})
                            .build();

            ByteBuffer input = convertBitmapToByteBuffer(img);
            FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                    .add(input)  // add() as many input arrays as your model requires
                    .build();
            Task<FirebaseModelOutputs> result =
                    firebaseInterpreter.run(inputs, inputOutputOptions)
                            .addOnSuccessListener(
                                    new OnSuccessListener<FirebaseModelOutputs>() {
                                        @Override
                                        public void onSuccess(FirebaseModelOutputs result) {
                                            float[][] output = result.<float[][]>getOutput(0);
                                            float[] probabilities = output[0];
                                            StringBuffer sb = new StringBuffer("");

                                            float max = probabilities[0];
                                            int maxIndex = 0;
                                            for (int i=0;i<probabilities.length;i++) {
                                                float f = probabilities[i];
                                                sb.append(f+" ");
                                                if (max < f) {
                                                    max = f;
                                                    maxIndex = i;
                                                }
                                            }
                                            Log.i("ML Success", "result: " + sb);
                                            Log.i("ML Success", "Max Element Index: " + maxIndex);
                                            Integer disId = maxIndex+1;
                                            /*if (AppConstants.SELECTED_IMG == null)
                                                disId = 5;
                                            else if (AppConstants.SELECTED_IMG.endsWith("Apple_Black_rot.jpg"))
                                                disId = 2;
                                            else if (AppConstants.SELECTED_IMG.endsWith("Apple_Scab_3.jpg"))
                                                disId = 1;
                                            else if (AppConstants.SELECTED_IMG.endsWith("Apple_scab.jpg"))
                                                disId = 1;
                                            else if (AppConstants.SELECTED_IMG.endsWith("Apple_Scab1.jpg"))
                                                disId = 1;
                                            else
                                                disId = 5;*/
                                            Map<Integer, Disease> allDisease = AppData.getInstance().getAllDisease();
                                            if (allDisease != null && allDisease.containsKey(disId)) {
                                                Disease selectedDis = allDisease.get(disId);
                                                data.setDiseaseId(disId);
                                                data.setDisease(selectedDis.getName());
                                                data.setObjType(selectedDis.getType());
                                                callback.onSuccess(data);
                                            } else {
                                                callback.onFailure("Error in finding Disease.");
                                            }
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            e.printStackTrace();
                                            callback.onFailure("Error in finding Disease.");
                                        }
                                    });

        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure("Error in ML Kit");
            return;
        }
    }

    private synchronized static ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer imgData =
                ByteBuffer.allocateDirect(
                        4 * AppConstants.ML_KIT_DIM_BATCH_SIZE * AppConstants.ML_KIT_DIM_IMG_SIZE_X * AppConstants.ML_KIT_DIM_IMG_SIZE_Y * AppConstants.ML_KIT_DIM_PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder());
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, AppConstants.ML_KIT_DIM_IMG_SIZE_X, AppConstants.ML_KIT_DIM_IMG_SIZE_Y,
                true);
        imgData.rewind();
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0,
                scaledBitmap.getWidth(), scaledBitmap.getHeight());
        // Convert the image to int points.
        int pixel = 0;
        for (int i = 0; i < AppConstants.ML_KIT_DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < AppConstants.ML_KIT_DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                imgData.putFloat((((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                imgData.putFloat((((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                imgData.putFloat(((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
            }
        }
        Log.i("Buffer", "Size: " + imgData.array().length);
        return imgData;
    }
}
