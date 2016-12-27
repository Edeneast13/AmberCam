package com.ambercam.android.camera2basic.views;

import com.ambercam.android.camera2basic.models.User;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by brianroper on 12/15/16.
 */

public interface GetStartedView extends View {
    void loginUserWithFirebase(User user, FirebaseAuth auth);
    void setFirebaseAuthListener();
}
