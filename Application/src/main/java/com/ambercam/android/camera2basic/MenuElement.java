package com.ambercam.android.camera2basic;

import android.graphics.drawable.Drawable;

/**
 * Created by brianroper on 8/22/16.
 */
public class MenuElement {

    private String title;
    private int iconId;

    public MenuElement() {
    }

    public MenuElement(String title, int iconId) {
        this.title = title;
        this.iconId = iconId;
    }

    public String getTitle() {
        return title;
    }

    public int getIcon() {
        return iconId;
    }
}
