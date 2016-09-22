package com.ambercam.android.camera2basic.ui;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.ambercam.android.camera2basic.CloudImage;
import com.ambercam.android.camera2basic.adapter.GalleryAdapter;
import com.ambercam.android.camera2basic.R;
import com.ambercam.android.camera2basic.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity {

    private ImageButton mCameraButton;
    private ImageButton mMenuButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mActiveUser;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ArrayList<String> mFirebaseDataList = new ArrayList<String>();
    private ArrayList<String> mImageUrlList = new ArrayList<>();
    private GridView mGalleryGridView;
    private Drawer mDrawer;

    /**
     * Life Cycle methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        if(savedInstanceState == null){
            initializeViews();

            mAuth = FirebaseAuth.getInstance();

            //firebase listeners
            setAuthStateListener();

            //button listeners
            setCameraButtonListener();
            setMenuButtonListener();
            setGalleryGridViewListener();

            setNavDrawer();
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        mImageUrlList.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mImageUrlList.clear();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.transition.slide_right, R.transition.fade_in);
    }

    /**
     * initialize the views for the activity
     */
    public void initializeViews(){
        mCameraButton = (ImageButton)findViewById(R.id.gallery_picture);
        mMenuButton = (ImageButton)findViewById(R.id.gallery_menu);
        mGalleryGridView = (GridView)findViewById(R.id.gallery_gridview);
    }

    /**
     * sets the navigation drawer for the activity
     */
    public void setNavDrawer(){
        mDrawer = getNavDrawer();
    }

    /**
     * listener for camera button
     */
    public void setCameraButtonListener(){
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(cameraIntent);
                overridePendingTransition(R.transition.slide_left, R.transition.fade_out);
            }
        });
    }

    /**
     * listener for menu button
     */
    public void setMenuButtonListener(){
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer();
            }
        });
    }

    /**
     * listener for gallery gridview
     */
    public void setGalleryGridViewListener() {
        mGalleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(getApplicationContext(), DetailActivity.class);
                detailIntent.putStringArrayListExtra("list", mImageUrlList);
                detailIntent.putExtra("position", position);

                ImageView sharedElement = (ImageView)view;

                Bundle bundle = ActivityOptions
                        .makeSceneTransitionAnimation(GalleryActivity.this, sharedElement, sharedElement.getTransitionName())
                        .toBundle();

                startActivity(detailIntent, bundle);
            }
        });
    }

    /**
     * listner for firebase authentication
     */
    public void setAuthStateListener(){
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                //gets the active user
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                //if the user is not null the database reference is retrieved
                if(firebaseUser != null){
                    mActiveUser = firebaseUser;
                    mDatabaseReference = FirebaseDatabase
                            .getInstance()
                            .getReference()
                            .child(Util.returnSplitEmail(mActiveUser.getEmail().toString()));

                    setChildEventListener(mDatabaseReference, mChildEventListener);
                }
            }
        };
    }

    /**
     * set child event listener for firebase real-time database
     */
    public void setChildEventListener(final DatabaseReference reference,
                                      ChildEventListener childEventListener){

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Map<String, CloudImage> cd = (HashMap<String, CloudImage>)dataSnapshot.getValue();
                Collection<CloudImage> values = cd.values();

                mFirebaseDataList = new ArrayList(values);

                /*
                 * puts the mImageUrlList into a HashSet and removes the duplicate elements
                 * and repopulates the list
                 */
                /*HashSet<String> hashSet = new HashSet<>();
                hashSet.addAll(mImageUrlList);
                mImageUrlList.clear();
                mImageUrlList.addAll(hashSet);*/

                GallerySort gallerySort = new GallerySort(mFirebaseDataList);
                gallerySort.run();

                updateUI();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        reference.addChildEventListener(childEventListener);
    }

    /**
     * updates the gridview to contain the most recent images
     */
    public void updateUI(){
        GalleryAdapter adapter = new GalleryAdapter(getApplicationContext(), mImageUrlList);
        mGalleryGridView.setAdapter(adapter);
    }

    /**
     * selection for for gallery images
     * created by tsparrow
     */
    public static String[] selectionSort(ArrayList mList) {

        String[] list = new String[mList.size()];

        /**
         * take ArrayList mList and transfer contents into String[] list;
         */
        for (int i = 0; i < mList.size(); i++) {
            list[i] = mList.get(i).toString();
        }

        /**
         * arrays to store the urls and the timestamps separately.
         * both half the size of the original array.
         */
        String[] urlArray = new String[list.length / 2];
        Integer[] timeStampArray = new Integer[list.length / 2];

        /**
         * counters for the two separate arrays.
         */
        int urlCounter = 0;
        int timeCounter = 0;

        /**
         * split the list by urls and by timestamps
         */
        for (int i = 0; i < list.length; i+=2) {

            /**
             * if i is an even number
             * (these are the urls)
             */
            if (i == 0 || i % 2 == 0) {
                urlArray[urlCounter++] = list[i];
            }

            /**
             * if i is an odd number
             * (these are the timeStamps)
             */
            else {
                timeStampArray[timeCounter++] = Integer.valueOf(list[i]);
            }
        }

        /**
         * selection sort the arrays based on the timestamps
         */
        for (int i = 0; i < timeStampArray.length; i++) {

            int index = 1;

            for (int j = i + 1; j < timeStampArray.length; j++) {

                if (timeStampArray[j] < timeStampArray[index]) {
                    index = j;
                }

                int smallerNumber = timeStampArray[index];
                String smallerUrl = urlArray[index];

                /**
                 * sorting the timestamps.
                 */
                timeStampArray[index] = timeStampArray[i];
                timeStampArray[i] = smallerNumber;

                /**
                 * sorting the urls.
                 */
                urlArray[index] = urlArray[i];
                urlArray[i] = smallerUrl;
            }

            /**
             * return the sorted array of urls.
             */
            return urlArray;
        }
        return null;
    }

    /**
     * sorts the image url list off thread
     */
    public class GallerySort implements Runnable{

        private ArrayList<String> mList;

        public GallerySort() {
        }

        public GallerySort(ArrayList<String> list) {
            mList = list;
        }

        @Override
        public void run() {
            String[] urlArray = selectionSort(mList);

            for (int i = 0; i < urlArray.length; i++) {

                mImageUrlList.add(urlArray[i]);

            }
        }
    }

    /**
     * methods for nav drawer
     */

    /**
     * opens the play store using an intent so users can rate the app
     */
    public void openPlayStore(){
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, uri);

        //allows for us to return to app from play store using back button
        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        try{
            startActivity(playStoreIntent);
        }
        catch (ActivityNotFoundException e){
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getApplicationContext().getPackageName())));
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

    /**
     * opens the cloud usage activity
     */
    public void cloudUsageIntent() {
        Intent usageIntent = new Intent(getApplicationContext(), UsageActivity.class);
        startActivity(usageIntent);
    }

    /**
     * logs the user out of firebase and returns the user to the get started activity
     */
    public void logOut(){
        FirebaseAuth.getInstance().signOut();
        Intent logoutActivity = new Intent(getApplicationContext(), GetStartedActivity.class);
        startActivity(logoutActivity);
    }

    /**
     * sets the nav drawer behavior
     */
    public Drawer getNavDrawer(){

        new DrawerBuilder().withActivity(this).build();

        //set the nav drawer elements
        PrimaryDrawerItem cloudUsageItem = new PrimaryDrawerItem()
                .withIdentifier(1)
                .withName(getString(R.string.menu_element_usage))
                .withIcon(R.drawable.ic_cloud_black_48dp);

        PrimaryDrawerItem rateItem = new PrimaryDrawerItem()
                .withIdentifier(2)
                .withName(getString(R.string.menu_element_rate_app))
                .withIcon(R.drawable.ic_star_rate_black_18dp);

        PrimaryDrawerItem feedbackItem = new PrimaryDrawerItem()
                .withIdentifier(2)
                .withName(getString(R.string.menu_element_feedback))
                .withIcon(R.drawable.ic_feedback_black_24dp);

        PrimaryDrawerItem settingsItem = new PrimaryDrawerItem()
                .withIdentifier(3)
                .withName(getString(R.string.menu_element_settings))
                .withIcon(R.drawable.ic_settings_black_24dp);

        PrimaryDrawerItem logoutItem = new PrimaryDrawerItem()
                .withIdentifier(3)
                .withName(getString(R.string.menu_element_log_out))
                .withIcon(R.drawable.ic_power_settings_new_black_24dp);

        //create the drawer
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(setNavDrawerHeader())
                .addDrawerItems(cloudUsageItem,
                        new DividerDrawerItem(),
                        feedbackItem,
                        rateItem,
                        new DividerDrawerItem(),
                        settingsItem,
                        logoutItem)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position) {
                            case 0: {
                                break;
                            }
                            case 1: {
                                cloudUsageIntent();
                                break;
                            }
                            case 2: {
                                break;
                            }
                            case 3: {
                                sendFeedBack();
                                break;
                            }
                            case 4: {
                                //openPlayStore();
                                break;
                            }
                            case 5: {
                                break;
                            }
                            case 6: {
                                //TODO: settings intent
                                break;
                            }
                            case 7: {
                                logOut();
                                break;
                            }
                        }
                        return true;
                    }
                })
                .build();

        return result;
    }

    /**
     * sets the header image for the nav drawer
     */
    public AccountHeader setNavDrawerHeader(){
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.fall_camera)
                .build();

        return headerResult;
    }
}
