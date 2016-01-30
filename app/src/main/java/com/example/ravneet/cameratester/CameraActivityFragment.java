package com.example.ravneet.cameratester;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;

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
    private TextView discardInstruction;
    private TextView cropInstruction;
    ImageView saveButton;
    ImageView discardButton;
    ImageView captureButton;
    ImageView analyzeBill;
    TextView receiptAnalyzeInstruction;
    LinearLayout receiptAnalyzeContainerLayout;
    String rs;
    EditText ocrResult;
    private String ScanType;

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
            saveButton.bringToFront();
            discardButton.setVisibility(View.VISIBLE);
            discardButton.bringToFront();
            discardInstruction.setVisibility(View.VISIBLE);
            discardInstruction.bringToFront();
            captureButton.setVisibility(View.GONE);
            cropInstruction.setVisibility(View.VISIBLE);
            cropInstruction.bringToFront();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onCreateView() called");
        rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        releaseCameraAndPreview();
        saveButton = (ImageView) rootView.findViewById(R.id.crop_button);
        saveButton.bringToFront();
        saveButton.setVisibility(View.INVISIBLE);
        discardButton = (ImageView) rootView.findViewById(R.id.discard_button);
        discardButton.setVisibility(View.INVISIBLE);
        discardInstruction = (TextView)rootView.findViewById(R.id.discard_instruction);
        discardInstruction.setVisibility(View.INVISIBLE);
        cropInstruction = (TextView)rootView.findViewById(R.id.crop_instruction);
        cropInstruction.setVisibility(View.VISIBLE);
        analyzeBill = (ImageView)rootView.findViewById(R.id.bill_analyze);
        analyzeBill.setVisibility(View.INVISIBLE);
        receiptAnalyzeInstruction = (TextView)rootView.findViewById(R.id.receipt_scan_instruction);
        receiptAnalyzeInstruction.setVisibility(View.INVISIBLE);
        receiptAnalyzeContainerLayout = (LinearLayout)rootView.findViewById(R.id.receipt_instruction_container);
        if(mCamera == null) {
            releaseCameraAndPreview();
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


            captureButton = (ImageView) rootView.findViewById(R.id.button_capture);
            captureButton.bringToFront();
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
        int height = getActivity().getWindow().getDecorView().getHeight();
        Rect rectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        height -= statusBarHeight;
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    public void configureButtons(final ImageView discardButton, final ImageView cropButton, final CropImageView cropImageView) {
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap croppedImage = cropImageView.getCroppedBitmap();
                cropImageView.setCropEnabled(false);
                cropImageView.setImageBitmap(croppedImage);
                cropButton.setOnClickListener(null);
                cropButton.setImageResource(R.drawable.ic_description_black_24dp);
                cropInstruction.setText("Doc\nScan");
                cropInstruction.setTextColor(Color.parseColor("#000000"));
                discardButton.setImageResource(R.drawable.ic_cancel_black_48dp);
                discardInstruction.setTextColor(Color.parseColor("#000000"));
                addAnalyzeBillButton(croppedImage);
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

    public void changeCropButton(final ImageView analyzeButton, final Bitmap croppedImage) {
        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocrResult = new EditText(getActivity());
                ocrResult.setBackgroundResource(R.drawable.rounded_edittext);
                discardButton.setVisibility(View.INVISIBLE);
                discardInstruction.setVisibility(View.INVISIBLE);
                analyzeButton.setVisibility(View.INVISIBLE);
                receiptAnalyzeContainerLayout.setVisibility(View.INVISIBLE);
                cropInstruction.setVisibility(View.INVISIBLE);
                ScanType = "Document";
                scanImage(croppedImage);
                preview.removeView(imageView);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                ocrResult.setGravity(Gravity.CENTER);
                ocrResult.setLayoutParams(lp);
                ocrResult.setVisibility(View.VISIBLE);
                ocrResult.setTextSize(15);
                lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
                ScrollView scrollView = new ScrollView(getActivity());
                lp.setMargins(0,dpToPx(144),0,0);
                scrollView.addView(ocrResult);
                scrollView.setLayoutParams(lp);
                preview.addView(scrollView);
                analyzeButton.setOnClickListener(null);


            }
        });
    }
    private void addAnalyzeBillButton(final Bitmap croppedImage) {
        analyzeBill.setVisibility(View.VISIBLE);
        analyzeBill.bringToFront();
        receiptAnalyzeInstruction.setVisibility(View.VISIBLE);
        receiptAnalyzeInstruction.setText("Receipt\nScan");
        receiptAnalyzeInstruction.bringToFront();
        receiptAnalyzeContainerLayout.bringToFront();
        analyzeBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder builtString = new StringBuilder("");
                ScanType="Bill";//scanBill`
                preview.removeAllViews();
                ScanReceipt scanReceipt = new ScanReceipt();
                scanReceipt.execute(croppedImage);

                /*ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Analyzing");
                progressDialog.show();
                final File imgPath = saveToExternalStorageString(croppedImage);
                Log.e(GroceryLineBreakMatch.class.getSimpleName(), "Saved to External Storage");
                ArrayList<Integer> lineBreaks = GroceryLineBreakMatch.preProcess(imgPath.getAbsolutePath(), 0);
                ArrayList<Bitmap> linedImages = new ArrayList<Bitmap>();
                for(int i = 0; i < lineBreaks.size()-1;i++) {
                    linedImages.add(Bitmap.createBitmap(croppedImage,0,lineBreaks.get(i),croppedImage.getWidth(),Math.min(croppedImage.getHeight()-lineBreaks.get(i),lineBreaks.get(i+1)-lineBreaks.get(i)+15)));
                }
                LinearLayout l = new LinearLayout(getActivity());
                l.setOrientation(LinearLayout.VERTICAL);
                ScrollView s = new ScrollView(getActivity());
                s.addView(l);
                preview.addView(s);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 10);
                //displayBitmaps(linedImages, l, lp);
                scanBitmaps(linedImages, 0,progressDialog, l,new HashSet<Integer>());*/

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
                eliminateSaveButton();
                Log.e(LOG_TAG, throwable.toString());
                ocrResult.setText(rs);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    rs = new String(responseBody, "UTF-8");// success
                    rs = analyzeJSONString(rs);
                    ocrResult.setText(rs);
                    Log.e(LOG_TAG, rs);
                    if (rs.substring(0, 6).equals("Error")) {
                        eliminateSaveButton();
                    } else {
                        cropInstruction.setVisibility(View.VISIBLE);
                        saveButton.setVisibility(View.VISIBLE);
                        saveButton.setImageResource(R.drawable.ic_check_circle_black_48dp);
                        cropInstruction.setText("Save\nResults");
                        saveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                saveToExternalStorage(bitmap);
                                startLoginActivity(ocrResult.getText().toString().trim(), ScanType);
                            }
                        });
                    }
                    Log.e(LOG_TAG, ocrResult.getText().toString().trim());
                } catch (Exception e) {
                    Log.v(LOG_TAG, e.toString());
                }

            }

            public void eliminateSaveButton() {
                saveButton.setVisibility(View.GONE);
                cropInstruction.setVisibility(View.GONE);
            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
                discardButton.setVisibility(View.VISIBLE);
                discardInstruction.setVisibility(View.VISIBLE);
                deleteFromInternalStorage();
            }

            @Override
            public void onStart() {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("Analyzing");
                progressDialog.show();
                progressDialog.setCancelable(false);
            }

            public String analyzeJSONString(String s) {
                String response = "";
                try {
                    JSONObject rootObject = new JSONObject(s);
                    boolean isError = Boolean.getBoolean(rootObject.getString("IsErroredOnProcessing"));
                    if (isError) {
                        String errorMessage = rootObject.getString("ErrorMessage");
                        String errorDetails = rootObject.getString("ErrorDetails");
                        response = "Error: " + errorMessage;
                    } else {
                        JSONArray parsedResults = rootObject.getJSONArray("ParsedResults");
                        JSONObject redundantaf = parsedResults.getJSONObject(0);
                        String parse = redundantaf.getString("ParsedText");
                        if (!parse.equals("")) {
                            response = parse;
                        } else {
                            String errorMessage = redundantaf.getString("ErrorMessage");
                            String errorDetails = redundantaf.getString("ErrorDetails");
                            response = "Error: " + errorMessage;
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

    public void startLoginActivity(String s, String scanType) {
        Intent intent = new Intent(getActivity(), SignInActivityWithDrive.class);
        intent.putExtra(Intent.EXTRA_TEXT, s);
        intent.putExtra("Parent Activity", CameraActivity.class.getSimpleName());
        intent.putExtra("Type", ScanType);
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
            getActivity().recreate();
            Log.e(LOG_TAG, "onStart() camera null");
            mCamera = getCameraInstance();
            mCamera.setDisplayOrientation(90);
            if (mCamera != null) {
                mPreview = new CameraPreview(getActivity(), mCamera);
                preview = (FrameLayout) rootView.findViewById(R.id.camera_preview);
                preview.addView(mPreview);
                /*captureButton.setVisibility(View.VISIBLE);
                discardButton.setVisibility(View.INVISIBLE);
                saveButton.setVisibility(View.INVISIBLE);*/
                mPreview.setTag("Surface");
            }
        }
    }
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
    public void deleteFromInternalStorage() {
        ContextWrapper cw = new ContextWrapper(getContext());
        // path to /data/data/yourapp/app_data/imageDir
        if(cw != null && getContext() != null) {
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            // Create imageDir
            File mypath = new File(directory, "bill.jpg");
            if (mypath.exists()) {
                Log.e(LOG_TAG, "Deleted from internal storage");
                mypath.delete();
            }
        }
    }
    public File saveToExternalStorageString(Bitmap bitmap) {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(LOG_TAG, "Error creating media file, check storage permissions");
            return null;
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
        return pictureFile;
    }
    public void scanBitmaps(final ArrayList<Bitmap> bitmaps, final int i, final ProgressDialog progressDialog, final LinearLayout l, final HashSet<Integer> checkedBoxes,final int checkBoxes) {
        if(i >= bitmaps.size()) {
            return;
        }
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        RequestParams params = new RequestParams();
        String path = saveToInternalSorage(bitmaps.get(i));
        params.put("apikey", "helloworld");
        final File imageFile = new File(path, "bill.jpg");
        try {
            params.put("file", imageFile);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }
        params.put("language", "eng");
        client.post("https://api.ocr.space/parse/image", params, new AsyncHttpResponseHandler() {
            int updatedCheckBoxes = checkBoxes;
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                // error handling
                Log.e(LOG_TAG, "Failed for image");
                Log.e(LOG_TAG,throwable.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    rs = new String(responseBody, "UTF-8");// success
                    rs = analyzeJSONString(rs);
                    TextView t = new TextView(getActivity());
                    if(rs.contains("Error")) {

                    }
                    else {
                        updatedCheckBoxes+=1;
                        ArrayList<String> itemAndPrice = extractItemAndPrice(rs);
                        Log.e(LOG_TAG, itemAndPrice.toString());
                        LinearLayout priceItemLayout = new LinearLayout(getActivity());
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, 10);
                        priceItemLayout.setOrientation(LinearLayout.HORIZONTAL);
                        CheckBox c = new CheckBox(getActivity());
                        c.setChecked(false);
                        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(isChecked) {
                                    checkedBoxes.add(updatedCheckBoxes);
                                }
                                else {
                                    if(checkedBoxes.contains(updatedCheckBoxes)) {
                                        checkedBoxes.remove(updatedCheckBoxes);
                                    }
                                }
                            }
                        });
                        priceItemLayout.addView(c);
                        if(!itemAndPrice.get(0).equals("") && itemAndPrice.get(0) != null) {
                            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);
                            EditText e = new EditText(getActivity());
                            e.setBackgroundResource(R.drawable.rounded_edittext);
                            e.setText(itemAndPrice.get(0));
                            e.setGravity(Gravity.CENTER_HORIZONTAL);
                            e.setLayoutParams(lp2);
                            lp2.setMargins(0,0,dpToPx(20),0);
                            priceItemLayout.addView(e);
                        }
                        if(!itemAndPrice.get(1).equals("") && itemAndPrice.get(1) != null) {
                            LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                            EditText e = new EditText(getActivity());
                            e.setPadding(dpToPx(10),0,dpToPx(10),0);
                            e.setLayoutParams(lp3);
                            e.setGravity(Gravity.CENTER);
                            e.setBackgroundResource(R.drawable.rounded_edittext);
                            e.setText(itemAndPrice.get(1));
                            priceItemLayout.addView(e);
                        }
                        priceItemLayout.setLayoutParams(lp);
                        l.addView(priceItemLayout);


                       /* t.setText(rs);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0,0,0,10);
                        t.setLayoutParams(lp);
                        l.addView(t);*/
                    }
                }catch(Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }


            @Override
            public void onFinish() {
                if(i == bitmaps.size()-1) {
                    progressDialog.dismiss();
                    createAddButton(l);
                }
                discardButton.setVisibility(View.VISIBLE);
                discardInstruction.setVisibility(View.VISIBLE);
                deleteFromInternalStorage();
                scanBitmaps(bitmaps, i + 1, progressDialog, l, checkedBoxes, updatedCheckBoxes);
            }

            @Override
            public void onStart() {
                if( i == 0) {
                    /*progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setTitle("Analyzing");
                    progressDialog.show();*/
                    LinearLayout buttonLayout = createButtonLayout(l);
                    ImageView mergeButton = (ImageView)buttonLayout.getChildAt(2);
                    addSaveFunctionality(buttonLayout,l);
                    addDiscardItemFunctionality(buttonLayout,l);
                    addDiscardFunctionality(buttonLayout);
                    mergeButton.setImageResource(R.drawable.ic_merge_type_black_24dp);

                    mergeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (checkedBoxes.size() < 2) {
                                Toast.makeText(getActivity(), "Please select atleast two items", Toast.LENGTH_SHORT).show();

                            } else {
                                ArrayList<Integer> selectedBoxes = generateOrderedList(checkedBoxes);//get the position of all checked boxes
                                Log.e(LOG_TAG, selectedBoxes.toString());
                                boolean properFieldSeen = false;
                                boolean invalid = false;
                                StringBuilder item = new StringBuilder("");
                                String price = "";
                                for (int i : selectedBoxes) {
                                    LinearLayout itemLayout = (LinearLayout) l.getChildAt(i);
                                    EditText e = (EditText) itemLayout.getChildAt(1);
                                    if (properFieldSeen) {
                                        if (itemLayout.getChildCount() == 3) {
                                            Toast.makeText(getActivity(), "Cannot merge matched items", Toast.LENGTH_SHORT).show();
                                            invalid = true;
                                        } else {
                                            String individualItem = e.getText().toString().trim();
                                            item.append(" " + individualItem);
                                        }
                                    } else {
                                        if (itemLayout.getChildCount() == 3) {
                                            properFieldSeen = true;
                                            EditText priceEditText = (EditText) itemLayout.getChildAt(2);
                                            price = priceEditText.getText().toString().trim();
                                        }
                                        item.append(e.getText().toString().trim());
                                    }
                                }
                                if (!invalid) {
                                    LinearLayout mergedItemLayout = (LinearLayout) l.getChildAt(selectedBoxes.get(0));
                                    EditText itemEditText = (EditText) mergedItemLayout.getChildAt(1);
                                    itemEditText.setText(item);
                                    EditText priceEditText;
                                    if (mergedItemLayout.getChildCount() == 3) {
                                        priceEditText = (EditText) mergedItemLayout.getChildAt(2);
                                    } else {
                                        priceEditText = new EditText(getActivity());
                                        priceEditText.setBackgroundResource(R.drawable.rounded_edittext);
                                        if (!price.equals("")) {
                                            mergedItemLayout.addView(priceEditText);
                                        }
                                    }
                                    priceEditText.setText(price);
                                    for (int i = 1; i < selectedBoxes.size(); i++) {
                                        l.removeViewAt(selectedBoxes.get(i));
                                        for (int j = i; j < selectedBoxes.size(); j++) {
                                            selectedBoxes.set(j, selectedBoxes.get(j) - 1);
                                        }
                                    }
                                }
                            }
                            checkedBoxes.clear();
                            updatedBoxes(l);
                            for (int i = 1; i < l.getChildCount() - 1; i++) {
                                LinearLayout il = (LinearLayout) l.getChildAt(i);
                                CheckBox c = (CheckBox) il.getChildAt(0);
                                c.setChecked(false);
                            }
                        }


                    });
                    l.addView(buttonLayout);
                }
                Log.e(LOG_TAG, "On iteration: " + Integer.toString(i));
            }
            public LinearLayout createButtonLayout(LinearLayout parent) {
                LinearLayout buttonLayout = new LinearLayout(getActivity());
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonLayout.setWeightSum(4.0f);
                ImageView img;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(100),1.0f);
                for(int i = 0; i < 4; i++) {
                    img = new ImageView(getActivity());
                    img.setLayoutParams(lp);
                    buttonLayout.addView(img);
                }
                return buttonLayout;
            }
            public ArrayList<Integer> generateOrderedList(HashSet<Integer> checkedBoxes) {
                ArrayList<Integer> answer = new ArrayList<Integer>();
                for (int i : checkedBoxes) {
                    answer.add(i);
                }
                Collections.sort(answer);
                return answer;
            }
            public void addDiscardItemFunctionality(LinearLayout buttonLayout, LinearLayout superLayout) {
                ImageView discardImageView = (ImageView)buttonLayout.getChildAt(1);
                discardImageView.setImageResource(R.drawable.ic_remove_black_36dp);
                discardImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<Integer> orderedCheckBoxes = generateOrderedList(checkedBoxes);
                        for (int i = 0; i < orderedCheckBoxes.size(); i++) {
                            int position = orderedCheckBoxes.get(i);
                            l.removeViewAt(position);
                            for (int j = i; j < orderedCheckBoxes.size(); j++) {
                                orderedCheckBoxes.set(j, orderedCheckBoxes.get(j) - 1);
                            }

                        }
                        checkedBoxes.clear();
                        updatedBoxes(l);
                    }
                });
            }
            public void updatedBoxes(LinearLayout l) {
                for (int i = 1; i < l.getChildCount() - 1; i++) {
                    LinearLayout itemLayout = (LinearLayout) l.getChildAt(i);
                    CheckBox c = (CheckBox) itemLayout.getChildAt(0);
                    final int j = i;
                    c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                checkedBoxes.add(j);
                            }
                            else {
                                if(checkedBoxes.contains(j)) {
                                    checkedBoxes.remove(j);
                                }
                            }
                            Log.e(LOG_TAG, checkedBoxes.toString());
                        }
                    });
                }
            }
            public void addDiscardFunctionality(LinearLayout buttonLayout) {
                ImageView discardButton = (ImageView)buttonLayout.getChildAt(0);
                discardButton.setImageResource(R.drawable.ic_cancel_black_48dp);
                discardButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().recreate();
                    }
                });
            }
            public void addSaveFunctionality(LinearLayout buttonLayout, final LinearLayout parent) {
                final LinkedHashMap<String,String> itemPriceMap = new LinkedHashMap<String,String>();
                ImageView saveButton = (ImageView) buttonLayout.getChildAt(3);
                saveButton.setImageResource(R.drawable.ic_check_circle_black_48dp);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String price;
                        String item="";
                        itemPriceMap.clear();
                        for(int i = 1; i < parent.getChildCount()-1;i++) {
                            price=null;
                            item="";
                            LinearLayout itemLayout = (LinearLayout)parent.getChildAt(i);
                            EditText itemText = (EditText)itemLayout.getChildAt(1);
                            item = itemText.getText().toString().trim();
                            if(itemLayout.getChildCount() == 3) {
                                EditText priceText = (EditText)itemLayout.getChildAt(2);
                                price = priceText.getText().toString().trim();
                            }
                            itemPriceMap.put(item,price);
                        }
                        startLoginActivity(itemPriceMap.toString(),ScanType);
                        Log.e(LOG_TAG, itemPriceMap.toString());
                    }
                });

            }
            public void createAddButton(final LinearLayout parent) {
                ImageView addButton = new ImageView(getActivity());
                addButton.setImageResource(R.drawable.ic_add_black_36dp);
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinearLayout itemPriceLayout = new LinearLayout(getActivity());
                        itemPriceLayout.setOrientation(LinearLayout.HORIZONTAL);
                        itemPriceLayout.addView(new CheckBox(getActivity()));
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                        lp.setMargins(0, 0, dpToPx(20), 0);
                        EditText itemEditText = new EditText(getActivity());
                        itemEditText.setBackgroundResource(R.drawable.rounded_edittext);
                        itemEditText.setGravity(Gravity.CENTER);
                        itemEditText.setLayoutParams(lp);
                        EditText priceEditText = new EditText(getActivity());
                        priceEditText.setBackgroundResource(R.drawable.rounded_edittext);
                        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(dpToPx(64),LinearLayout.LayoutParams.WRAP_CONTENT);
                        priceEditText.setGravity(Gravity.CENTER);
                        priceEditText.setPadding(dpToPx(10),dpToPx(10),dpToPx(10),dpToPx(10));
                        priceEditText.setLayoutParams(lp2);
                        itemPriceLayout.addView(itemEditText);
                        itemPriceLayout.addView(priceEditText);
                        parent.addView(itemPriceLayout,parent.getChildCount()-1);
                        updatedBoxes(parent);
                    }
                });
                parent.addView(addButton);
            }
            public String analyzeJSONString(String s) {
                String response = "";
                try {
                    JSONObject rootObject = new JSONObject(s);
                    boolean isError = Boolean.getBoolean(rootObject.getString("IsErroredOnProcessing"));
                    if (isError) {
                        String errorMessage = rootObject.getString("ErrorMessage");
                        String errorDetails = rootObject.getString("ErrorDetails");
                        response = "Error: " + errorMessage;
                    } else {
                        JSONArray parsedResults = rootObject.getJSONArray("ParsedResults");
                        JSONObject redundantaf = parsedResults.getJSONObject(0);
                        String parse = redundantaf.getString("ParsedText");
                        if (!parse.equals("")) {
                            response = parse;
                        } else {
                            String errorMessage = redundantaf.getString("ErrorMessage");
                            String errorDetails = redundantaf.getString("ErrorDetails");
                            response = "Error: " + errorMessage;
                        }
                    }

                } catch (JSONException e) {
                    response = "API not responding correctly.";
                    Log.e(LOG_TAG, e.toString());
                }
                return response;
            }
        });
    }
    public void displayBitmaps(ArrayList<Bitmap> bitmaps, LinearLayout l, LinearLayout.LayoutParams lp) {
        for(Bitmap b : bitmaps) {
            ImageView i = new ImageView(getActivity());
            i.setImageBitmap(b);
            i.setLayoutParams(lp);
            l.addView(i);
        }
    }
    public ArrayList<String> extractItemAndPrice(String s) {
        s = cleanString(s);
        StringBuilder word = new StringBuilder("");
        String price = "";
        StringBuilder item = new StringBuilder("");
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(c == ' ') {
                if(word.toString().contains("$")) {
                    word = new StringBuilder(word.substring(word.indexOf("$")+1));
                }
                try{
                    Double l = Double.parseDouble(word.toString());
                    price = word.toString();
                }
                catch(Exception e) {
                    item.append(word.toString() + " ");
                }
                word = new StringBuilder("");
            }
            else {
                word.append(c);
            }
        }
        ArrayList<String> answer = new ArrayList<String>();
        answer.add(item.toString());
        answer.add(price);
        return answer;
    }
    public String cleanString(String s) {
        StringBuilder clean = new StringBuilder("");
        StringBuilder word = new StringBuilder("");
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(c == ' ' || c == '\n' || c =='\r') {
                if(!word.toString().equals("")) {
                    clean.append(word+" ");
                    word = new StringBuilder("");
                }
            }
            else {
                word.append(c);
            }
        }
        clean.append(word);
        return clean.toString();

    }
        // Create our Preview view and set it as the content of our activity.
    public class ScanReceipt extends AsyncTask<Bitmap,Void,Void> {
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            ArrayList<Bitmap> linedImages = new ArrayList<Bitmap>();
            @Override
            public void onPreExecute() {
                progressDialog.setTitle("Detecting items");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            @Override
            public Void doInBackground(Bitmap... params) {
                Bitmap receiptImage = params[0];
                final File imgPath = saveToExternalStorageString(receiptImage);
                Log.e(GroceryLineBreakMatch.class.getSimpleName(), "Saved to External Storage");
                ArrayList<Integer> lineBreaks = GroceryLineBreakMatch.preProcess(imgPath.getAbsolutePath(), 0);
                for(int i = 0; i < lineBreaks.size()-1;i++) {
                    linedImages.add(Bitmap.createBitmap(receiptImage, 0, lineBreaks.get(i), receiptImage.getWidth(), Math.min(receiptImage.getHeight() - lineBreaks.get(i), lineBreaks.get(i + 1) - lineBreaks.get(i) + 15)));
                }

                return null;
            }
            @Override
            protected void onPostExecute(Void param){
                progressDialog.setTitle("Analyzing");
                LinearLayout l = new LinearLayout(getActivity());
                l.setOrientation(LinearLayout.VERTICAL);
                ScrollView s = new ScrollView(getActivity());
                s.addView(l);
                preview.addView(s);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 10);
                //displayBitmaps(linedImages, l, lp);
                scanBitmaps(linedImages, 0,progressDialog, l,new HashSet<Integer>(),0);
            }
        }
}