package com.example.ravneet.cameratester;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BillFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BillFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BillFragment extends android.app.Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private View rootView;
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private final ArrayList<HashMap<String,String>> billContents = new ArrayList<HashMap<String, String>>();
    private static final String LOG_TAG = BillFragment.class.getSimpleName();
    // TODO: Rename and change types of parameters
    ProgressDialog progressDialog;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ArrayList<DriveId> imageFiles = new ArrayList<DriveId>();
    private ArrayList<DriveId> billFiles = new ArrayList<DriveId>();
    private ArrayList<DriveId> fileArray = new ArrayList<DriveId>();
    private OnFragmentInteractionListener mListener;

    public BillFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BillFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BillFragment newInstance(String param1, String param2) {
        BillFragment fragment = new BillFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = (View)inflater.inflate(R.layout.fragment_document, container, false);
        mRecyclerView = (RecyclerView) rootView;
        mLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e(LOG_TAG, "Successful YAYY!");
        ReceiptRetrieve receiptRetrieve = new ReceiptRetrieve();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Glide.get(getActivity()).register(DriveId.class, InputStream.class, new DriveIdModelLoader.Factory(mGoogleApiClient));
        receiptRetrieve.execute();//loadInformation();
    }
    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(LOG_TAG, "GoogleApiClient connection suspended");
        Glide.get(getActivity()).unregister(DriveId.class, InputStream.class);
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(LOG_TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(LOG_TAG, "Exception while starting resolution activity", e);
        }
    }

    public class ReceiptRetrieve extends AsyncTask<Void,Void,Void> {
        @Override
        public Void doInBackground(Void... params) {
            generateArrayLists();
            return null;
        }
        public void generateArrayLists() {
            SortOrder sortOrder = new SortOrder.Builder()
                    .addSortDescending(SortableField.CREATED_DATE).build();

            Query imageSortedQuery = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.MIME_TYPE, "image/jpeg"))
                    .setSortOrder(sortOrder).build();
            Query billSortedQuery = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                    .setSortOrder(sortOrder).build();
            MetadataBuffer metadataBuffer = Drive.DriveApi.getAppFolder(mGoogleApiClient).queryChildren(mGoogleApiClient, imageSortedQuery).await().getMetadataBuffer();
            if (metadataBuffer != null) {
                for (Metadata md : metadataBuffer) {
                    Log.e(LOG_TAG, md.getTitle());
                    if (!md.isTrashed()) {
                        imageFiles.add(md.getDriveId());
                    }
                }
            }
            MetadataBuffer metadataBuffer1 = Drive.DriveApi.getAppFolder(mGoogleApiClient).queryChildren(mGoogleApiClient, billSortedQuery).await().getMetadataBuffer();
            if (metadataBuffer1 != null) {
                for (Metadata md : metadataBuffer1) {
                    Log.e(LOG_TAG, md.getTitle());
                    if (!md.isTrashed()) {
                        billFiles.add(md.getDriveId());
                    }
                }
            }
            /*for (DriveId d : billFiles) {
                try {
                    DriveApi.DriveContentsResult driveContentsResult = d.asDriveFile().open(mGoogleApiClient,DriveFile.MODE_READ_ONLY,null).await();
                    DriveContents driveContents = driveContentsResult.getDriveContents();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(driveContents.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    billContents.add(convertToHashMap(builder));
                    reader.close();
                }
                catch(IOException e) {
                    Log.e(LOG_TAG,"Problem reading from drive file");
                }
            }
            Log.e(LOG_TAG,billContents.toString());*/
        }
        @Override
        protected void onPostExecute(Void yolo) {
            //Log.e(LOG_TAG,"Image Files ArrayList : " + imageFiles.toString());
            //Log.e(LOG_TAG,"Bill Files ArrayList : "+ billFiles.toString());
            mAdapter = new BillAdapter(getActivity(),billFiles,mGoogleApiClient,imageFiles);
            mRecyclerView.setAdapter(mAdapter);
            Toolbar plz = (Toolbar)getActivity().findViewById(R.id.toolbar);
            mRecyclerView.setPadding(0,plz.getHeight(),0,0);
            progressDialog.dismiss();
        }
        private HashMap<String,String> convertToHashMap(StringBuilder s) {
            HashMap<String,String> answer = new HashMap<String,String>();
            String k = s.toString();
            if(k.equals("{}")) {
                return answer;
            }
            s = new StringBuilder(s.substring(1,s.indexOf("}")));
            boolean itemFill = true;
            StringBuilder item = new StringBuilder("");
            StringBuilder price = new StringBuilder("");
            for(int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if(c == '=') {
                    itemFill = false;
                }
                else if(itemFill) {
                    if(!price.toString().equals("")) {
                        if(price.equals("null")) {
                            answer.put(item.toString(),null);
                        }
                        else {
                            answer.put(item.toString(),price.toString());
                        }
                        price = new StringBuilder("");
                        item = new StringBuilder("");
                    }
                    item.append(c);
                }
                else {
                    if (c == ',') {
                        itemFill = true;
                    }
                    else {
                        price.append(c);
                    }
                }
            }
            answer.put(item.toString(),price.toString());
            //Log.e(LOG_TAG,answer.toString());
            return answer;
        }
    }
}
