package com.stg.makeathon.agrohelper.config;

import java.text.SimpleDateFormat;

public class AppConstants {
    public static final String FB_DISEASE_COLLECTION_NAME = "diseaseData";
    public static final String FB_CHECKUP_COLLECTION_NAME = "checkupData";
    public static final String FB_CHECKUP_DATA_COL_APP_ID = "appId";
    public static final String SAVED_CONFIG_FILE_NAME = "configData";
    public static final int THUMBNAIL_IMG_SIZE = 192;
    public static final int COMPRESSED_IMG_MAX_WIDTH = 800;
    public static final int COMPRESSED_IMG_MAX_HEIGHT = 800;
    public static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat UI_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // ML Kit
    public static final String ML_KIT_TF_MODEL_NAME = "kisan-app-model";
    public static final String ML_KIT_TF_FILE_NAME = "kisan-app-model.tflite";
    public static final int ML_KIT_DIM_BATCH_SIZE = 1;
    public static final int ML_KIT_DIM_PIXEL_SIZE = 3;
    public static final int ML_KIT_DIM_IMG_SIZE_X = 64;
    public static final int ML_KIT_DIM_IMG_SIZE_Y = 64;
    public static final int ML_KIT_OUTPUT_SIZE = 6;

    public static final String KEYWORD_HEALTHY = "Healthy";
    public static String SELECTED_IMG = null;
}
