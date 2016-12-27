package com.ambercam.android.camera2basic.presenters;

import android.content.Context;
import android.widget.Toast;

import com.ambercam.android.camera2basic.models.CountData;
import com.ambercam.android.camera2basic.models.User;
import com.ambercam.android.camera2basic.util.Util;
import com.ambercam.android.camera2basic.views.CreateAccountView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by brianroper on 12/27/16.
 */

public class CreateAccountPresenter implements Presenter<CreateAccountView> {

    private CreateAccountView mCreateAccountView;
    private Context mContext;

    final int BASE_IMAGE_MAX = 100;
    final int BASE_IMAGE_COUNT = 0;

    public CreateAccountPresenter(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void attachView(CreateAccountView view) {
        this.mCreateAccountView = view;
    }

    /**
     * sets the values for the users count data which is used to keep track of
     * images stored and the users personal limit within firebase
     */
    public void initializeCountData(FirebaseDatabase database, User user){
        String[] userRoot = user.getEmail().split("@");

        //reference to the users image count
        DatabaseReference reference = database.getReference(userRoot[0] + "_count");

        //reference to the users count data
        DatabaseReference subReference = reference.child("countData");

        //all accounts start with 0 stored images and a 100 image max
        CountData countData = new CountData(BASE_IMAGE_COUNT, BASE_IMAGE_MAX);

        //sets the initialized values to the newly created firebase reference
        subReference.setValue(countData);
    }

    /**
     * returns text from views and puts data into new user object
     */
    public User returnNewUser(String email, String confirmEmail,
                              String password, String confirmPassword) {

        User user = new User();

        //email tests
        if (!email.equals(confirmEmail)){
            Toast.makeText(mContext, "emails do not match",
                    Toast.LENGTH_SHORT).show();
        } else if (email.equals(null)) {
            Toast.makeText(mContext, "Enter a valid email address",
                    Toast.LENGTH_SHORT).show();
        } else if (!Util.isValidEmail(email)) {
            Toast.makeText(mContext, "Enter a valid email address",
                    Toast.LENGTH_SHORT).show();
        } else {
            user.setEmail(email);
        }

        //password tests
        if (!password.equals(confirmPassword)) {
            Toast.makeText(mContext, "Passwords do not match",
                    Toast.LENGTH_SHORT).show();
        }
        else if (password.length() < 6) {
            Toast.makeText(mContext, "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT).show();
        }
        else{
            user.setPassword(password);
        }

        return user;
    }
}
