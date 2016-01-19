package com.example.ravneet.cameratester;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveId;

import java.util.ArrayList;

/**
 * Created by Ravneet on 1/17/16.
 */
public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {
    private ArrayList<DriveId> mDataset;
    private Context c;
    private String LOG_TAG = DocumentAdapter.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<String> documentLinks;

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
    public DocumentAdapter(Context context, ArrayList<DriveId> myDataset,GoogleApiClient googleApiClient,ArrayList<String> docLinks) {
        mDataset = myDataset;
        c = context;
        mGoogleApiClient = googleApiClient;
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
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(documentLinks.get(position)));
                    c.startActivity(browserIntent);
                }
            }
        });
        final TextView viewImage = (TextView)holder.mCardView.findViewById(R.id.view_image);
        /*viewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open Image View Activity
            }
        });*/ // set value in DocumentFragment
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