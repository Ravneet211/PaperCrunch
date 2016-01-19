package com.example.ravneet.cameratester;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DriveSaveActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = DriveSaveActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private Bitmap mBitmapToSave;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private final Activity activityReference = this;
    public static CustomPropertyKey customPropertyKey = new CustomPropertyKey("timestamp",CustomPropertyKey.PUBLIC);
    ProgressDialog savingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_drive_save);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
        savingDialog = new ProgressDialog(this);
        savingDialog.setTitle("Saving to Google Drive");
        savingDialog.show();
    }
    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
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
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");
        if (mBitmapToSave == null) {
            // This activity has no UI of its own. Just start the camera.
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"MyCameraApp");
            File imageFile = new File(mediaStorageDir.getPath()+File.separator+"receipt.jpg");
            if(!imageFile.exists()) {
                throw new NullPointerException("Image not saved");
            }
            else {
                mBitmapToSave = getImage(imageFile);
            }
        }
        saveFileToDrive();
    }
    private Bitmap getImage(File imageFile) {
        return BitmapFactory.decodeFile(imageFile.getPath());
    }
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        final Bitmap image = mBitmapToSave;

        final DriveFolder appFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
        if (appFolder != null) {
            Log.e(TAG, appFolder.toString());
        }
        new Thread() {
            public void run() {
                createFoldersIfNotCreated(appFolder);
            }
        }.start();
//        new Thread() {
//            public void run() {
//                createFoldersIfNotCreated(appFolder);
//            }
//        }.start();
//        savingDialog.dismiss();
//        Intent intent = new Intent(this,HomeActivity.class);
//        startActivity(intent);

//        Drive.DriveApi.newDriveContents(mGoogleApiClient)
//                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
//
//                    @Override
//                    public void onResult(final DriveApi.DriveContentsResult result) {
//                        // If the operation was not successful, we cannot do anything
//                        // and must
//                        // fail.
//                        if (!result.getStatus().isSuccess()) {
//                            Log.i(TAG, "Failed to create new contents.");
//                            return;
//                        }
//                        // Otherwise, we can write our data to the new contents.
//                        /*Log.i(TAG, "New contents created.");
//                        // Get an output stream for the contents.
//                        OutputStream outputStream = result.getDriveContents().getOutputStream();
//                        // Write the bitmap data from it.
//                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
//                        image.compress(Bitmap.CompressFormat.JPEG, 100, bitmapStream);
//                        try {
//                            outputStream.write(bitmapStream.toByteArray());
//                        } catch (IOException e1) {
//                            Log.i(TAG, "Unable to write file contents.");
//                        }
//                        // Create the initial metadata - MIME type and title.
//                        // Note that the user will be able to change the title later.
//                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
//                                .setMimeType("image/jpeg").setTitle("Receipt.jpg").build();
//                        // Create an intent for the file chooser, and start it.
//                        Drive.DriveApi.getRootFolder(mGoogleApiClient)
//                                .createFile(mGoogleApiClient, metadataChangeSet, result.getDriveContents())
//                                .setResultCallback(fileCallback);*/
//                        new Thread() {
//                            @Override
//                            public void run() {
//                                String type = getIntent().getStringExtra("Type");
//                                Log.e(TAG, type);
//
//                                OutputStream outputStream = result.getDriveContents().getOutputStream();
//                                // Write the bitmap data from it.
//                                ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
//                                image.compress(Bitmap.CompressFormat.JPEG, 100, bitmapStream);
//                                try {
//                                    outputStream.write(bitmapStream.toByteArray());
//                                } catch (IOException e1) {
//                                    Log.i(TAG, "Unable to write file contents.");
//                                }
//                                // Create the initial metadata - MIME type and title.
//                                // Note that the user will be able to change the title later.
//                                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
//                                        .setMimeType("image/jpeg").setTitle("Receipt.jpg").build();
//                                // Create an intent for the file chooser, and start it.
//                                Drive.DriveApi.getRootFolder(mGoogleApiClient)
//                                        .createFile(mGoogleApiClient, metadataChangeSet, result.getDriveContents())
//                                        .setResultCallback(fileCallback);
//                                //get app folder
//                                //saveFile(type);
//                            }
//                        }.start();
//                    }
//                });
    }


   /* final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    savingDialog.dismiss();
                    if (!result.getStatus().isSuccess()) {
                        Log.e(TAG,"Error while trying to create the file");

                        return;
                    }
                    Log.e(TAG, "Created a file with content: " + result.getDriveFile().getDriveId());
                    Intent intent = new Intent(activityReference,HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            };*/



    public void createFoldersIfNotCreated(final DriveFolder appFolder) {
        Query receiptsQuery = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "OCRReceipts2"))
                .build();
        appFolder.queryChildren(mGoogleApiClient,receiptsQuery).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult result) {

                    // Iterate over the matching Metadata instances in mdResultSet
                    if (!result.getStatus().isSuccess()) {
                        Log.v(TAG, "Problem while trying to fetch metadata.");
                        endActivity();
                        return;
                    }

                MetadataBuffer metadataBuffer = result.getMetadataBuffer();
                String data = getIntent().getStringExtra(Intent.EXTRA_TEXT);
                if(metadataBuffer == null) {
                    Log.e(TAG,"As Expected");
                    endActivity();
                }
                if (metadataBuffer != null ) {
                    if(metadataBuffer.getCount() == 0) {
                        createRecieptsFolder(appFolder);
                    }
                    for (Metadata md : metadataBuffer) {
                        if(md != null && md.isDataValid())
                            Log.e(TAG,md.toString());
                            String type = getIntent().getStringExtra("Type");
                            if(type.equals("Receipt")) {
                                saveDataToFolder(data,"OCRReceipts2");
                            }

                    }
                }
                if(metadataBuffer != null) {
                    metadataBuffer.close();
                }
            }
        });

        Query documentsQuery = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "OCRDocuments2"))
                .build();
        appFolder.queryChildren(mGoogleApiClient,documentsQuery).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult result) {

                // Iterate over the matching Metadata instances in mdResultSet
                if (!result.getStatus().isSuccess()) {
                    Log.v(TAG, "Problem while trying to fetch metadata.");
                    endActivity();
                    return;
                }
                String data = getIntent().getStringExtra(Intent.EXTRA_TEXT);
                MetadataBuffer metadataBuffer = result.getMetadataBuffer();
                if(metadataBuffer == null) {
                    Log.e(TAG,"As Expected");
                }
                if (metadataBuffer != null ) {
                    if(metadataBuffer.getCount() == 0) {
                        createDocumentsFolder(appFolder);
                    }
                    for (Metadata md : metadataBuffer) {
                        if(md != null && md.isDataValid())
                            Log.e(TAG,md.toString());
                            String type = getIntent().getStringExtra("Type");
                            if(type.equals("Document")) {
                                saveDataToFolder(data,"OCRDocuments2");
                            }

                    }
                }
                if(metadataBuffer != null) {
                    metadataBuffer.close();
                }
            }
        });

    }

    private void createRecieptsFolder(final DriveFolder appFolder) {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("OCRReceipts2").build();
        appFolder.createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(DriveFolder.DriveFolderResult driveFolderResult) {
                if(driveFolderResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Receipts folder created");
                    saveDataToFolder(getIntent().getStringExtra(Intent.EXTRA_TEXT), "OCRReceipts2");
                }
                else {
                    Log.e(TAG, "Uh Oh! Problem creating Receipts folder");
                    endActivity();
                }
            }
        });
    }



    private void createDocumentsFolder(final DriveFolder appFolder) {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("OCRDocuments2").build();
        appFolder.createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(DriveFolder.DriveFolderResult driveFolderResult) {
                if(driveFolderResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Documents folder created");
                    saveDataToFolder(getIntent().getStringExtra(Intent.EXTRA_TEXT), "OCRDocuments2");
                }
                else {
                    Log.e(TAG,"Uh Oh! Problem creating Documents folder");
                    endActivity();
                }
            }
        });
    }
    public void saveDataToFolder(final String data, final String folderName) {
        DriveFolder rootFolder;
        if (folderName == "OCRDocuments2") {
            rootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
        } else {
            rootFolder = Drive.DriveApi.getAppFolder(mGoogleApiClient);
        }
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, folderName))
                .build();
        rootFolder.queryChildren(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                if (!metadataBufferResult.getStatus().isSuccess()) {
                    Log.v(TAG, "Problem while trying to fetch metadata.");
                    endActivity();
                    return;
                }

                MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();
                if (metadataBuffer == null) {
                    Log.e(TAG, "Error: metaDataBuffer Null");
                }
                if (metadataBuffer != null) {
                    for (Metadata md : metadataBuffer) {
                        if (md != null && md.isDataValid()) {
                            DriveId folderID = md.getDriveId();
                            writeResultToFolder(folderName,data,folderID);
                        }

                    }
                }
                if (metadataBuffer != null) {
                    metadataBuffer.close();
                }
            }
        });
    }

    private void writeResultToFolder(String folderName,String data, DriveId folderID) {
        final DriveFolder driveFolder = Drive.DriveApi.getFolder(mGoogleApiClient, folderID);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        MetadataChangeSet changeSet;
        MetadataChangeSet imageChangeSet;
        if(folderName == "OCRDocuments2") {
             changeSet = new MetadataChangeSet.Builder()
                    .setTitle("OCRdoc"+timeStamp)
                    .setMimeType("application/msword").setCustomProperty(customPropertyKey,timeStamp).build();
             imageChangeSet = new MetadataChangeSet.Builder()
                     .setMimeType("image/jpeg").setTitle("OCRDOCpic"+timeStamp+".jpg").setCustomProperty(customPropertyKey,timeStamp).build();
        }
        else {
            changeSet = new MetadataChangeSet.Builder()
                    .setTitle("OCRBill"+timeStamp)
                    .setMimeType("text/plain").setCustomProperty(customPropertyKey,timeStamp).build();
            imageChangeSet = new MetadataChangeSet.Builder()
                    .setMimeType("image/jpeg").setTitle("OCRBILLpic" + timeStamp + ".jpg").setCustomProperty(customPropertyKey,timeStamp).build();

        }
        driveFolder.createFile(mGoogleApiClient, changeSet, null).setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
            @Override
            public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                if (!driveFileResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Error Creating txt file");
                } else {
                    Log.e(TAG, "Created txt file");
                    writeDataToFile(getIntent().getStringExtra(Intent.EXTRA_TEXT), driveFileResult.getDriveFile().getDriveId());
                }
            }
        });
        driveFolder.createFile(mGoogleApiClient, imageChangeSet, null).setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
            @Override
            public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                if(!driveFileResult.getStatus().isSuccess()) {
                    Log.e(TAG,"Error Creating image file");
                }
                else {
                    Log.e(TAG, "Created image file");
                    writeImageToFile(mBitmapToSave, driveFileResult.getDriveFile().getDriveId());
                }
            }
        });
    }
    public void endActivity() {
        savingDialog.dismiss();
        Intent intent = new Intent(activityReference,HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    public void writeDataToFile(final String s,DriveId driveId) {
        DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient,driveId);
        driveFile.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(final DriveApi.DriveContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Error opening created file");// Handle error
                    return;
                }
                DriveContents driveContents = result.getDriveContents();
                Log.e(TAG, driveContents.toString());
                try {
                    /*ParcelFileDescriptor parcelFileDescriptor = driveContents.getParcelFileDescriptor();
                    FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor
                            .getFileDescriptor());
                    // Read to the end of the file.
                    fileInputStream.read(new byte[fileInputStream.available()]);

                    // Append to the file.
                    FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor
                            .getFileDescriptor());
                    Writer writer = new OutputStreamWriter(fileOutputStream);
                    writer.write(s);
                    driveContents.commit(mGoogleApiClient, null).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    if (status.isSuccess()) {
                                        Log.e(TAG,"Written to file successfully");
                                        endActivity();
                                    }
                                    else {
                                        Log.e(TAG,"File Write Failed");
                                    }
                                }
                            }
                    );*/
                    OutputStream outputStream = driveContents.getOutputStream();
                    outputStream.write(s.getBytes());
                    driveContents.commit(mGoogleApiClient, null).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.e(TAG, "Written to file successfully");
                            } else {
                                Log.e(TAG, "Error writing to file");
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "Error with OutputStream");
                }

            }
        });

    }
    public void writeImageToFile(final Bitmap bitmap,DriveId driveID) {
        DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, driveID);
        driveFile.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(final DriveApi.DriveContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Error opening created file");// Handle error
                    return;
                }
                DriveContents driveContents = result.getDriveContents();
                Log.e(TAG, driveContents.toString());
                OutputStream outputStream = driveContents.getOutputStream();
                ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bitmapStream);
                try {
                    outputStream.write(bitmapStream.toByteArray());
                    driveContents.commit(mGoogleApiClient,null).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Log.e(TAG,"Image write successful:" + Boolean.toString(status.isSuccess()));
                            endActivity();
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    endActivity();
                }
            }

        });
    }
}
