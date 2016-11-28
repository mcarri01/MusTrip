package com.xeno.MusTrip;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
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
 *
 * The "LazyAdapter" class is a custom ArrayAdapter that allows us to place both a Bitmap (the image
 * for the album cover) and the songs name and location within the same row for a listview.
 */
public class LazyAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> text;
    private final ArrayList<Bitmap> imageId;
    /* Constructor */
    public LazyAdapter(Activity context,
                       ArrayList<String> text, ArrayList<Bitmap> imageId) {
        super(context, R.layout.row_layout, text);
        this.context = context;
        this.text = text;
        this.imageId = imageId;

    }
    @Override
    /* Initializes each row of the listview */
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.row_layout, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(Html.fromHtml(text.get(position)));

        imageView.setImageBitmap(imageId.get(position));
        return rowView;
    }
}