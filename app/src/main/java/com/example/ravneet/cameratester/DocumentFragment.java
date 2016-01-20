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
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;

import java.io.InputStream;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DocumentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DocumentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DocumentFragment extends android.app.Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final String LOG_TAG = DocumentFragment.class.getSimpleName();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ArrayList<DriveId> timeStamps = new ArrayList<DriveId>();
    ProgressDialog progressDialog;
    View rootView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<DriveId> docLinks = new ArrayList<DriveId>();


    private OnFragmentInteractionListener mListener;

    public DocumentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DocumentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DocumentFragment newInstance(String param1, String param2) {
        DocumentFragment fragment = new DocumentFragment();
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
    public void onConnected(Bundle connectionHint) {
        Log.e(LOG_TAG, "Successful YAYY!");
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.show();
        DocumentRetrieve documentRetrieve = new DocumentRetrieve();
        documentRetrieve.execute(rootView);
        Glide.get(getActivity()).register(DriveId.class, InputStream.class, new DriveIdModelLoader.Factory(mGoogleApiClient));
        //loadInformation();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView = (View)inflater.inflate(R.layout.fragment_document, container, false);
        mRecyclerView = (RecyclerView) rootView;
        mLayoutManager = new LinearLayoutManager(getActivity());
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
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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
    public class DocumentRetrieve extends AsyncTask<View,Void,Void> {
        @Override
        protected Void doInBackground(View... params) {
            DriveFolder rootFolder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
            Query documentFolderQuery = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE, "OCRDocuments2"))
                    .build();
            MetadataBuffer metadataBuffer = Drive.DriveApi.getRootFolder(mGoogleApiClient).queryChildren(mGoogleApiClient, documentFolderQuery).await().getMetadataBuffer();
            DriveFolder documentsFolder = null;
            if(metadataBuffer == null) {
                Log.e(LOG_TAG,"Error getting MetaDataBuffer");
                return null;
            }
            if(metadataBuffer.getCount() == 0) {
                Log.e(LOG_TAG,"Empty");
            }
            for(Metadata md:metadataBuffer) {
                if(md!=null && md.isDataValid()) {
                    if(md.getTitle() != null) {
                        documentsFolder = md.getDriveId().asDriveFolder();
                    }
                }
            }
            if(documentsFolder == null) {
                return null;
            }
            timeStamps = generateArrayList(documentsFolder);

            Log.e(LOG_TAG, "Images: " + timeStamps.toString());
            return null;
        }

        @Override
        public void onPreExecute() {
            progressDialog.setTitle("Loading");
            progressDialog.show();
        }
        @Override
        protected void onPostExecute(Void nothing) {
            progressDialog.dismiss();
            mAdapter = new DocumentAdapter(getActivity(),timeStamps,mGoogleApiClient,docLinks);
            mRecyclerView.setAdapter(mAdapter);
            Toolbar plz = (Toolbar)getActivity().findViewById(R.id.toolbar);
            mRecyclerView.setPadding(0,plz.getHeight(),0,0);
            Log.e(LOG_TAG,"DocLinks:" + docLinks.toString());
        }
        public ArrayList<DriveId> generateArrayList(DriveFolder documentsFolder) {
            ArrayList<DriveId> temp = new ArrayList<DriveId>();
            SortOrder sortOrder = new SortOrder.Builder()
                    .addSortDescending(SortableField.CREATED_DATE).build();

            Query sortedQuery = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.MIME_TYPE, "image/jpeg"))
                    .setSortOrder(sortOrder).build();
            MetadataBuffer metadataBuffer = documentsFolder.queryChildren(mGoogleApiClient, sortedQuery).await().getMetadataBuffer();
            if(metadataBuffer != null) {
                for(Metadata md : metadataBuffer){
                    Log.e(LOG_TAG,md.getTitle());
                    if(!md.isTrashed()) {
                        Log.e(LOG_TAG, "Explicitly trashed "+Boolean.toString(md.isExplicitlyTrashed()));
                        Log.e(LOG_TAG,"Trashable: " + Boolean.toString(md.isTrashable()));
                        temp.add(md.getDriveId());


                        if(md.getCustomProperties() != null) {
                            Log.e(LOG_TAG, md.getCustomProperties().toString());
                            if(md.getCustomProperties().containsKey(new CustomPropertyKey("timestamp",CustomPropertyKey.PUBLIC))) {
                                CustomPropertyKey customPropertyKey = new CustomPropertyKey("timestamp",CustomPropertyKey.PUBLIC);
                                String timestamp = md.getCustomProperties().get(customPropertyKey);
                                Query doclinksQuery = new Query.Builder().addFilter(Filters.eq(customPropertyKey,timestamp)).
                                        addFilter(Filters.eq(SearchableField.MIME_TYPE, "application/msword")).
                                        build();
                                MetadataBuffer documentBuffer = documentsFolder.queryChildren(mGoogleApiClient,doclinksQuery).await().getMetadataBuffer();
                                if(documentBuffer != null) {
                                    for (Metadata md2 : documentBuffer) {
                                        if(md2.getAlternateLink() != null && !md2.isTrashed()) {
                                            docLinks.add(md2.getDriveId());
                                        }
                                    }
                                }

                            }
                        }


                    }
//

                }
            }
            return temp;
        }
        public String extractFileID(String s) {
            int slashes = 0;
            StringBuilder fileID = new StringBuilder("");
            for(int i = 0; i < s.length(); i++) {

                char c = s.charAt(i);
                if(c == '/') {
                    slashes++;
                }
                if(slashes == 5) {
                    fileID.append(c);
                }
                if(slashes > 5) {
                    break;
                }
            }
            return fileID.substring(1).toString();
        }
    }
}
