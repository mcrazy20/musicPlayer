package com.example.jeff.musicplayer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

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
        for (int i=0; i < songNames.length; i++)
        {
            ProfileDataItem it = new ProfileDataItem((String)songNames[i], "" + songsPlayed.get((String)songNames[i]));
        }

        for (int i=0; i < artistNames.length; i++)
        {
            ProfileDataItem it = new ProfileDataItem((String)artistNames[i], "" + artistPlayed.get((String)artistNames[i]));
        }

        ProfileAdapter songAdapter = new ProfileAdapter((MainActivity)getActivity(), R.layout.profile_list_item, songList);
        ProfileAdapter artistAdapter = new ProfileAdapter((MainActivity)getActivity(), R.layout.profile_list_item, artistList);

        ListView list1 = (ListView)view.findViewById(R.id.songList);
        ListView list2 = (ListView)view.findViewById(R.id.artistList);
        list1.setAdapter(songAdapter);
        list2.setAdapter(artistAdapter);
        return view;
    }


}
