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

//This class is used to inflate the lyrics fragment
//Most of the work for this fragment is done in MainActivity
public class LyricsFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lyrics,container,false);
        TextView tv = (TextView) view.findViewById(R.id.txt_lyrics);
        tv.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

}
