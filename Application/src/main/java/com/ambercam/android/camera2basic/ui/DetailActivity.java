package com.ambercam.android.camera2basic.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ambercam.android.camera2basic.R;
import com.ambercam.android.camera2basic.util.Util;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    private ImageView mDetailImageView;
    private RelativeLayout mRelativeLayout;
    private Button mDetailDeleteButton;
    private Toolbar mToolbar;
    private ArrayList<String> mImageUrlList;
    private int mPosition;
    final int VIBRATE_DURATION = 200;
    private String mFinalUrl;

    final String FIREBASE_BUCKET = "gs://cloudcamera-95ade.appspot.com";
    private FirebaseStorage mFirebaseStorage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mActiveUser;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initializeViews();
        initializeToolbarBehavior(mToolbar);
        returnIntentExtras();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        setAuthStateListener();

        setDetailImageViewLongClickListener();
        setDetailDeleteButtonListener();

        SplitUrl splitUrl = new SplitUrl(mPosition, mImageUrlList);
        splitUrl.run();

        updateUI(mImageUrlList, mPosition);
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
     * initialize all views in activity
     */
    public void initializeViews(){
        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mDetailImageView = (ImageView)findViewById(R.id.detail_imageview);
        mDetailDeleteButton = (Button)findViewById(R.id.detail_delete_button);
        mRelativeLayout = (RelativeLayout)findViewById(R.id.detail_relative_layout);
    }

    /**
     * handles toolbar behavior
     */
    public void initializeToolbarBehavior(Toolbar toolbar){
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.app_title));
    }

    /**
     * retrieve intent extras from previous activity
     */
    public void returnIntentExtras(){
        Bundle bundle = getIntent().getExtras();
        mPosition = bundle.getInt("position");
        mImageUrlList = bundle.getStringArrayList("list");
    }

    /**
     * update user interface
     */
    public void updateUI(ArrayList<String> list, int position){
        Glide.with(getApplicationContext())
                .load(list.get(position))
                .into(mDetailImageView);
    }

    /**
     * long click listener for the detail imageview
     */
    public void setDetailImageViewLongClickListener(){
        mDetailImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mRelativeLayout.setVisibility(View.VISIBLE);
                setDeviceVibration();
                return false;
            }
        });
    }

    /**
     * listener for detail delete button
     */
    public void setDetailDeleteButtonListener(){
        mDetailDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImageFromDatabase(mDatabaseReference,
                        Util.returnSplitEmail(mActiveUser.getEmail()));
                deleteImageFromStorage(mFirebaseStorage,
                        Util.returnSplitEmail(mActiveUser.getEmail()));
            }
        });
    }

    /**
     * enables device vibration
     */
    public void setDeviceVibration(){
        Vibrator vibrator = (Vibrator) getApplicationContext()
                .getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATE_DURATION);
    }

    /**
     * removes the image from firebase storage
     */
    public void deleteImageFromStorage(FirebaseStorage storage, String user){

        //reference to the projects storage bucket
        StorageReference reference = storage.getReferenceFromUrl(FIREBASE_BUCKET);

        //reference to the users storage folder
        StorageReference subReference = reference.child(user);

        //reference to the individual image stored in firebase
        StorageReference timestampReference = subReference
                .child(mFinalUrl);

        //deletes the image
        timestampReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), getString(R.string.detail_delete_success_toast),
                        Toast.LENGTH_SHORT).show();
                Intent galleryIntent = new Intent(getApplicationContext(), GalleryActivity.class);
                startActivity(galleryIntent);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getString(R.string.detail_delete_failed_toast),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * removes image data from firebase realtime database
     */
    public void deleteImageFromDatabase(DatabaseReference databaseReference, String user){

        //reference to the users data in firebase
        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(Util.returnSplitEmail(mActiveUser.getEmail().toString()));

        //reference to the individual image data stored in firebase
        DatabaseReference subReference = databaseReference
                .child(mFinalUrl);

        //removes the image data from firebase
        subReference
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("Realtime Delete: ", "Success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.i("Realtime Delete: ", "Failed");
                        e.printStackTrace();
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
                mActiveUser = firebaseAuth.getCurrentUser();
            }
        };
    }

    /**
     * returns the timestamp from the url off thread
     */
    public class SplitUrl implements Runnable{

        private int position;
        private ArrayList<String> list;

        public SplitUrl() {
        }

        public SplitUrl(int position, ArrayList<String> list) {
            this.position = position;
            this.list = list;
        }

        @Override
        public void run() {
            returnImageTimeStamp(position, list);
        }

        /**
         * returns the timestamp from the url
         */
        public String returnImageTimeStamp(int position, ArrayList<String> list){
            String url = list.get(position);
            String[] splitUrl = url.split("%2F");
            String secondUrl = splitUrl[1];
            String[] secondSplitUrl = secondUrl.split("\\?");
            mFinalUrl = secondSplitUrl[0];
            return mFinalUrl;
        }
    }
}
