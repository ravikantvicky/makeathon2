package com.stg.makeathon.agrohelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stg.makeathon.agrohelper.config.AppConstants;
import com.stg.makeathon.agrohelper.config.AppData;
import com.stg.makeathon.agrohelper.config.SavedContents;
import com.stg.makeathon.agrohelper.domain.CheckupData;
import com.stg.makeathon.agrohelper.service.CheckupHistory;
import com.stg.makeathon.agrohelper.service.FileOperation;
import com.stg.makeathon.agrohelper.service.FileUtil;
import com.stg.makeathon.agrohelper.service.FireBaseServices;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class HomeActivity extends AppCompatActivity implements CheckupDataListFragment.OnListFragmentInteractionListener,
        DetailsFragment.OnFragmentInteractionListener, WelcomeFragment.OnFragmentInteractionListener {
    private int CAMERA_EVENT = 0, GALLERY_EVENT = 1;
    private FloatingActionButton startCamera, openGallery;
    private String mCurrentPhotoPath;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FireBaseServices services = new FireBaseServices();
    private FragmentManager fragmentManager;
    private View progressBarContainer;
    public Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_home);
        fragmentManager = getSupportFragmentManager();
        replaceFragment(WelcomeFragment.newInstance(), WelcomeFragment.class.getSimpleName());
        checkAndCreateAppId();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            mAuth.signInAnonymously();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        progressBarContainer = findViewById(R.id.progressContainer);
        startCamera = findViewById(R.id.startCamera);
        startCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return;
                    }
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(HomeActivity.this,
                                BuildConfig.APPLICATION_ID + ".provider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, CAMERA_EVENT);
                    }
                }
            }
        });
        openGallery = findViewById(R.id.openGallery);
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, GALLERY_EVENT);
            }
        });
        // Fetching History Data
        try {
            CheckupHistory historyService = new CheckupHistory();
            historyService.getCheckupHistory(new CheckupHistory.OnServiceCompleteListener() {
                @Override
                public void onSuccess(List<CheckupData> checkupDataList) {
                    if (checkupDataList == null)
                        Log.e("Checkup History", "Null value in checkupDataList");
                    else {
                        Log.i("Checkup History", checkupDataList.size() + " records found.");
                        if (checkupDataList.size() > 0){
                            AppData.getInstance().setCheckupDataList(checkupDataList);
                            replaceFragment(CheckupDataListFragment.newInstance(1), CheckupDataListFragment.class.getSimpleName());
                        }
                    }
                }

                @Override
                public void onError(String errorMsg) {
                    Log.e("Checkup History", errorMsg);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_EVENT) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "No Image selected.", Toast.LENGTH_LONG).show();
                return;
            }
            if (data == null || data.getData() == null) {
                Toast.makeText(this, "No Image selected.", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                showProgressBar();
                Uri compressedImg = compressImage(data.getData());
                if (compressedImg == null) {
                    Toast.makeText(this, "Error occurred in file selection.", Toast.LENGTH_LONG).show();
                    return;
                }
                services.processImage(this, compressedImg, new FireBaseServices.OnCompletion() {
                    @Override
                    public void onComplete(CheckupData data) {
                        hideProgressBar();
                        if (data == null) {
                            Toast.makeText(mContext, "Unable to complete.", Toast.LENGTH_LONG).show();
                        } else {
                            AppData.getInstance().setSelectedRecord(data);
                            replaceFragment(DetailsFragment.newInstance(), DetailsFragment.class.getSimpleName());
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error occurred in file selection.", Toast.LENGTH_LONG).show();
                return;
            }
        } else if (requestCode == CAMERA_EVENT) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "No Image selected.", Toast.LENGTH_LONG).show();
                return;
            }
            Uri imageUri = Uri.parse(mCurrentPhotoPath);

            try {
                showProgressBar();
                Uri compressedImg = compressImage(imageUri);
                if (compressedImg == null) {
                    Toast.makeText(this, "Error occurred in image selection.", Toast.LENGTH_LONG).show();
                    return;
                }
                services.processImage(this, compressedImg, new FireBaseServices.OnCompletion() {
                    @Override
                    public void onComplete(CheckupData data) {
                        hideProgressBar();
                        if (data == null) {
                            Toast.makeText(mContext, "Unable to complete.", Toast.LENGTH_LONG).show();
                        } else {
                            AppData.getInstance().setSelectedRecord(data);
                            replaceFragment(DetailsFragment.newInstance(), DetailsFragment.class.getSimpleName());
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error occurred in file selection.", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
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
            }
        });
    }

    private void replaceFragment(Fragment fragment, String tag) {
        if (!isFinishing() && fragmentManager != null) {
            fragmentManager.beginTransaction().replace(R.id.mainContent, fragment, tag).commit();
        }
    }

    private void showProgressBar() {
        if (progressBarContainer != null)
            progressBarContainer.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (progressBarContainer != null)
            progressBarContainer.setVisibility(View.GONE);
    }

    private Uri compressImage(Uri actImg) {
        Uri finalImage = null;
        try {
            File actualImage = FileUtil.from(this, actImg);
            File compressedImage = new Compressor(this)
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(90)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .compressToFile(actualImage);
            finalImage = Uri.fromFile(compressedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return finalImage;
    }

    @Override
    public void onCheckupHistorySelection(CheckupData data) {
        if (data == null) {
            Toast.makeText(mContext, "Unable to complete.", Toast.LENGTH_LONG).show();
        } else {
            AppData.getInstance().setSelectedRecord(data);
            replaceFragment(DetailsFragment.newInstance(), DetailsFragment.class.getSimpleName());
        }
    }

    @Override
    public void onDetailsFragmentAction(Uri uri) {

    }

    @Override
    public void onProgressStart() {
        showProgressBar();
    }

    @Override
    public void onProgressComplete() {
        hideProgressBar();
    }
}
