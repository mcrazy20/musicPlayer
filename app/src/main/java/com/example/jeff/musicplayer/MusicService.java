package com.example.jeff.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener{

    private static MediaPlayer mMediaPlayer = new MediaPlayer();
    private static String pathOfSong;
    //private static final String TAG = ((Object)this).getClass().getSimpleName();
    private static String TAG = "MusicService";
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
        Log.d("STUFF", "In on prepared");
        mediaPlayer.start();
        if (!mediaPlayer.isPlaying())
        {
            Log.d("DAFUQ", "WHY NO PLAY?");
        }
    }

    @Override
    public int onStartCommand(Intent i, int q, int r)
    {
        Log.d(TAG, "In on start");
       /* mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Intent intent = new Intent("song-finished");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        });*/
        mMediaPlayer.setOnPreparedListener(this);
        return 0;
    }

    public static void setPathOfSong(String path) throws IOException {

        pathOfSong=path;
        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(path);
        Log.d(TAG,"Starting new song");
        mMediaPlayer.prepareAsync();
        //mMediaPlayer.start();

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

    public static int getMDuration()
    {
        return mMediaPlayer.getDuration();
    }

    public static int getMCurrentPosition()
    {
        return mMediaPlayer.getCurrentPosition();
    }

    public static void changeMPosition(int i)
    {
        mMediaPlayer.seekTo(i);
    }

    public static boolean isMusicPlaying()
    {
        return mMediaPlayer.isPlaying();
    }

    public static MediaPlayer getmMediaPlayer()
    {
        return mMediaPlayer;
    }
}
