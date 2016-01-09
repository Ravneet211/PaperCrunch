package com.example.ravneet.cameratester;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
    public ImageView imageView;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            View v = getView();
            FrameLayout preview = (FrameLayout) v.findViewById(R.id.camera_preview);
            preview.removeView(v.findViewWithTag("Surface"));
            releaseCameraAndPreview();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length,options);
            data = null;
//            imageView = new TouchImageView(getActivity());
//            imageView.setImageBitmap(realImage);
//            RotateTask rotateTask = new RotateTask(imageView,realImage);
//            rotateTask.execute();

            /*File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if(pictureFile.exists()) {
                pictureFile.delete();
            }*/

            /*Bitmap oldBitmap = bitmapImage;
            Matrix matrix = new Matrix();
            matrix.postRotate((float)90);
            bitmapImage = Bitmap.createBitmap(bitmapImage,0,0,bitmapImage.getWidth(),bitmapImage.getHeight(),matrix,false);
            oldBitmap.recycle();*/
            imageView = new ImageView(getActivity());
            realImage = rotateImageIfRequired(realImage,90);

            //imageView.setRotation(90);
//            final JniBitmapHolder jniBitmapHolder = new JniBitmapHolder(realImage);
//            realImage.recycle();
//            jniBitmapHolder.rotateBitmapCcw90();
//            realImage = jniBitmapHolder.getBitmapAndFree();
            imageView.setImageBitmap(realImage);
//            RotateTask rotateTask = new RotateTask(imageView,realImage);
//            rotateTask.execute();
            imageView.setPadding(0, 0, 0, 0);
            imageView.setAdjustViewBounds(true);
            LinearLayout contain = (LinearLayout)preview.getParent();
            contain.removeAllViews();
            contain.addView(imageView);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
            imageView.setLayoutParams(lp);
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
            Button captureButton = (Button) rootView.findViewById(R.id.button_capture);
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
        mtx.postTranslate(-w/2,h/2);
        mtx.postRotate(degree);
        mtx.postTranslate(w / 2, h / 2);
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

}