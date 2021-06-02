package com.example.skateterrain;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.android.gms.maps.model.LatLng;

public class Item {
    private String itemName;
    private String itemDescription;
    private String itemImageFilePath;
    private LatLng itemLocation;
    private String itemType;

    public Item(String name, String description,String spotType,String filePath,LatLng location) {
        this.itemName = name;
        this.itemDescription = description;
        this.itemType = spotType;
        this.itemImageFilePath = filePath;
        this.itemLocation = location;
    }

    public String getItemName() {
        return this.itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public String getItemFilePath() {
        return itemImageFilePath;
    }

    public LatLng getItemLocation() {
        return itemLocation;
    }

    public String getItemType() {
        return itemType;
    }
}
