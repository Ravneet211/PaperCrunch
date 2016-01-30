package com.example.ravneet.cameratester;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;

import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ImageDisplayActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private GoogleApiClient mGoogleApiClient;
    private final String LOG_TAG = ImageDisplayActivity.class.getSimpleName();
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private WeakReference<Activity> imageDisplayActivityWeakReference = new WeakReference<Activity>(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(imageDisplayActivityWeakReference.get(),CameraActivity.class);
                cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(cameraIntent);
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e(LOG_TAG, "Successful YAYY!");

        Glide.get(this).register(DriveId.class, InputStream.class, new DriveIdModelLoader.Factory(mGoogleApiClient));
        String encodedDriveID = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        DriveId imageFileID = DriveId.decodeFromString(encodedDriveID);
        ImageView expandedImage = (ImageView)findViewById(R.id.expanded_image);
        Glide.with(this).from(DriveId.class).load(DriveId.decodeFromString(getIntent().getStringExtra(Intent.EXTRA_TEXT))).into(expandedImage);
        //loadInformation();
    }
    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(LOG_TAG, "GoogleApiClient connection suspended");
        Glide.get(this).unregister(DriveId.class, InputStream.class);
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(LOG_TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(LOG_TAG, "Exception while starting resolution activity", e);
        }
    }

}
