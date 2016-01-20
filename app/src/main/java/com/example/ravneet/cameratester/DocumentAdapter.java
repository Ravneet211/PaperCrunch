package com.example.ravneet.cameratester;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;

import java.util.ArrayList;

/**
 * Created by Ravneet on 1/17/16.
 */
public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {
    private ArrayList<DriveId> mDataset;
    private Context c;
    private String LOG_TAG = DocumentAdapter.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<DriveId> documentLinks;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView mCardView;
        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public DocumentAdapter(Context context, ArrayList<DriveId> myDataset,GoogleApiClient googleApiClient,ArrayList<DriveId> docLinks) {
        mDataset = myDataset;
        c = context;
        mGoogleApiClient = googleApiClient;// ensure that this doesn't fail
        documentLinks = docLinks;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DocumentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        CardView v = (CardView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.documents_card_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final ImageView docImage = (ImageView) holder.mCardView.findViewById(R.id.doc_pic);
        final TextView viewDoc = (TextView)holder.mCardView.findViewById(R.id.view_document);
        viewDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(documentLinks.size() > position) {
                    final Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    DriveId docDriveId = documentLinks.get(position);
                    DriveFile docFile = docDriveId.asDriveFile();
                    docFile.getMetadata(mGoogleApiClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                        @Override
                        public void onResult(DriveResource.MetadataResult metadataResult) {
                            if (metadataResult != null) {
                                Metadata md = metadataResult.getMetadata();
                                browserIntent.setData(Uri.parse(md.getAlternateLink()));
                                c.startActivity(browserIntent);
                            }
                        }
                    });
                }
            }
        });
        final TextView viewImage = (TextView)holder.mCardView.findViewById(R.id.view_image);
        viewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c,ImageDisplayActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT,mDataset.get(position).encodeToString());
                c.startActivity(intent);//Open Image View Activity
            }
        });
        /*DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, mDataset.get(position));
        driveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(
                        new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(DriveApi.DriveContentsResult result) {
                                if (!result.getStatus().isSuccess()) {
                                    // Handle an error
                                }
                                DriveContents driveContents = result.getDriveContents();
                                InputStream is = driveContents.getInputStream();
                                Bitmap bitmap = BitmapFactory.decodeStream(is);
                                // Do something with the bitmap
                                docImage.setImageBitmap(bitmap);
                                // Don't forget to close the InputStream
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    Log.e(LOG_TAG, "Error Closing input stream");
                                }
                            }
                        });*/
        //Picasso.with(c).load(Uri.parse("https://drive.google.com/uc?export=view&id="+mDataset.get(position))).into(docImage);
        ImageView deleteButton = (ImageView) holder.mCardView.findViewById(R.id.delete_icon);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(c);
                alertDialogBuilder.setMessage(R.string.dialog_message)
                        .setTitle(R.string.dialog_title).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mGoogleApiClient.isConnected()) {
                            DriveFile imageFile = mDataset.get(position).asDriveFile();
                            imageFile.delete(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    if(status.isSuccess()) {
                                        mDataset.remove(position);
                                        documentLinks.get(position).asDriveFile().delete(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(Status status) {
                                                if(status.isSuccess()) {
                                                    documentLinks.remove(position);
                                                    Log.e(LOG_TAG,"Doc file deleted");
                                                    notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                //Drive.DriveApi.getFile(mGoogleApiClient,mDataset.get(position)).delete(mGoogleApiClient);
            }
        });
        Glide.with(c)
                .from(DriveId.class)
                .load(mDataset.get(position))
                .into(docImage);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}