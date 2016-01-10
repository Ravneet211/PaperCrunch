package com.example.ravneet.cameratester;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * A placeholder fragment containing a simple view.
 */
public class CameraActivityFragment extends Fragment {
    private static final String LOG_TAG = CameraActivityFragment.class.getSimpleName();
    private Camera mCamera;
    private CameraPreview mPreview;
    public View rootView;
    public FrameLayout preview;
    public com.isseiaoki.simplecropview.CropImageView imageView;
    Button saveButton;
    Button discardButton;
    Button captureButton;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            View v = getView();
            FrameLayout preview = (FrameLayout) v.findViewById(R.id.camera_preview);
            preview.removeView(v.findViewWithTag("Surface"));
            releaseCameraAndPreview();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length,options);
            if(realImage.getWidth() > realImage.getHeight()) {
                realImage = rotateImageIfRequired(realImage, 90);
            }
            realImage = expandBitmap(realImage);
            data = null;
            imageView = new com.isseiaoki.simplecropview.CropImageView(getActivity());
            imageView.setImageBitmap(realImage);
            imageView.setPadding(0, 0, 0, 0);
            imageView.setAdjustViewBounds(true);
            RelativeLayout contain = (RelativeLayout)preview.getParent();
            //Button captureButton = (Button)preview.findViewById(R.id.button_capture);
            contain.removeView(captureButton);
            preview.addView(imageView);
            //Button saveButton = (Button)preview.findViewById(R.id.crop_button);
            saveButton.setVisibility(View.VISIBLE);
            //Button discardButton = (Button)preview.findViewById(R.id.discard_button);
            discardButton.setVisibility(View.VISIBLE);
            //captureButton.setVisibility(View.GONE);
            configureButtons(discardButton,saveButton,imageView);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, 0, 0);
            imageView.setLayoutParams(lp);
            imageView.setCropEnabled(true);
            imageView.setCropMode(CropImageView.CropMode.RATIO_FREE);
            imageView.setInitialFrameScale(0.75f);
//              releaseCameraAndPreview();
//              String filename = "Crop_Image.png";
//              BitmapFactory.Options options = new BitmapFactory.Options();
//              options.inSampleSize = 2;
//              Bitmap realImage = BitmapFactory.decodeByteArray(data,0,data.length,options);
//              try {
//                  FileOutputStream fo = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
//                  realImage.compress(Bitmap.CompressFormat.PNG, 100, fo);
//                  fo.close();
//                  realImage.recycle();
//                  Intent intent = new Intent(getActivity(),CropActivity.class);
//                  intent.putExtra(Intent.EXTRA_STREAM,filename);
//                  startActivity(intent);
//              }
//              catch(Exception e) {
//                Log.v(LOG_TAG,e.toString());
//                //throw new NullPointerException();
//            }


        }

        public static final int MEDIA_TYPE_IMAGE = 1;
        public static final int MEDIA_TYPE_VIDEO = 2;

        /** Create a file Uri for saving an image or video */
        private  Uri getOutputMediaFileUri(int type){
            return Uri.fromFile(getOutputMediaFile(type));
        }

        /** Create a File for saving an image or video */
        private File getOutputMediaFile(int type){
            // To be safe, you should check that the SDCard is mounted
            // using Environment.getExternalStorageState() before doing this.

            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyCameraApp");
            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (! mediaStorageDir.exists()){
                if (! mediaStorageDir.mkdirs()){
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile;
            if (type == MEDIA_TYPE_IMAGE){
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "IMG_"+ timeStamp + ".jpg");
            } else if(type == MEDIA_TYPE_VIDEO) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "VID_"+ timeStamp + ".mp4");
            } else {
                return null;
            }

            return mediaFile;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseCameraAndPreview();
    }



    public CameraActivityFragment() {
    }
    //private ZoomControls zoomControls;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    /*private void enableZoom() {
        zoomControls = new ZoomControls(getActivity());
        zoomControls.setIsZoomInEnabled(true);
        zoomControls.setIsZoomOutEnabled(true);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                zoomCamera(false);

            }
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                zoomCamera(true);
            }
        });
        preview.addView(zoomControls);
    }*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        releaseCameraAndPreview();
        saveButton = (Button)rootView.findViewById(R.id.crop_button);
        //saveButton.setVisibility(View.INVISIBLE);
        discardButton = (Button)rootView.findViewById(R.id.discard_button);
        //discardButton.setVisibility(View.INVISIBLE);
//        Toolbar plz = (Toolbar)getActivity().findViewById(R.id.toolbar);
//        plz.setVisibility(View.GONE);
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        // Create our Preview view and set it as the content of our activity.
        if (mCamera != null) {
            mPreview = new CameraPreview(getActivity(), mCamera);
            preview = (FrameLayout) rootView.findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            //enableZoom();
            mPreview.setTag("Surface");

            // Add a listener to the Capture button
            // Add a listener to the Capture button
            captureButton = (Button) rootView.findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // get an image from the camera
                            if(mCamera != null) {
                                mCamera.takePicture(null, null, mPicture);
                            }
                        }
                    }
            );

        }
        // Add a listener to the Capture button
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
        return c; // returns null if camera is unavailable
    }
    private void releaseCameraAndPreview() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if(mPreview != null){
            mPreview.destroyDrawingCache();
            mPreview.mCamera = null;
        }
    }
    /*public void zoomCamera(boolean zoomInOrOut) {
        if(mCamera!=null) {
            Camera.Parameters parameter = mCamera.getParameters();

            if(parameter.isZoomSupported()) {
                int MAX_ZOOM = parameter.getMaxZoom();
                int currnetZoom = parameter.getZoom();
                if(zoomInOrOut && (currnetZoom <MAX_ZOOM && currnetZoom >=0)) {
                    parameter.setZoom(++currnetZoom);
                }
                else if(!zoomInOrOut && (currnetZoom <=MAX_ZOOM && currnetZoom >0)) {
                    parameter.setZoom(--currnetZoom);
                }
            }
            else
                Toast.makeText(getActivity(), "Zoom Not Avaliable", Toast.LENGTH_LONG).show();

            mCamera.setParameters(parameter);
        }
    }*/
    public Bitmap rotateImageIfRequired(Bitmap bitmap, int degree)
    {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int d = getRotationDegree(bitmap);
        Matrix mtx = new Matrix();
        //mtx.postTranslate(-w/2,h/2);
        mtx.postRotate(degree);
        //mtx.postTranslate(w / 2, h / 2);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
//        iv.setScaleType(ImageView.ScaleType.CENTER);
//        iv.setImageBitmap(resizedBitmap);
    }
    public int getRotationDegree(Bitmap bitmap) {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        int rotationInDegrees = 0;
        if (pictureFile == null){
            Log.d(LOG_TAG, "Error creating media file, check storage permissions");
            return 0;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            ExifInterface exif = new ExifInterface(pictureFile.getPath());
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            rotationInDegrees = exifToDegrees(rotation);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
        }
        return rotationInDegrees;
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90;
            }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270;
        }
        return 0;
    }
    public Bitmap expandBitmap(Bitmap bitmap) {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
//        Toolbar plz = (Toolbar)getActivity().findViewById(R.id.toolbar);
//        plz.setVisibility(View.GONE);
        int height = getActivity().getWindow().getDecorView().getHeight();
        Rect rectangle= new Rect();
        Window window= getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight= rectangle.top;
        height-=statusBarHeight;
        //View decorView = getActivity().getWindow().getDecorView();
// Hide the status bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
//        final TypedArray styledAttributes = getContext().getTheme().obtainStyledAttributes(
//                new int[] { android.R.attr.actionBarSize });
//        int mActionBarSize = (int) styledAttributes.getDimension(0, 0);
//        height-=mActionBarSize;
//        styledAttributes.recycle();

        return Bitmap.createScaledBitmap(bitmap,width,height,false);
    }
    public void configureButtons(Button discardButton, final Button cropButton, final CropImageView cropImageView) {
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap croppedImage = cropImageView.getCroppedBitmap();
                expandBitmap(croppedImage);
                cropImageView.setImageBitmap(croppedImage);
            }
        });
    }

}