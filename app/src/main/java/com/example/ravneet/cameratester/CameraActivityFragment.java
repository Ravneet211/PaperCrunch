package com.example.ravneet.cameratester;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * A placeholder fragment containing a simple view.
 */
public class CameraActivityFragment extends Fragment {
    private static final String LOG_TAG = CameraActivityFragment.class.getSimpleName();
    private Camera mCamera;
    private CameraPreview mPreview;
    public CameraActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        if(mCamera != null) {
            mPreview = new CameraPreview(getActivity(), mCamera);
            FrameLayout preview = (FrameLayout) rootView.findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }
        return rootView;

    }
    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.e(LOG_TAG,"Error opening camera instance");// Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

}
