package com.example.jeff.musicplayer;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

/**
 * Created by Rohit on 10/19/14.
 * This Fragment is the fragment used to display the list of songs present in the device
 * and user can interact with the list and play any song
 */
public class musicLoaderFragment extends Fragment {

    public static String[] aMusicList;
    public static String[] albumIds;
    public static Hashtable<String, Song> musicHash;
    public static HashMap<Integer, String> albumHash;
    private View view;
    private String TAG = "MusicLoaderFrag";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.music_loader,container,false);
        musicHash = new Hashtable<String, Song>();
        albumHash = new HashMap<Integer, String>();
        async test = new async();
        test.execute();
        Button btn = (Button) view.findViewById(R.id.btn_song_list_back);
        btn.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view2) {
                                       ((MainActivity)getActivity()).hideTheFrag();
                                   }
                               });
        return view;
    }

    public void onResume()
    {
        super.onResume();
        ((MainActivity)getActivity()).rebindService();
        Log.d(TAG,"ON RESUME");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "ON PAUSE");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "ON STOP");
    }

    //This class is used to get the music stored within the phone
    class async extends AsyncTask<Void, Void, String[]>
    {
        private String[] mMusicList;

        @Override
        protected String[] doInBackground(Void... voids) {
            mMusicList = getMusic();
            return mMusicList;
        }

        protected void onPostExecute(String[] list)
        {
            ListView musicList = (ListView) view.findViewById(R.id.music_list);
            ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(view.getContext(),
                    android.R.layout.simple_list_item_1, mMusicList);
            musicList.setAdapter(mAdapter);

            musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                        long arg3) {
                    try {
                        Log.d(TAG,"Setting path for new song");
                        ((MainActivity)getActivity()).songFromList(arg2);//musicHash.get(mMusicList[arg2]).getPath());
                        MainActivity.currentSong=arg2;
                        ((MainActivity)getActivity()).hideTheFrag();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            aMusicList = mMusicList;
            MainActivity.updateTables();
        }

        //This function reads the data within MediaStore and stores it in a cursor for our listview to use
        public String[] getMusic() {

            //Querying MediaStore
            final Cursor mCursor = view.getContext().getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media.DISPLAY_NAME,MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.ALBUM_ID,MediaStore.Audio.Media.DATA}, null, null,
                    "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");
            final Cursor albumCursor = view.getContext().getContentResolver().query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART}, null, null,
                    "LOWER(" + MediaStore.Audio.Albums._ID + ") ASC");

            int count = mCursor.getCount();

            String[] songs = new String[count];
            int i = 0;
            //Creating a hash table for music
            if (mCursor.moveToFirst()) {
                do {
                    String artist;
                    int albumName;
                    String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if (mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST) == -1) {
                        artist = "";
                    } else {
                        artist = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    }

                    if (mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID) == -1) {
                        albumName =0;
                    } else {
                        albumName = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    }
                    Song song = new Song(path, artist, albumName);
                    songs[i] = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    musicHash.put(songs[i], song);
                    i++;

                } while (mCursor.moveToNext());
            }

            mCursor.close();
            //Creating the hash table for the album art
            if (albumCursor.moveToFirst())
            {
                int albumId = 0;
                String albumArt ="";
                do {
                    albumId = albumCursor.getInt(albumCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));
                    if (albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART) == -1)
                    {
                        Log.d("GETTINGALBUMS", "Here? 2");
                    }
                    else
                    {
                        albumArt = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    }
                    albumHash.put(albumId, albumArt);
                } while (albumCursor.moveToNext());
                albumCursor.close();
            }
            return songs;
        }

    }

}
