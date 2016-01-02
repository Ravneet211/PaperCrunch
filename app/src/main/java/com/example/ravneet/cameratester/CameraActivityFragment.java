package com.example.ravneet.cameratester;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * A placeholder fragment containing a simple view.
 */
public class CameraActivityFragment extends Fragment {
    private static final String LOG_TAG = CameraActivityFragment.class.getSimpleName();
    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseCameraAndPreview();
    }



    public CameraActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        releaseCameraAndPreview();
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        if (mCamera != null) {
            mPreview = new CameraPreview(getActivity(), mCamera);
            FrameLayout preview = (FrameLayout) rootView.findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            // Add a listener to the Capture button
            // Add a listener to the Capture button
            mPicture = new CaptureImage();
            Button captureButton = (Button) rootView.findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // get an image from the camera
                            mCamera.takePicture(null, null, mPicture);
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

}