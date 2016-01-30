package com.example.ravneet.cameratester;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
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

import java.util.ArrayList;

/**
 * Created by Ravneet on 1/26/16.
 */

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {
    private ArrayList<DriveId> mDataset;
    private Context c;
    private String LOG_TAG = BillAdapter.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<DriveId> imageIds;

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
    public BillAdapter(Context context, ArrayList<DriveId> myDataset, GoogleApiClient googleApiClient, ArrayList<DriveId> imgLinks) {
        mDataset = myDataset;
        c = context;
        mGoogleApiClient = googleApiClient;// ensure that this doesn't fail
        imageIds = imgLinks;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BillAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bill_cards_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ImageView billImage = (ImageView)holder.mCardView.findViewById(R.id.image_placeholder);
        TextView billButton = (TextView)holder.mCardView.findViewById(R.id.view_receipt);
        billButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent billIntent = new Intent(c,BillDisplayActivity.class);
                billIntent.putExtra("Bill_Drive_ID",mDataset.get(position).encodeToString());
                c.startActivity(billIntent);
            }
        });
        Glide.with(c)
                .from(DriveId.class)
                .load(imageIds.get(position))
                .into(billImage);
        ImageView deleteButton = (ImageView) holder.mCardView.findViewById(R.id.delete_receipt);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(c);
                alertDialogBuilder.setMessage(R.string.dialog_message)
                        .setTitle(R.string.dialog_title).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mGoogleApiClient.isConnected()) {
                            DriveFile hashMapFile = mDataset.get(position).asDriveFile();
                            hashMapFile.delete(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    if (status.isSuccess()) {
                                        mDataset.remove(position);
                                        Log.e(LOG_TAG,"Deleted hashmap file");
                                        imageIds.get(position).asDriveFile().delete(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(Status status) {
                                                if (status.isSuccess()) {
                                                    imageIds.remove(position);
                                                    Log.e(LOG_TAG, "Image File deleted");
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
        TextView viewBillImage = (TextView)holder.mCardView.findViewById(R.id.view_bill_image);
        viewBillImage.setClickable(true);
        viewBillImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, ImageDisplayActivity.class);
                i.putExtra(Intent.EXTRA_TEXT,imageIds.get(position).encodeToString());
                c.startActivity(i);
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Log.e(LOG_TAG,Integer.toString(mDataset.size()));
        return mDataset.size();
    }
}
