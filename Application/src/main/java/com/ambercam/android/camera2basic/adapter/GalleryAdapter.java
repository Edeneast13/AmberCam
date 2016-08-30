package com.ambercam.android.camera2basic.adapter;

import android.app.ActionBar;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.ambercam.android.camera2basic.R;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by brianroper on 8/7/16.
 */
public class GalleryAdapter extends ArrayAdapter {

    private Context mContext;
    private ArrayList<String> mArrayList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private ImageView mImageView;

    public GalleryAdapter(Context context, ArrayList<String> list){
        super(context, R.layout.gallery_gridview, list);
        this.mContext = context;
        this.mArrayList = list;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.gallery_item, parent, false);
            mImageView = (ImageView)convertView;
        }
        else{
            mImageView = (ImageView)convertView;
        }

        mImageView.setPadding(2,2,2,2);
        mImageView.setAdjustViewBounds(true);

        for (int i = 0; i < mArrayList.size(); i++) {
            Glide.with(mContext)
                    .load(mArrayList.get(position))
                    .into(mImageView);
        }
        return mImageView;
    }
}
