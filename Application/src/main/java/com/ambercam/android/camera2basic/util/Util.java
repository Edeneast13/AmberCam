package com.ambercam.android.camera2basic.util;

import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Patterns;
import android.view.Display;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by brianroper on 8/4/16.
 */
public class Util {

    /**
     * initializes and shows a progress dialog
     */
    public static MaterialDialog showProgressDialog(Context context){
        MaterialDialog builder = new MaterialDialog.Builder(context)
                .title("Uploading...")
                .content("please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();
        return builder;
    }

    /**
     * gets the devices screen height
     */
    public int returnScreenHeight(Context context){
        WindowManager windowManager = (WindowManager)context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        return height;
    }

    /**
     * gets the devices screen width
     */
    public int returnScreenWidth(Context context){
        WindowManager windowManager = (WindowManager)context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    /**
     * check the device for an active network connection
     */
    static public boolean activeNetworkCheck(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * check for valid email format
     */
    static public boolean isValidEmail(CharSequence email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * returns the first half of an email address
     */
    static public String returnSplitEmail(String email){
        String[] splitEmail = email.split("@");
        return splitEmail[0];
    }
}
