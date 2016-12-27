package com.ambercam.android.camera2basic.views;

import com.ambercam.android.camera2basic.models.FirebaseData;

/**
 * Created by brianroper on 12/15/16.
 */

public interface DetailView extends View {
    void returnIntentExtras();
    void setDeviceVibration();
    void shareImage();
    void returnUserPreferences();
    void handleDeviceVibration();
    void setFirebaseAuthListener();
    FirebaseData setFirebaseData();
    void setDetailImageViewSwipeListener();
}
