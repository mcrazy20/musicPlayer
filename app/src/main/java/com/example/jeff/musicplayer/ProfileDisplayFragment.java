package com.example.jeff.musicplayer;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Created by J on 11/12/2014.
 */
public class ProfileDisplayFragment extends Fragment {
    private View view;
    private ProfileDataItem[] songList;
    private ProfileDataItem[] artistList;

    public static HashMap<String, Integer> songsPlayed;
    public static HashMap<String, Integer> artistPlayed;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.profile_info,container,false);
        Button btn = (Button) view.findViewById(R.id.backToMain);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                ((MainActivity)getActivity()).hideProfileFrag();
            }
        });
        songsPlayed = CurrentSession.songsPlayed;
        artistPlayed = CurrentSession.artistPlayed;
        Object[] songNames = songsPlayed.keySet().toArray();
        Object[] artistNames = artistPlayed.keySet().toArray();
        songList = new ProfileDataItem[songsPlayed.size()];
        artistList = new ProfileDataItem[artistPlayed.size()];
        Log.d("PROFILE DISPLAY", "Getting here?");
        TextView header1= new TextView(view.getContext());
        header1.setText("Artists");
        header1.setTextSize(40);
        TextView header2= new TextView(view.getContext());
        header2.setText("Songs");
        header2.setTextSize(40);
        for (int i=0; i < songNames.length; i++)
        {
            ProfileDataItem it = new ProfileDataItem((String)songNames[i], songsPlayed.get((String)songNames[i]).toString());
            songList[i]= it;
        }

        for (int i=0; i < artistNames.length; i++)
        {
            ProfileDataItem it = new ProfileDataItem((String)artistNames[i], "" + artistPlayed.get((String)artistNames[i]).toString());
            artistList[i] = it;
        }

        ProfileAdapter songAdapter = new ProfileAdapter((MainActivity)getActivity(), R.layout.profile_list_item, songList);
        ProfileAdapter artistAdapter = new ProfileAdapter((MainActivity)getActivity(), R.layout.profile_list_item, artistList);

        ListView list1 = (ListView)view.findViewById(R.id.songList);
        ListView list2 = (ListView)view.findViewById(R.id.artistList);
        list1.addHeaderView(header2);
        list2.addHeaderView(header1);
        list1.setAdapter(songAdapter);
        list2.setAdapter(artistAdapter);
        return view;
    }


}
