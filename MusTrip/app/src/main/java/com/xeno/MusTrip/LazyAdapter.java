package com.xeno.MusTrip;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mcarr on 11/8/2016.
 */
public class LazyAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> web;
    private final ArrayList<Bitmap> imageId;
    public LazyAdapter(Activity context,
                       ArrayList<String> web, ArrayList<Bitmap> imageId) {
        super(context, R.layout.row_layout, web);
        this.context = context;
        this.web = web;
        this.imageId = imageId;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.row_layout, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(web.get(position));

        imageView.setImageBitmap(imageId.get(position));
        return rowView;
    }
}