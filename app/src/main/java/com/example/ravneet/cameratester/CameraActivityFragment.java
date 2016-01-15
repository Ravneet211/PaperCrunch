package com.example.ravneet.cameratester;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.isseiaoki.simplecropview.CropImageView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

/**
 * A placeholder fragment containing a simple view.
 * <p/>
 * Created by Ravneet on 1/15/16.
 */
/**
 * Created by Ravneet on 1/15/16.
 */

/**
 * A placeholder fragment containing a simple view.
 */
public class CameraActivityFragment extends Fragment {
    private static final String LOG_TAG = CameraActivityFragment.class.getSimpleName();
    private Camera mCamera;
    private CameraPreview mPreview;
    public View rootView;
    public FrameLayout preview;
    public CropImageView imageView;
    Button saveButton;
    Button discardButton;
    Button captureButton;
    String rs;
    TextView ocrResult;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            View v = getView();
            FrameLayout preview = (FrameLayout) v.findViewById(R.id.camera_preview);
            preview.removeView(v.findViewWithTag("Surface"));
            releaseCameraAndPreview();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            if (realImage.getWidth() > realImage.getHeight()) {
                realImage = rotateImageIfRequired(realImage, 90);
            }
            realImage = expandBitmap(realImage);
            data = null;
            imageView = new CropImageView(getActivity());
            imageView.setImageBitmap(realImage);
            imageView.setPadding(0, 0, 0, 0);
            imageView.setAdjustViewBounds(true);
            RelativeLayout contain = (RelativeLayout) preview.getParent();
            contain.removeView(captureButton);
            preview.addView(imageView);
            saveButton.setVisibility(View.VISIBLE);
            discardButton.setVisibility(View.VISIBLE);
            captureButton.setVisibility(View.GONE);
            configureButtons(discardButton, saveButton, imageView);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, 0, 0);
            imageView.setLayoutParams(lp);
            imageView.setCropEnabled(true);
            imageView.setCropMode(CropImageView.CropMode.RATIO_FREE);
            imageView.setInitialFrameScale(0.6f);
        }

        public static final int MEDIA_TYPE_IMAGE = 1;
        public static final int MEDIA_TYPE_VIDEO = 2;

        /** Create a file Uri for saving an image or video */
        private Uri getOutputMediaFileUri(int type) {
            return Uri.fromFile(getOutputMediaFile(type));
        }

        /** Create a File for saving an image or video */
        private File getOutputMediaFile(int type) {
            // To be safe, you should check that the SDCard is mounted
            // using Environment.getExternalStorageState() before doing this.

            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyCameraApp");
            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile;
            if (type == MEDIA_TYPE_IMAGE) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "IMG_" + timeStamp + ".jpg");
            } else if (type == MEDIA_TYPE_VIDEO) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "VID_" + timeStamp + ".mp4");
            } else {
                return null;
            }

            return mediaFile;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onCreateView() called");
        rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        releaseCameraAndPreview();
        saveButton = (Button) rootView.findViewById(R.id.crop_button);
        saveButton.setVisibility(View.INVISIBLE);
        discardButton = (Button) rootView.findViewById(R.id.discard_button);
        discardButton.setVisibility(View.INVISIBLE);
        Toolbar plz = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (plz != null) {
            plz.setVisibility(View.GONE);
        }
        if(mCamera == null) {
            mCamera = getCameraInstance();
            mCamera.setDisplayOrientation(90);
            if(mPreview != null) {
                preview.removeView(mPreview);
            }
        }
                // Create our Preview view and set it as the content of our activity.
        if (mCamera != null) {
            mPreview = new CameraPreview(getActivity(), mCamera);
            preview = (FrameLayout) rootView.findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            mPreview.setTag("Surface");


            captureButton = (Button) rootView.findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // get an image from the camera
                            if (mCamera != null) {
                                mCamera.takePicture(null, null, mPicture);
                            }
                        }
                    });

        }// Add a listener to the Capture button
        return rootView;

    }


    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error opening camera instance");// Camera is not available (in use or does not exist)
        }
        return c;
    }

    private void releaseCameraAndPreview() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mPreview != null) {
            mPreview.destroyDrawingCache();
            mPreview.mCamera = null;
            preview.removeView(mPreview);
            mPreview = null;
        }
    }

    public Bitmap rotateImageIfRequired(Bitmap bitmap, int degree) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "receipt.jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public Bitmap expandBitmap(Bitmap bitmap) {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        Toolbar plz = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (plz != null) {
            plz.setVisibility(View.GONE);
        }
        int height = getActivity().getWindow().getDecorView().getHeight();
        Rect rectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        height -= statusBarHeight;
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    public void configureButtons(Button discardButton, final Button cropButton, final CropImageView cropImageView) {
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap croppedImage = cropImageView.getCroppedBitmap();
                cropImageView.setCropEnabled(false);
                cropImageView.setImageBitmap(croppedImage);
                cropButton.setText("Analyze");
                cropButton.setOnClickListener(null);
                changeCropButton(cropButton, croppedImage);

            }
        });
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContextWrapper cw = new ContextWrapper(getContext());
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                File mypath = new File(directory, "bill.jpg");
                if (mypath.exists()) {
                    Log.e(LOG_TAG, "Deleted: " + Boolean.toString(mypath.delete()));
                }
                getActivity().recreate();
            }
        });
    }

    public void changeCropButton(final Button analyzeButton, final Bitmap croppedImage) {
        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocrResult = new TextView(getActivity());
                discardButton.setVisibility(View.INVISIBLE);
                analyzeButton.setVisibility(View.INVISIBLE);
                scanImage(croppedImage);
                preview.removeView(imageView);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                ocrResult.setGravity(Gravity.CENTER);
                ocrResult.setLayoutParams(lp);
                ocrResult.setVisibility(View.VISIBLE);
                ocrResult.setTextSize(8);
                preview.addView(ocrResult);
                analyzeButton.setOnClickListener(null);

            }
        });
    }

    private String saveToInternalSorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "bill.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
        return directory.getAbsolutePath();
    }

    private String scanImage(final Bitmap bitmap) {
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        RequestParams params = new RequestParams();
        String path = saveToInternalSorage(bitmap);
        params.put("apikey", "helloworld");
        final File imageFile = new File(path, "bill.jpg");
        try {
            params.put("file", imageFile);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
        params.put("language", "eng");
        client.post("https://api.ocr.space/parse/image", params, new AsyncHttpResponseHandler() {
            ProgressDialog progressDialog = new ProgressDialog(getActivity());

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                // error handling
                rs = "Network connection Weak. Connect to network and try again";
                Log.e(LOG_TAG, throwable.toString());
                ocrResult.setText(rs);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    rs = new String(responseBody, "UTF-8");// success
                    rs = analyzeJSONString(rs);
                    ocrResult.setText(rs);
                } catch (Exception e) {
                    Log.v(LOG_TAG, e.toString());
                }

            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
                discardButton.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                saveButton.setText("Save Result");
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveToExternalStorage(bitmap);
                        startLoginActivity(rs);
                    }
                });
            }

            @Override
            public void onStart() {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("Analyzing");
                progressDialog.show();
            }

            public String analyzeJSONString(String s) {
                String response = "";
                try {
                    JSONObject rootObject = new JSONObject(s);
                    boolean isError = Boolean.getBoolean(rootObject.getString("IsErroredOnProcessing"));
                    if (isError) {
                        String errorMessage = rootObject.getString("ErrorMessage");
                        String errorDetails = rootObject.getString("ErrorDetails");
                        response = "ErrorMessage: " + errorMessage + "\n" + "ErrorDetails: " + errorDetails;
                    } else {
                        JSONArray parsedResults = rootObject.getJSONArray("ParsedResults");
                        JSONObject redundantaf = parsedResults.getJSONObject(0);
                        String parse = redundantaf.getString("ParsedText");
                        if (!parse.equals("")) {
                            response = parse;
                        } else {
                            String errorMessage = redundantaf.getString("ErrorMessage");
                            String errorDetails = redundantaf.getString("ErrorDetails");
                            response = "ErrorMessage: " + errorMessage + "\n" + "ErrorDetails: " + errorDetails;
                        }
                    }

                } catch (JSONException e) {
                    response = "API not responding correctly.";
                    Log.e(LOG_TAG, e.toString());
                }
                return response;
            }
        });
        return rs;
    }

    public void saveToExternalStorage(Bitmap bitmap) {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(LOG_TAG, "Error creating media file, check storage permissions");
            return;
        }

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(byteArray);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
        }
    }

    public void startLoginActivity(String s) {
        Intent intent = new Intent(getActivity(), SignInActivityWithDrive.class);
        intent.putExtra(Intent.EXTRA_TEXT, s);
        intent.putExtra("Parent Activity", CameraActivity.class.getSimpleName());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseCameraAndPreview();
    }

    @Override
    public void onPause() {
        super.onPause();
        //releaseCameraAndPreview();
    }
    @Override
    public void onStop() {
        super.onStop();
        releaseCameraAndPreview();
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.e(LOG_TAG, "onStart() called");
        if (mCamera == null) {
            Log.e(LOG_TAG, "onStart() camera null");
            mCamera = getCameraInstance();
            mCamera.setDisplayOrientation(90);
            if (mCamera != null) {
                mPreview = new CameraPreview(getActivity(), mCamera);
                preview = (FrameLayout) rootView.findViewById(R.id.camera_preview);
                preview.addView(mPreview);
                mPreview.setTag("Surface");
            }
        }
    }
        // Create our Preview view and set it as the content of our activity.
}