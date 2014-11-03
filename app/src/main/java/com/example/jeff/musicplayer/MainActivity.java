package com.example.jeff.musicplayer;

import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.MediaController;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;


public class MainActivity extends ActionBarActivity{
    private static String[] mMusicList;
    private FragmentTransaction ft;
    private musicLoaderFragment frag;
    MusicService mService;
    boolean mBound = false;
    public static Hashtable<String, Song> musicHash;
    public static int currentSong = 0;
    private boolean paused = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frag = new musicLoaderFragment();
        Intent backgroundService = new Intent(this, MusicService.class);
        startService(backgroundService);
        bindService(backgroundService, mConnection, Context.BIND_AUTO_CREATE);
        Log.d("MAIN", "ON CREATE");

    }

    protected void onResume()
    {
        super.onResume();

        Log.d("MAIN", "ON RESUME");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MAIN", "ON PAUSE");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void moveToMusic(View V)
    {
        Log.d("Fragment", "Inside Fragment");
        FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_musicloader);
        fl.setVisibility(FrameLayout.VISIBLE);
        Log.d("Fragment", "Should be visible");
        ft = getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        ft.add(R.id.fragment_musicloader, frag, "MusicList").addToBackStack("MusicList").commit();
    }

    public void hideTheFrag(){
        getFragmentManager().popBackStack();
        FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_musicloader);
        fl.setVisibility(FrameLayout.GONE);
    }


    public static void updateTables()
    {
        musicHash = musicLoaderFragment.musicHash;
        mMusicList = musicLoaderFragment.aMusicList;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.MyBinder binder = (MusicService.MyBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void previousSong(View V)
    {
        updateTables();
        currentSong++;
        if (currentSong == musicHash.size())
        {
            currentSong=0;
        }
        if (mBound)
        {
            if (mService.isMusicPlaying())
            {
                try {
                    mService.setPathOfSong(musicHash.get(mMusicList[currentSong]).getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void playSong(View V)
    {
        updateTables();
        if (mBound) {
            if (!mService.isMusicPlaying()) {
                if (paused)
                {
                    mService.resumeMusic();
                }
                else {
                    try {
                        if (!(musicHash ==null || mMusicList == null)) {
                            mService.setPathOfSong(musicHash.get(mMusicList[currentSong]).getPath());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                mService.pauseMusic();
                paused=true;
            }
        }
    }
    public void nextSong(View V)
    {
        updateTables();
        currentSong--;
        if (currentSong < 0)
        {
           currentSong=musicHash.size()-1;
        }
        if (mBound)
        {
            if (mService.isMusicPlaying())
            {
                try {
                    if (!(musicHash ==null || mMusicList == null)) {
                        mService.setPathOfSong(musicHash.get(mMusicList[currentSong]).getPath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}