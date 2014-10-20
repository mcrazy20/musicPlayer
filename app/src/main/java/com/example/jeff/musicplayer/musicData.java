package com.example.jeff.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.provider.MediaStore;

import java.util.Hashtable;
import java.util.Stack;

/**
 * Created by J on 10/19/2014.
 */
public class musicData {
    private MediaPlayer mMediaPlayer;
    private String[] aMusicList;
    private Hashtable<String, Song> musicHash;
    private Context context;
    private Stack<Integer> previousSongs;
    private int currentSong;
    private boolean shuffle = false;

    public musicData(MediaPlayer m, String[] a, Hashtable<String, Song> h, Context c, Stack<Integer> s, int cs, boolean sh)
    {

    }

}
