package com.example.myapplication.examplerecyclerview;

import android.animation.TimeInterpolator;

public class ItemModel {
    public final String description;
    public boolean isExpanded;
    public final int colorId1;
    public final int colorId2;
    public final TimeInterpolator interpolator;

    public ItemModel(String description, int colorId1, int colorId2,boolean isExpanded, TimeInterpolator interpolator) {
        this.description = description;
        this.colorId1 = colorId1;
        this.colorId2 = colorId2;
        this.isExpanded = isExpanded;
        this.interpolator = interpolator;
    }
}