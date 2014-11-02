package com.example.jeff.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener{

    private static MediaPlayer mMediaPlayer = new MediaPlayer();
    private static String pathOfSong;
    private final String TAG = ((Object)this).getClass().getSimpleName();
    private final IBinder binder = new MyBinder();
    public class MyBinder extends Binder{
        MusicService getService()
        {
            return MusicService.this;
        }
    }



        @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "Calling on Bind");
        return binder;

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "In on prepared");
        mMediaPlayer.start();
    }

    @Override
    public int onStartCommand(Intent i, int q, int r)
    {
        Log.d(TAG, "In on start");
        return 0;
    }
    public static void setPathOfSong(String path) throws IOException {
        pathOfSong=path;
        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(path);
        //mMediaPlayer.prepareAsync();
        mMediaPlayer.prepare();
        mMediaPlayer.start();
    }

    public static void pauseMusic()
    {
        if (mMediaPlayer.isPlaying())
        {
            mMediaPlayer.pause();
        }
    }
    public static void resumeMusic()
    {
        if (!mMediaPlayer.isPlaying())
        {
            mMediaPlayer.start();
        }
    }

    public static boolean isMusicPlaying()
    {
        return mMediaPlayer.isPlaying();
    }
}
