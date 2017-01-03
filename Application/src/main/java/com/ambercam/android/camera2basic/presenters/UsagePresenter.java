package com.ambercam.android.camera2basic.presenters;

import android.content.Context;
import com.ambercam.android.camera2basic.views.UsageView;

/**
 * Created by brianroper on 12/27/16.
 */

public class UsagePresenter implements Presenter<UsageView> {

    private UsageView mUsageView;
    private Context mContext;

    public UsagePresenter(Context context) {
        this.mContext = context;
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
    public void attachView(UsageView view) {
        this.mUsageView = view;
    }
}
