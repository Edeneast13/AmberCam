package com.ambercam.android.camera2basic.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ambercam.android.camera2basic.CountData;
import com.ambercam.android.camera2basic.R;
import com.ambercam.android.camera2basic.util.OnSwipeTouchListener;
import com.ambercam.android.camera2basic.util.Util;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    private ImageView mDetailImageView;
    private RelativeLayout mRelativeLayout;
    private Button mDetailDeleteButton;
    private FloatingActionButton mCameraFab;
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
    private FirebaseDatabase mFirebaseDatabase;
    private ChildEventListener mChildEventListener;
    private CountData mCountData;

    final String SWIPEDIRECTIONLEFT = "LEFT";
    final String SWIPEDIRECTIONRIGHT = "RIGHT";
    final int VIEWSLIDEDURATION = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initializeViews();
        initializeToolbarBehavior(mToolbar);
        returnIntentExtras();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        setAuthStateListener();

        setDetailImageViewLongClickListener();
        setDetailDeleteButtonListener();
        setDetailImageViewSwipeListener();
        setCameraFabListener();

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
        mToolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        mDetailImageView = (ImageView)findViewById(R.id.detail_imageview);
        mDetailDeleteButton = (Button)findViewById(R.id.detail_delete_button);
        mRelativeLayout = (RelativeLayout)findViewById(R.id.detail_relative_layout);
        mCameraFab = (FloatingActionButton)findViewById(R.id.detail_camera_fab);
    }

    /**
     * handles toolbar behavior
     */
    public void initializeToolbarBehavior(Toolbar toolbar){
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setLogo(R.drawable.ambercam_title);
    }

    /**
     * menu methods
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_forever:{
                //if delete button is already visible it is hidden
                if(mRelativeLayout.getVisibility() == View.VISIBLE){
                    mRelativeLayout.setVisibility(View.GONE);
                    mCameraFab.setVisibility(View.VISIBLE);
                }
                else{
                    //if button is not visible it is shown
                    mRelativeLayout.setVisibility(View.VISIBLE);
                    setDeviceVibration();
                    mCameraFab.setVisibility(View.GONE);
                }
                break;
            }
            case R.id.share:{
                shareImage();
                break;
            }
            default: return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
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
                return true;
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
                String user = Util.returnSplitEmail(mActiveUser.getEmail());

                deleteImageFromDatabase(mDatabaseReference,
                        Util.returnSplitEmail(user));
                deleteImageFromStorage(mFirebaseStorage,
                        Util.returnSplitEmail(user));

                updateDatabaseImageCounter(mFirebaseDatabase, user);
            }
        });
    }

    /**
     * swipe listener for detail image view
     */
    public void setDetailImageViewSwipeListener() {
        mDetailImageView.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {

            @Override
            public void onSwipeLeft() {

                if (mImageUrlList.size() - 1 == mPosition) {
                    Toast.makeText(getApplicationContext(), "There's no more pictures!", Toast.LENGTH_SHORT).show();
                } else {
                    mPosition = mPosition + 1;
                    updateUI(mImageUrlList, mPosition);
                }

                SlideImageViewAnimation setSlide = new SlideImageViewAnimation(
                        getApplicationContext(),
                        mDetailImageView,
                        SWIPEDIRECTIONLEFT);
                setSlide.run();
            }

            @Override
            public void onSwipeRight() {

                if (mPosition == 0) {
                    Toast.makeText(getApplicationContext(), "That's your first picture!", Toast.LENGTH_SHORT).show();
                } else {
                    mPosition = mPosition - 1;
                    updateUI(mImageUrlList, mPosition);
                }

                SlideImageViewAnimation setSlide = new SlideImageViewAnimation(
                        getApplicationContext(),
                        mDetailImageView,
                        SWIPEDIRECTIONRIGHT);
                setSlide.run();
            }
        });
    }

    public void setCameraFabListener() {
        mCameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(cameraIntent);
                overridePendingTransition(R.transition.fade_in, R.transition.fade_out);
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
     * child event listener that retrieves the current count data for the user
     */
    public void setChildEventListener(DatabaseReference reference,
                                      ChildEventListener childEventListener){

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mCountData = dataSnapshot.getValue(CountData.class);
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
     * updates the image count when a photo is deleted
     */
    public void updateDatabaseImageCounter(FirebaseDatabase database,
                                           String user){

        //decrements the image count when a photo is deleted
        mCountData.setImageCount(mCountData.getImageCount()-1);

        //reference to the users image count
        DatabaseReference reference = database.getReference(user + "_count");

        //reference to the users count data
        DatabaseReference subReference = reference.child("countData");

        subReference.setValue(mCountData);
    }

    /**
     * listener for firebase authentication
     */
    public void setAuthStateListener(){
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                mActiveUser = firebaseAuth.getCurrentUser();
                mDatabaseReference = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child(Util.returnSplitEmail(mActiveUser.getEmail().toString()) + "_count");
                setChildEventListener(mDatabaseReference, mChildEventListener);
            }
        };
    }

    /**
     * share image url using intent
     */
    public void shareImage(){
        String url = mImageUrlList.get(mPosition);
        Log.i("Url: ", url);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(shareIntent, "Share using"));
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

    /**
     * handles detail imageview slide animation off thread
     */
    public class SlideImageViewAnimation implements Runnable{

        private ImageView mImageView;
        private Context mContext;
        private String mDirection;

        public SlideImageViewAnimation(Context context, ImageView imageView, String direction) {
            this.mContext = context;
            this.mImageView = imageView;
            this.mDirection = direction;
        }

        @Override
        public void run() {
            setSlideTransition(mDirection, mImageView);
        }

        /**
         * slide transition for touch swipe listener
         */
        public void setSlideTransition(String direction, ImageView image){
            if(direction.equals(SWIPEDIRECTIONLEFT)){
                ObjectAnimator mover = ObjectAnimator.ofFloat(image,
                        "translationX", Util.returnScreenWidth(mContext), 0f);
                AnimatorSet animatorSet = new AnimatorSet();
                mover.setDuration(VIEWSLIDEDURATION);
                animatorSet.play(mover);
                animatorSet.start();
            }
            else if(direction.equals(SWIPEDIRECTIONRIGHT)){
                ObjectAnimator mover = ObjectAnimator.ofFloat(image,
                        "translationX", -(Util.returnScreenWidth(mContext)), 0f);
                AnimatorSet animatorSet = new AnimatorSet();
                mover.setDuration(VIEWSLIDEDURATION);
                animatorSet.play(mover);
                animatorSet.start();
            }
        }
    }
}
