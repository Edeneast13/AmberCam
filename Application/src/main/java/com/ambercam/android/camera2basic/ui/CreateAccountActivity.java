package com.ambercam.android.camera2basic.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ambercam.android.camera2basic.R;
import com.ambercam.android.camera2basic.models.User;
import com.ambercam.android.camera2basic.presenters.CreateAccountPresenter;
import com.ambercam.android.camera2basic.util.Util;
import com.ambercam.android.camera2basic.views.CreateAccountView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateAccountActivity extends AppCompatActivity implements CreateAccountView {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private User mUser;
    private CreateAccountPresenter mCreateAccountPresenter;

    @BindView(R.id.create_account_email)
    public EditText mEmailEditText;
    @BindView(R.id.create_account_confirm_email)
    public EditText mConfirmEmailEditText;
    @BindView(R.id.create_account_password)
    public EditText mPasswordEditText;
    @BindView(R.id.create_account_confirm_password)
    public EditText mConfirmPasswordEditText;
    @BindView(R.id.create_account_create)
    public Button mCreateButton;
    @BindView(R.id.create_toolbar)
    public Toolbar mToolbar;

    /**
     * LIFE CYCLE methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        ButterKnife.bind(this);

        mCreateAccountPresenter = new CreateAccountPresenter(getApplicationContext());
        mCreateAccountPresenter.attachView(this);

        initializeToolbarBehavior(mToolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        setFirebaseAuthListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.transition.slide_left, R.transition.fade_out);
    }

    /**
     * listener for firebase authentication
     */
    @Override
    public void setFirebaseAuthListener(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
            }
        };
    }

    /**
     * creates a new user in firebase
     */
    public void createNewUser(User user, FirebaseAuth auth){

        final User newUser = user;

        auth.createUserWithEmailAndPassword(newUser.getEmail(), newUser.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.authentication_failed),
                                    Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.authentication_success),
                                    Toast.LENGTH_LONG).show();

                            mCreateAccountPresenter.initializeCountData(mFirebaseDatabase, newUser);

                            Intent loginIntent = new Intent(getApplicationContext(), CameraActivity.class);
                            startActivity(loginIntent);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * listener for create account button
     */
    @OnClick(R.id.create_account_create)
    public void createButtonListener(){
        mUser = mCreateAccountPresenter.returnNewUser(
                mEmailEditText.getText().toString(),
                mConfirmEmailEditText.getText().toString(),
                mPasswordEditText.getText().toString(),
                mConfirmPasswordEditText.getText().toString());

        if(Util.activeNetworkCheck(getApplicationContext()) == true){
            createNewUser(mUser, mFirebaseAuth );
        }
        else{
            Util.noActiveNetworkToast(getApplicationContext());
        }
    }
}

