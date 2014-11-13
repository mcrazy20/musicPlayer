package com.example.jeff.musicplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Rohit on 11/12/14.
 */
public class LyricsFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_lyrics,container,false);
        TextView tv = (TextView) view.findViewById(R.id.txt_lyrics);
        tv.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

}
