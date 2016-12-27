package com.ambercam.android.camera2basic.ui;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ambercam.android.camera2basic.R;
import com.ambercam.android.camera2basic.models.User;
import com.ambercam.android.camera2basic.presenters.GetStartedPresenter;
import com.ambercam.android.camera2basic.util.Util;
import com.ambercam.android.camera2basic.views.GetStartedView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GetStartedActivity extends Activity implements GetStartedView {

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView mCreateTextView;
    private ImageView mIconImageView;

    private GetStartedPresenter mGetStartedPresenter;

    /**
     * LIFE CYCLE methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        mGetStartedPresenter = new GetStartedPresenter(getApplicationContext());
        mGetStartedPresenter.attachView(this);

        initializeViews();
        mGetStartedPresenter.setViewTransitionAnimations(mIconImageView);

        mAuth = FirebaseAuth.getInstance();
        setFirebaseAuthListener();

        setLoginButtonListener();
        setCreateTextViewListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * disables device and toolbar back buttons
     */
    @Override
    public void onBackPressed() {}

    /**
     * initialize the activities views
     */
    public void initializeViews(){
        mEmailEditText = (EditText)findViewById(R.id.get_started_email);
        mPasswordEditText = (EditText)findViewById(R.id.get_started_password);
        mLoginButton = (Button)findViewById(R.id.get_started_login);
        mCreateTextView = (TextView)findViewById(R.id.get_started_create);
        mIconImageView = (ImageView)findViewById(R.id.get_started_icon);
    }

    /**
     * listener for login button
     */
    public void setLoginButtonListener(){
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                User user = mGetStartedPresenter.verifyTextFromViews(mEmailEditText.getText().toString(),
                        mPasswordEditText.getText().toString());

                if(Util.activeNetworkCheck(getApplicationContext()) == true){
                    try{
                        loginUserWithFirebase(user, mAuth);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    Util.noActiveNetworkToast(getApplicationContext());
                }
            }
        });
    }

    /**
     * initialize the listener for firebase authentication
     */
    @Override
    public void setFirebaseAuthListener(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent autoLoginIntent = new Intent(getApplicationContext(),
                            CameraActivity.class);
                    startActivity(autoLoginIntent);
                    overridePendingTransition(R.transition.fade_in, R.transition.fade_out);
                }
            }
        };
    }

    /**
     * login an existing user using email and password
     */
    @Override
    public void loginUserWithFirebase(User user, FirebaseAuth auth){
        auth.signInWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Intent cameraIntent = new Intent(getApplicationContext(),
                                    CameraActivity.class);
                            startActivity(cameraIntent);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.get_started_error),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    /**
     * listener for create account clickable text view
     */
    public void setCreateTextViewListener(){
        mCreateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createIntent = new Intent(getApplicationContext(), CreateAccountActivity.class);
                startActivity(createIntent);
                overridePendingTransition(R.transition.slide_right, R.transition.fade_out);
            }
        });
    }
}
















