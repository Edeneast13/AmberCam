package com.ambercam.android.camera2basic.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ambercam.android.camera2basic.MenuElement;
import com.ambercam.android.camera2basic.R;
import com.ambercam.android.camera2basic.adapter.MenuAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    private ListView mMenuListView;
    private Toolbar mToolbar;
    private ArrayList<MenuElement> mMenuElementList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    /**
     * LIFE CYCLE methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initializeViews();
        setToolbarBehavior(mToolbar);

        mAuth = FirebaseAuth.getInstance();
        setFirebaseAuthListener();

        setMenuElementList();

        updateUI();

        setMenuListViewListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    /**
     * initialize views for the activity
     */
    public void initializeViews(){
        mMenuListView = (ListView)findViewById(R.id.menu_list);
        mToolbar = (Toolbar)findViewById(R.id.menu_toolbar);
    }

    /**
     * handles toolbar behavior
     */
    public void setToolbarBehavior(Toolbar toolbar){
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.app_title));
    }

    /**
     * listener for firebase authentication
     */
    public void setFirebaseAuthListener(){
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
            }
        };
    }

    /**
     * sign the user out of the app and return to get started activity
     */
    public void logOut(){
        FirebaseAuth.getInstance().signOut();
        Intent logoutActivity = new Intent(getApplicationContext(), GetStartedActivity.class);
        startActivity(logoutActivity);
    }

    /**
     * listener for the menu list view
     */
    public void setMenuListViewListener(){
        mMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        cloudUsageIntent();
                        break;
                    case 1:
                        sendFeedBack();
                        break;
                    case 2:
                        //openPlayStore(getApplicationContext());
                        break;
                    case 3:
                        //TODO: Settings Activity
                        break;
                    case 4:
                        logOut();
                        break;
                }
            }
        });
    }

    /**
     * set menu elements to be passed into the menu adapter
     */
    public void setMenuElementList(){
        MenuElement usageElement = new MenuElement(
                getString(R.string.menu_element_usage), R.drawable.ic_cloud_black_48dp);
        mMenuElementList.add(usageElement);

        MenuElement feedbackElement = new MenuElement(
                getString(R.string.menu_element_feedback), R.drawable.ic_feedback_black_24dp);
        mMenuElementList.add(feedbackElement);

        MenuElement rateElement = new MenuElement(
                getString(R.string.menu_element_rate_app), R.drawable.ic_star_rate_black_18dp);
        mMenuElementList.add(rateElement);

        MenuElement settingsElement = new MenuElement(
                getString(R.string.menu_element_settings), R.drawable.ic_settings_black_24dp);
        mMenuElementList.add(settingsElement);

        MenuElement logOutElement = new MenuElement(
                getString(R.string.menu_element_log_out), R.drawable.ic_power_settings_new_black_24dp);
        mMenuElementList.add(logOutElement);
    }

    /**
     * updates the listview with current menu elements
     */
    public void updateUI(){
        MenuAdapter menuAdapter = new MenuAdapter(getApplicationContext(), mMenuElementList);
        mMenuListView.setAdapter(menuAdapter);
    }

    /**
     * opens the play store using an intent so users can rate the app
     */
    public void openPlayStore(Context context){
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, uri);

        //allows for us to return to app from play store using back button
        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        try{
            startActivity(playStoreIntent);
        }
        catch (ActivityNotFoundException e){
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.getPackageName())));
        }
    }

    /**
     * allows the user to chose there favorite email app and send feedback to the development team
     */
    public void sendFeedBack(){
        Intent feedbackIntent = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode("appdevbri@gmail.com") +
                "?subject=" + Uri.encode("AmberCam Feedback");

        Uri uri = Uri.parse(uriText);
        feedbackIntent.setData(uri);

        //allows for us to return to app from play store using back button
        feedbackIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        startActivity(Intent.createChooser(feedbackIntent, "Send mail......"));
    }

    public void cloudUsageIntent(){
        Intent usageIntent = new Intent(getApplicationContext(), UsageActivity.class);
        startActivity(usageIntent);
    }
}
