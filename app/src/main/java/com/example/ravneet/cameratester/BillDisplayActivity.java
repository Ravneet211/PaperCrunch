package com.example.ravneet.cameratester;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.ivbaranov.mli.MaterialLetterIcon;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BillDisplayActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private WeakReference<Activity> billDisplayActivityWeakReference = new WeakReference<Activity>(this);
    private final String LOG_TAG = BillDisplayActivity.class.getSimpleName();
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private LinearLayout rootLayout;
    private HashMap<String,Integer> letterColorHashMap = new HashMap<String,Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_display);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(billDisplayActivityWeakReference.get(),CameraActivity.class);
                cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(cameraIntent);
            }
        });
        rootLayout = (LinearLayout) findViewById(R.id.receipt_linear_layout);
        ScrollView s = (ScrollView)findViewById(R.id.receipt_scroll);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e(LOG_TAG, "Successful YAYY!");
        loadBillLayout();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(LOG_TAG, "GoogleApiClient connection suspended");
        Glide.get(this).unregister(DriveId.class, InputStream.class);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(LOG_TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(LOG_TAG, "Exception while starting resolution activity", e);
        }
    }

    private void loadBillLayout() {
        String encodedBillDriveId = getIntent().getStringExtra("Bill_Drive_ID");
        DriveId billDriveId = DriveId.decodeFromString(encodedBillDriveId);
        billDriveId.asDriveFile().getMetadata(mGoogleApiClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
            @Override
            public void onResult(DriveResource.MetadataResult metadataResult) {
                if(metadataResult.getStatus().isSuccess()) {

                }
            }
        });
        billDriveId.asDriveFile().open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                if (driveContentsResult.getStatus().isSuccess()) {
                    try {
                        DriveContents billContents = driveContentsResult.getDriveContents();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(billContents.getInputStream()));
                        StringBuilder builder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                        reader.close();
                        createBillLayout(convertToHashMap(builder));
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Problem reading from drive file");
                    }
                }
            }

            private LinkedHashMap<String, String> convertToHashMap(StringBuilder s) {
                LinkedHashMap<String, String> answer = new LinkedHashMap<String, String>();
                String k = s.toString();
                if (k.equals("{}")) {
                    return answer;
                }
                s = new StringBuilder(s.substring(1, s.indexOf("}")));
                boolean itemFill = true;
                StringBuilder item = new StringBuilder("");
                StringBuilder price = new StringBuilder("");
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (c == '=') {
                        itemFill = false;
                    } else if (itemFill) {
                        if (!price.toString().equals("")) {
                            if (price.toString().equals("null")) {
                                answer.put(item.toString(), null);
                            } else {
                                Log.e(LOG_TAG, price.toString());
                                answer.put(item.toString(), price.toString());
                            }
                            price = new StringBuilder("");
                            item = new StringBuilder("");
                        }
                        item.append(c);
                    } else {
                        if (c == ',') {
                            itemFill = true;
                        } else {
                            price.append(c);
                        }
                    }
                }
                answer.put(item.toString(), price.toString());
                Log.e(LOG_TAG, answer.toString());
                return answer;
            }

        });
    }

    private void createBillLayout(LinkedHashMap<String, String> billMap) {
        /*boolean displayItemPriceLayout = true;
        ViewStub v = new ViewStub(this);
        v.setLayoutResource(R.layout.item_price_layout);
        LinearLayout.LayoutParams lonelyItem = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams pairedItem = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);
        LinearLayout itemPriceLayout;
        for (String s : billMap.keySet()) {
            if (billMap.get(s) == null) {
                if (!displayItemPriceLayout) {
                    displayItemPriceLayout = true;
                }
                TextView t = new TextView(this);
                t.setText(s);
                t.setMinHeight(dpToPx(18));
                t.setLayoutParams(lonelyItem);
                t.setGravity(Gravity.CENTER);
                t.setTypeface(null, Typeface.BOLD);
                rootLayout.addView(t);
            } else {
                if (displayItemPriceLayout) {
                    rootLayout.addView(v);
                    v.inflate();
                    displayItemPriceLayout = false;
                }
                itemPriceLayout = new LinearLayout(this);
                itemPriceLayout.setLayoutParams(lonelyItem);
                itemPriceLayout.setWeightSum(1.0f);
                itemPriceLayout.setMinimumHeight(dpToPx(18));
                itemPriceLayout.setOrientation(LinearLayout.HORIZONTAL);
                TextView itemLayout = new TextView(this);
                itemLayout.setGravity(Gravity.CENTER);
                itemLayout.setText(s);
                itemLayout.setLayoutParams(pairedItem);
                TextView priceLayout = new TextView(this);
                priceLayout.setText(billMap.get(s));
                priceLayout.setLayoutParams(pairedItem);
                priceLayout.setGravity(Gravity.CENTER);
                itemPriceLayout.addView(itemLayout);
                itemPriceLayout.addView(priceLayout);
                rootLayout.addView(itemPriceLayout);
            }

        }*/
        generateLetterColorHashMap();
        LinearLayout rootLinearLayout = (LinearLayout)findViewById(R.id.receipt_linear_layout);
        CardView content;
        LinearLayout contentLayout;
        for(String s : billMap.keySet()) {
            content = new CardView(this);
            contentLayout = new LinearLayout(this);
            if(billMap.get(s) == null || billMap.get(s).equals("null")) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                TextView t = new TextView(this);
                t.setText(s);
                t.setGravity(Gravity.CENTER);
                t.setLayoutParams(lp);
                t.setMinHeight(dpToPx(60));
                t.setTypeface(null,Typeface.BOLD);
                contentLayout.addView(t);
            }
            else {
                contentLayout.setWeightSum(1.0f);
                String letter = extractFirstLetter(s);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams materialIconParameters = new LinearLayout.LayoutParams(dpToPx(42),LinearLayout.LayoutParams.MATCH_PARENT);
                TextView itemText = new TextView(this);
                itemText.setGravity(Gravity.CENTER_VERTICAL);
                itemText.setText(s);
                itemText.setMinHeight(dpToPx(60));
                itemText.setLayoutParams(lp);
                lp2.setMargins(0,0,dpToPx(6),0);
                TextView priceText = new TextView(this);
                priceText.setGravity(Gravity.CENTER);
                priceText.setText(billMap.get(s));
                priceText.setLayoutParams(lp2);
                Log.e(LOG_TAG,"Letter : "+ letter);

                MaterialLetterIcon icon = new MaterialLetterIcon.Builder(this) //
                        .shapeColor(letterColorHashMap.get(letter))
                        .shapeType(MaterialLetterIcon.SHAPE_CIRCLE)
                        .letter(letter)
                        .letterColor(getResources().getColor(R.color.white))
                        .letterSize(18)
                        .lettersNumber(1)
                        .letterTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/roboto/Roboto-Light.ttf"))
                        .initials(false)
                        .initialsNumber(2)
                        .create();
                materialIconParameters.setMargins(dpToPx(6),0,dpToPx(6),0);
                icon.setLayoutParams(materialIconParameters);
                contentLayout.addView(icon);
                priceText.setMinHeight(dpToPx(60));
                contentLayout.addView(itemText);
                contentLayout.addView(priceText);
            }
            content.addView(contentLayout);
            LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLayoutParams.setMargins(0,dpToPx(4),0,dpToPx(4));
            content.setLayoutParams(cardLayoutParams);
            rootLinearLayout.addView(content);
        }

    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
    public String extractFirstLetter(String s) {
        StringBuilder firstLetter = new StringBuilder("");
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if((c >= 'A' && c <='Z') || (c >='a' && c<='z')) {
                firstLetter.append(c);
                break;
            }
        }
        return firstLetter.toString();
    }
    public void generateLetterColorHashMap() {
        char c = 'A';
        while(c <= 'Z') {
            if(c%5==0) {
                letterColorHashMap.put(Character.toString(c),getResources().getColor(R.color.red_letter));
                c+=32;
                letterColorHashMap.put(Character.toString(c),getResources().getColor(R.color.red_letter));
            }
            else if(c%5==1) {
                letterColorHashMap.put(Character.toString(c),getResources().getColor(R.color.green_letter));
                c+=32;
                letterColorHashMap.put(Character.toString(c),getResources().getColor(R.color.green_letter));
            }
            else if(c%5==2) {
                letterColorHashMap.put(Character.toString(c),getResources().getColor(R.color.blue_letter));
                c+=32;
                letterColorHashMap.put(Character.toString(c),getResources().getColor(R.color.blue_letter));
            }
            else if(c%5==3) {
                letterColorHashMap.put(Character.toString(c),getResources().getColor(R.color.yellow_letter));
                c+=32;
                letterColorHashMap.put(Character.toString(c),getResources().getColor(R.color.yellow_letter));
            }
            else {
                letterColorHashMap.put(Character.toString(c),getResources().getColor(R.color.gray_letter));
                c+=32;
                letterColorHashMap.put(Character.toString(c),getResources().getColor(R.color.gray_letter));
            }
            c-=32;
            c++;
        }

    }
}
