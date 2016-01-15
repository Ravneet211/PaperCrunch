package com.example.ravneet.cameratester;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.internal.ScrimInsetsFrameLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    private WeakReference<Activity> activityWeakReference = new WeakReference<Activity>(this);
    private GoogleApiClient mGoogleApiClient;
    private ImageView profileImageView;
    private GoogleSignInAccount account;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activityWeakReference.get(), CameraActivity.class);
                startActivity(intent);

            }
        });
        ArrayList<String> a = new ArrayList<String>();
        a.add("My Bills");
        a.add("My Scanned Documents");
        a.add("Logout");
        final ScrimInsetsFrameLayout s = (ScrimInsetsFrameLayout) findViewById(R.id.left_drawer_container);
        final ListView l = (ListView) findViewById(R.id.left_drawer);
        final FragmentManager fragmentManager = getFragmentManager();
        final android.app.Fragment billFragment = new BillFragment();
        final Fragment documentFragment = new DocumentFragment();
        fragmentManager.beginTransaction().replace(R.id.main_frame, billFragment)
                .commit();
        l.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    fragmentManager.beginTransaction().replace(R.id.main_frame, billFragment)
                            .commit();

                }
                if (position == 1) {
                    fragmentManager.beginTransaction().replace(R.id.main_frame,documentFragment).commit();
                }
                if (position == 2) {
                    Intent intent = new Intent(activityWeakReference.get(), SignInActivityWithDrive.class);
                    intent.putExtra("Parent Activity", LOG_TAG);
                    startActivity(intent);
                }
                l.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(s);

            }
        });
        CustomListAdapter  arrayAdapter = new CustomListAdapter(this, R.layout.nav_drawer_rowview, R.id.nav_linear_layout, a);
        l.setAdapter(arrayAdapter);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mDrawerLayout.setStatusBarBackground(R.color.colorAccent);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);

// Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PROFILE),new Scope(Scopes.DRIVE_APPFOLDER))
                .requestProfile()
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        profileImageView = (ImageView)findViewById(R.id.profile_image);
        if(profileImageView == null) {
            Log.e(LOG_TAG,"View not found");
        }
        signIn();
    }

    protected void signIn() {

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(LOG_TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    Log.d(LOG_TAG,"Kinda cached lol");
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }
    public void handleSignInResult(GoogleSignInResult result) {
        if(result.isSuccess()) {
            account = result.getSignInAccount();
            Picasso.with(this).load(account.getPhotoUrl()).transform(new CircleTransform()).into(profileImageView);
            String personName = account.getDisplayName();
            String personEmail = account.getEmail();
            TextView displayName = (TextView)findViewById(R.id.profile_display_name);
            displayName.setText(personName);
            TextView userEmail = (TextView)findViewById(R.id.profile_email);
            userEmail.setText(personEmail);
            String personId = account.getId();
            Uri personPhoto = account.getPhotoUrl();
            if(personPhoto == null) {
                Log.e(LOG_TAG,"Unable to retrieve profile info");
            }
            else {
                Log.e(LOG_TAG,personPhoto.toString());
            }
            Log.e(LOG_TAG,personName);
            Log.v(LOG_TAG, account.getEmail());
            Log.v(LOG_TAG,personId);
        }
        else {
            Intent signInIntent = new Intent(this,SignInActivityWithDrive.class);
            signInIntent.putExtra("Parent Activity",LOG_TAG);
            startActivity(signInIntent);

        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.v(LOG_TAG,result.toString());
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
            // If the nav drawer is open, hide action items related to the content view
            /*boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
            menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);*/
            return super.onPrepareOptionsMenu(menu);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
