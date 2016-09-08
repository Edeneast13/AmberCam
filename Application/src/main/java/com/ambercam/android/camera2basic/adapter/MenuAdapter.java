package com.ambercam.android.camera2basic.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ambercam.android.camera2basic.R;
import com.ambercam.android.camera2basic.MenuElement;

import java.util.ArrayList;

/**
 * Created by brianroper on 8/22/16.
 */
public class MenuAdapter extends ArrayAdapter {

    private Context mContext;
    private ArrayList<MenuElement> mMenuElementList;
    private LayoutInflater mLayoutInflater;
    private ImageView mMenuImageView;
    private TextView mMenuTextView;

    public MenuAdapter(Context context, ArrayList<MenuElement> list){
        super(context, R.layout.menu_listview, list);
        this.mContext = context;
        this.mMenuElementList = list;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.menu_item, parent, false);
        }

        mMenuImageView = (ImageView)convertView.findViewById(R.id.menu_item_icon);
        mMenuTextView = (TextView)convertView.findViewById(R.id.menu_item_text);

        mMenuTextView.setText(mMenuElementList.get(position).getTitle());

        Log.i("Menu Text: ", mMenuTextView.getText().toString());

        mMenuImageView.setImageResource(mMenuElementList.get(position).getIcon());

        return convertView;
    }
}
