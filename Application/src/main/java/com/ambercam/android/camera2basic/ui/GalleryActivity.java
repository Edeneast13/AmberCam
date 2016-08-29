package com.ambercam.android.camera2basic.ui;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GalleryActivity extends Activity {

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

    /**
     * Life Cycle methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initializeViews();

        mAuth = FirebaseAuth.getInstance();

        //firebase listeners
        setAuthStateListener();

        //button listeners
        setCameraButtonListener();
        setMenuButtonListener();
        setGalleryGridViewListener();
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
        mImageUrlList.clear();
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
     * listener for camera button
     */
    public void setCameraButtonListener(){
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(cameraIntent);
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
                Intent menuIntent = new Intent(getApplicationContext(), MenuActivity.class);
                startActivity(menuIntent);
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
                startActivity(detailIntent);
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
     * set child event listener for firebase realtime database
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
}
