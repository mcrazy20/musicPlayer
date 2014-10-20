package com.example.jeff.musicplayer;

import android.app.Fragment;
import android.content.Context;
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
import android.widget.ListView;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

/**
 * Created by Rohit on 10/19/14.
 */
public class musicLoaderFragment extends Fragment {

    private MediaPlayer mMediaPlayer;
    private String[] aMusicList;
    private Hashtable<String, Song> musicHash;
    private Context context;
    private Stack<Integer> previousSongs;
    private int currentSong;
    private boolean shuffle = false;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.music_loader,container,false);
        mMediaPlayer = new MediaPlayer();
        ListView mListView = (ListView) view.findViewById(R.id.music_list);
        musicHash = new Hashtable<String, Song>();
        async test = new async();
        test.execute();
        return view;
    }

    public void musicLoader()
    {
        mMediaPlayer = new MediaPlayer();
        ListView mListView = (ListView) view.findViewById(R.id.music_list);
        musicHash = new Hashtable<String, Song>();
        async test = new async();
        test.execute();
    }

    public void onResume()
    {
        super.onResume();

        Log.d("MUSICLOADER", "ON RESUME");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("MUSICLOADER", "ON PAUSE");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("MUSICLOADER", "ON STOP");
    }

    private void playSong(String path) throws IllegalArgumentException,
            IllegalStateException, java.io.IOException {

        Log.d("ringtone", "playSong :: " + path);

        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(path);
//mMediaPlayer.setLooping(true);
        mMediaPlayer.prepare();

        mMediaPlayer.start();
    }

    public void nextSong(View v) throws IOException {
        if (!shuffle) {
            currentSong++;
            if (currentSong == aMusicList.length) {
                currentSong = 0;
            }

            //This is the musicHash giving back a song object then accessing the path;
            playSong(musicHash.get(aMusicList[currentSong]).getPath());
        }
        else
        {
            Random rand = new Random();
            currentSong = rand.nextInt((aMusicList.length) + 1);
            playSong(musicHash.get(aMusicList[currentSong]).getPath());
            previousSongs.push(currentSong);
        }
    }
    public void lastSong(View v) throws IOException {
        if (!shuffle) {
            currentSong--;
            if (currentSong < 0) {
                currentSong = aMusicList.length - 1;
            }
            playSong(musicHash.get(aMusicList[currentSong]).getPath());
        }
        else
        {
            if(previousSongs.empty())
            {
                playSong(musicHash.get(aMusicList[0]).getPath());
                currentSong=0;
                previousSongs.push(currentSong);
            }
            else
            {
                currentSong = previousSongs.pop();
                playSong(musicHash.get(aMusicList[0]).getPath());
            }
        }
    }

    public void shuffleMusic(View v)
    {
        shuffle = !shuffle;
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
            ListView testlist = (ListView) view.findViewById(R.id.music_list);
            ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(view.getContext(),
                    android.R.layout.simple_list_item_1, mMusicList);
            testlist.setAdapter(mAdapter);

            testlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                        long arg3) {
                    try {
                        playSong(musicHash.get(mMusicList[arg2]).getPath());
                        currentSong=arg2;
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (java.io.IOException e) {
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
