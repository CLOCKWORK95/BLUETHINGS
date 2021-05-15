package com.example.gianmarco.bluethings.UTILS;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.gianmarco.bluethings.R;

import java.util.ArrayList;

public class PictureArrayAdapter extends ArrayAdapter {


    private Context context;
    int resource;
    private ArrayList<Bitmap> pictures;


    public PictureArrayAdapter(Context context, int resource, ArrayList<Bitmap> object){
        super(context, resource, object);

        this.context = context;
        this.resource = resource;
        this.pictures = object;

    }


    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent){

        Bitmap picture = (Bitmap) getItem(position);

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        ImageView image = (ImageView) convertView.findViewById(R.id.aPicture);
        image.setImageBitmap(picture);


        return convertView;

    }


}
