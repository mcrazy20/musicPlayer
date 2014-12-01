package com.example.jeff.musicplayer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by J on 11/12/2014.
 */

//This code is used by our profile display, so that it can display our artist/song information
public class ProfileAdapter extends ArrayAdapter<ProfileDataItem> {
    Context context;
    int layoutResourceId;
    ProfileDataItem data[] = null;
    public ProfileAdapter(Context context, int layoutResourceId, ProfileDataItem[] data)
    {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }
    @Override
    //Used to create the views in each spot in the list
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        WeatherHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new WeatherHolder();
            holder.txtTitle = (TextView)row.findViewById(R.id.dataTitle);
            holder.txtData = (TextView)row.findViewById(R.id.dataData);

            row.setTag(holder);
        }
        else
        {
            holder = (WeatherHolder)row.getTag();
        }

        ProfileDataItem weather = data[position];
        holder.txtTitle.setText(weather.title);
        holder.txtData.setText(weather.data);

        return row;
    }
    static class WeatherHolder
    {
        TextView txtTitle;
        TextView txtData;
    }
}
