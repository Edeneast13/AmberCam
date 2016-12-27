package com.ambercam.android.camera2basic.presenters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;

import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.ambercam.android.camera2basic.models.User;
import com.ambercam.android.camera2basic.util.Util;
import com.ambercam.android.camera2basic.views.GetStartedView;

/**
 * Created by brianroper on 12/26/16.
 */

public class GetStartedPresenter implements Presenter<GetStartedView> {

    private Context mContext;
    private GetStartedView mGetStartedView;

    public GetStartedPresenter(Context context) {
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
    public void attachView(GetStartedView view) {
        this.mGetStartedView = view;
    }

    /**
     * retrieve user entry from views
     */
    public User verifyTextFromViews(String email, String password){

        User user = new User();

        if(!email.equals(null) && Util.isValidEmail(email) == true){
            user.setEmail(email);
            Log.i("Email: ", email);
        }
        else{
            Toast.makeText(mContext, "Please enter a valid email",
                    Toast.LENGTH_LONG).show();
        }
        if(password.length() >= 6){
            user.setPassword(password);
        }
        else{
            Toast.makeText(mContext, "Password must be at least 6 characters",
                    Toast.LENGTH_LONG).show();
        }
        return user;
    }

    /**
     * sets animator for title slide transition animation
     */
    public void setViewTransitionAnimations(ImageView imageView){
        ObjectAnimator iconMover = ObjectAnimator.ofFloat(
                imageView,
                "translationY",
                (Util.returnScreenHeight(mContext)),
                0f);
        iconMover.setDuration(700);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(iconMover);
        animatorSet.start();
    }
}
