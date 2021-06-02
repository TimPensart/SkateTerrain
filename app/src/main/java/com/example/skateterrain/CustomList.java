package com.example.skateterrain;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.skateterrain.R;

import java.util.ArrayList;

public class CustomList extends ArrayAdapter<String>{

    private final Activity context;
    private ArrayList<String> web = new ArrayList<String>();
    private ArrayList<Integer> imageId = new ArrayList<Integer>();
    public CustomList(@NonNull Activity context, ArrayList<String> web, ArrayList<Integer> imageId) {
        super(context, R.layout.list_single, web);
        this.context = context;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(web.get(position));
        imageView.setImageResource(imageId.get(position));
        return rowView;
    }

/*
    public void addString(String string) {
        web.add(string);
    }


    public void addImageId(Integer integer) {
        imageId.add(integer);
    }
    */
}