package com.stg.makeathon.agrohelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class HomeActivity extends AppCompatActivity implements CheckupDataListFragment.OnListFragmentInteractionListener,
        DetailsFragment.OnFragmentInteractionListener, WelcomeFragment.OnFragmentInteractionListener, LocationListener,
        ReportFragment.OnFragmentInteractionListener {
    private int CAMERA_EVENT = 0, GALLERY_EVENT = 1;
    private FloatingActionButton startCamera, openGallery;
    private String mCurrentPhotoPath;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FireBaseServices services = new FireBaseServices();
    private FragmentManager fragmentManager;
    private View progressBarContainer;
    public Context mContext;
    private Toolbar toolbar;
    private LocationManager mLocationManager;
    private Uri finalImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_home);
        fragmentManager = getSupportFragmentManager();
        if (AppData.getInstance().getCheckupDataList().size() > 0)
            replaceFragment(CheckupDataListFragment.newInstance(1), CheckupDataListFragment.class.getSimpleName());
        else
            replaceFragment(WelcomeFragment.newInstance(), WelcomeFragment.class.getSimpleName());
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            mAuth.signInAnonymously();
        }
        toolbar = findViewById(R.id.toolbar);
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
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showErrorDialog("Error", "No access to GPS.");
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        AppConstants.SELECTED_IMG = null;
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
                finalImage = compressImage(data.getData());
                AppConstants.SELECTED_IMG = finalImage.toString();
                if (finalImage == null) {
                    Toast.makeText(this, "Error occurred in file selection.", Toast.LENGTH_LONG).show();
                    return;
                }
                callFBImgService();
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
                finalImage = compressImage(imageUri);
                if (finalImage == null) {
                    Toast.makeText(this, "Error occurred in image selection.", Toast.LENGTH_LONG).show();
                    return;
                }
                callFBImgService();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error occurred in file selection.", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private void callActFBImgService(String latitude, String longitude) {
        services.processImage(this, finalImage, latitude, longitude, new FireBaseServices.OnCompletion() {
            @Override
            public void onComplete(CheckupData data) {
                hideProgressBar();
                if (data == null) {
                    Toast.makeText(mContext, "Unable to complete.", Toast.LENGTH_LONG).show();
                } else {
                    AppData.getInstance().setSelectedRecord(data);
                    AppData.getInstance().getCheckupDataList().add(data);
                    replaceFragment(DetailsFragment.newInstance(), DetailsFragment.class.getSimpleName());
                }
            }
        });
    }

    private void callFBImgService() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showErrorDialog("Error", "Location Service not enabled. Please check location permission and enable GPS.");
            return;
        }
        callActFBImgService(AppData.getInstance().getSavedContents().getLatitude(), AppData.getInstance().getSavedContents().getLongitude());
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

    private void replaceFragment(Fragment fragment, String tag) {
        if (!isFinishing() && fragmentManager != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            if (fragment instanceof DetailsFragment || fragment instanceof ReportFragment) {
                if (getSupportActionBar() != null)
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                if (toolbar != null) {
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            replaceFragment(CheckupDataListFragment.newInstance(1), CheckupDataListFragment.class.getSimpleName());
                        }
                    });
                }
                if (startCamera != null)
                    startCamera.setVisibility(View.GONE);
                if (openGallery != null)
                    openGallery.setVisibility(View.GONE);
            } else {
                if (getSupportActionBar() != null)
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                if (toolbar != null) {
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });
                }
                if (startCamera != null)
                    startCamera.setVisibility(View.VISIBLE);
                if (openGallery != null)
                    openGallery.setVisibility(View.VISIBLE);
            }
            ft.replace(R.id.mainContent, fragment, tag).commit();
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
                    .setMaxWidth(AppConstants.COMPRESSED_IMG_MAX_WIDTH)
                    .setMaxHeight(AppConstants.COMPRESSED_IMG_MAX_HEIGHT)
                    .setQuality(100)
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
            showErrorDialog("Error", "Error while finding disease.");
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

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            AppData.getInstance().getSavedContents().setLatitude(location.getLatitude()+"");
            AppData.getInstance().getSavedContents().setLongitude(location.getLongitude()+"");
            Log.d("Location Changed", location.getLatitude() + " and " + location.getLongitude());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void showErrorDialog(String title, String message) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_report) {
            replaceFragment(ReportFragment.newInstance(null, null), ReportFragment.class.getSimpleName());
            return true;
        } else if (id == R.id.action_map) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
