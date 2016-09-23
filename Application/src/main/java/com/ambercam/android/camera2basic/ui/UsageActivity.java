package com.ambercam.android.camera2basic.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.ambercam.android.camera2basic.CountData;
import com.ambercam.android.camera2basic.R;
import com.ambercam.android.camera2basic.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class UsageActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView mUsageTextView;
    private MaterialProgressBar mUsageProgessBar;
    private TextView mUsagePercentTextView;
    private CardView mCloudCardView;
    private TextView mUserNameTextView;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mActiveUser;
    private DatabaseReference mDatabaseReference;
    private CountData mCountData;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);

        initializeViews();
        setToolbarBehavior(mToolbar);

        mAuth = FirebaseAuth.getInstance();
        setFirebaseAuthListener();

        setCardBackgroundColors();
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
     * initialize the activities views
     */
    public void initializeViews(){
        mToolbar = (Toolbar)findViewById(R.id.usage_toolbar);
        mUsageTextView = (TextView)findViewById(R.id.usage_text);
        mUsageProgessBar = (MaterialProgressBar)findViewById(R.id.usage_progress_bar);
        mUsagePercentTextView = (TextView)findViewById(R.id.usage_percent_text);
        mCloudCardView = (CardView)findViewById(R.id.image_surface);
        mUserNameTextView = (TextView)findViewById(R.id.usage_user_text);
    }

    /**
     * handles all toolbar behavior
     */
    public void setToolbarBehavior(Toolbar toolbar){
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    /**
     * sets the authentication state listener for firebase
     */
    private void setFirebaseAuthListener(){
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //gets the active user
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null){
                    mActiveUser = firebaseUser;
                    mDatabaseReference = FirebaseDatabase
                            .getInstance()
                            .getReference()
                            .child(Util.returnSplitEmail(firebaseUser.getEmail().toString()) + "_count");

                    setChildEventListener(mDatabaseReference, mChildEventListener);
                    setUserNameTextView();
                }
            }
        };
    }

    /**
     * set the child event listener that retrieves the count data from firebase
     */
    public void setChildEventListener(DatabaseReference reference,
                                      ChildEventListener childEventListener){

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mCountData = dataSnapshot.getValue(CountData.class);
                setUsageProgessBarValue(mCountData.getImageMax(), mCountData.getImageCount());
                setUsageTextViewText(mCountData.getImageMax(), mCountData.getImageCount());
                setCloudPercentText(mCountData.getImageMax(), mCountData.getImageCount());
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
     * set the values of the progress bar
     */
    public void setUsageProgessBarValue(int max, int count){
        mUsageProgessBar.setMax(max);
        mUsageProgessBar.setProgress(count);
    }

    /**
     * set the values for the count textView
     */
    public void setUsageTextViewText(int max, int count){
        mUsageTextView.setText(count + "/" + max);
    }

    /**
     * sets the value for the percent textView
     */
    public void setCloudPercentText(int max, int count){
        int percentUsed = ((count*100)/max);
        mUsagePercentTextView.setText(percentUsed + "%");
    }

    /**
     * sets card color background
     */
    public void setCardBackgroundColors(){
        mCloudCardView.setCardBackgroundColor(getResources().getColor(R.color.cardview_light_background));
    }

    /**
     * sets text for user name textView
     */
    public void setUserNameTextView(){
        mUserNameTextView.setText(mActiveUser.getEmail().toString());
    }
}
