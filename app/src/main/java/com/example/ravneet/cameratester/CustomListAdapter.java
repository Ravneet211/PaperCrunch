package com.example.ravneet.cameratester;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ravneet on 1/15/16.
 */
public class CustomListAdapter  extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;
    private final int layoutResourceID;

    public CustomListAdapter(Context context, int viewresourceID, int view, List<String> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        layoutResourceID = viewresourceID;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout rowView = (LinearLayout)inflater.inflate(layoutResourceID,parent,false);
        ImageView icon = (ImageView) rowView.findViewById(R.id.list_item_icon);
        TextView listText = (TextView)rowView.findViewById(R.id.list_item_content);
        if(position == 0) {
            icon.setImageResource(R.drawable.ic_receipt_black_24dp);
            listText.setText(values.get(0));
        }
        if(position == 1) {
            icon.setImageResource(R.drawable.ic_description_black_24dp);
            listText.setText(values.get(1));
        }
        if(position == 2) {
            icon.setImageResource(R.mipmap.ic_logout_icon);
            listText.setText(values.get(2));
        }
        return rowView;
    }
}
