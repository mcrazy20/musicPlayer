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
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

/**
 * Created by Rohit on 10/19/14.
 */
public class musicLoaderFragment extends Fragment {

    public static String[] aMusicList;
    public static Hashtable<String, Song> musicHash;
    private View view;
    private String TAG = "MusicLoaderFrag";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.music_loader,container,false);
        musicHash = new Hashtable<String, Song>();
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
                        MainActivity.mService.setPathOfSong(musicHash.get(mMusicList[arg2]).getPath());
                        MainActivity.currentSong=arg2;
                        ((MainActivity)getActivity()).hideTheFrag();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            aMusicList = mMusicList;

        }

        public String[] getMusic() {
            final Cursor mCursor = view.getContext().getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA}, null, null,
                    "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

            int count = mCursor.getCount();

            String[] songs = new String[count];
            String[] mAudioPath = new String[count];
            int i = 0;
            if (mCursor.moveToFirst()) {
                do {
                    String artist;
                    String albumName;
                    String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if (mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST) == -1) {
                        artist = "";
                    } else {
                        artist = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    }

                    if (mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM) == -1) {
                        albumName = "";
                    } else {
                        albumName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    }
                    Song song = new Song(path, artist, albumName);
                    songs[i] = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                    musicHash.put(songs[i], song);
                    i++;
                } while (mCursor.moveToNext());
            }

            mCursor.close();
            return songs;
        }

    }

}
