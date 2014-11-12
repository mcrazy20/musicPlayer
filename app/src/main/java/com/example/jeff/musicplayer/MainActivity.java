package com.example.jeff.musicplayer;

import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;

    public class MainActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener{
    private static String[] mMusicList;
    private FragmentTransaction ft;
    private musicLoaderFragment frag;
    public static MusicService mService;
    boolean mBound = false;
    public static Hashtable<String, Song> musicHash;
    public static int currentSong = 0;
    private boolean paused = false;
    //public IBinder musicServiceBinder;
    private SeekBar seek;
    private Handler handler;
    private Button playButton;
    private static CurrentSession currentSession;
    private String serverURL = "https://musicplayerserver.herokuapp.com/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frag = new musicLoaderFragment();
        Intent backgroundService = new Intent(this, MusicService.class);
        startService(backgroundService);
        bindService(backgroundService, mConnection, Context.BIND_AUTO_CREATE);

        //This is used to update the seekbar
        Runnable moveSeekBarThread = new Runnable() {

            public void run() {
                if (mBound) {
                    if (mService.isMusicPlaying()) {
                        int mediaPos_new = mService.getMCurrentPosition();
                        int mediaMax_new = mService.getMDuration();
                        seek.setMax(mediaMax_new);
                        seek.setProgress(mediaPos_new);
                    }
                }
                handler.postDelayed(this, 100); //Looping the thread after 0.1 second
                // seconds
            }
        };

        playButton = (Button) findViewById(R.id.btn_play);

        //This is finding it and running the runnable created earlier
        seek = (SeekBar)findViewById(R.id.mainSeekBar);
        seek.setOnSeekBarChangeListener(this);
        handler = new Handler();
        handler.removeCallbacks(moveSeekBarThread);
        handler.postDelayed(moveSeekBarThread, 100);
        currentSession = new CurrentSession();
        String[] s = new String[8];
        s[0] = serverURL+"log_user";
        s[1] = "POST";
        s[2] = "facebook_id";
        s[3] = "1234";
        s[4] = "name";
        s[5] = "Roka";
        s[6] = "email";
        s[7] = "test@test.com";
        new HttpAsync().execute(s);
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
        String[] s = new String[8];
        s[0] = serverURL+"update_session";
        s[1] = "POST";
        JSONObject songObject = currentSession.getSongs();          //Getting Top 10 songs
        JSONObject artistObject = currentSession.getArtists();      //Getting Top 10 artists
        s[2] = "songs";
        s[3] = songObject.toString();
        Log.d("songDATA",s[3]);
        s[4] = "artists";
        s[5] = artistObject.toString();
        s[6] = "facebook_id";
        s[7] = "1234";
        Log.d("artistDATA",s[5]);
        new HttpAsync().execute(s);                                 //Updating the Heroku Server
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void rebindService()
    {
        Intent backgroundService = new Intent(this, MusicService.class);
        bindService(backgroundService, mConnection, Context.BIND_AUTO_CREATE);
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

    //This code is used to open our musicLoaderFragment
    public void moveToMusic(View V)
    {
        Log.d("Fragment", "Inside Fragment");
        FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_musicloader);
        fl.setVisibility(FrameLayout.VISIBLE);
        Log.d("Fragment", "Should be visible");
        ft = getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        ft.add(R.id.fragment_musicloader, frag, "MusicList").addToBackStack("MusicList").commit();
    }

    //This is called inside the fragment to make sure our fragment disappears when the user is done
    public void hideTheFrag(){
        getFragmentManager().popBackStack();
        FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_musicloader);
        fl.setVisibility(FrameLayout.GONE);
    }

    //This gets the tables from musicLoaderFragment before it is destroyed so we have the information
    public static void updateTables()
    {
        musicHash = musicLoaderFragment.musicHash;
        mMusicList = musicLoaderFragment.aMusicList;

    }

    //This is used for binding the music service to the UI thread
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

    protected void changeCurrentSongName(int index){
        if(mService.isMusicPlaying()){
            playButton.setText("Pause");
        }
        String name = mMusicList[index];
        currentSession.updateSongCount(name);
        String artist = musicHash.get(mMusicList[index]).getArtist();
        currentSession.updateArtistCount(artist);
        name += " - " + artist;
        TextView tv = (TextView) findViewById(R.id.layout_current_song);
        tv.setText(name);

    }

    public void songFromList(int index) throws IOException{
        changeCurrentSongName(index);
        mService.setPathOfSong(musicHash.get(mMusicList[index]).getPath());
    }

    public void previousSong(View V)
    {
        //updateTables();
        currentSong--;
        if (currentSong < 0)
        {
            currentSong=mMusicList.length-1;
        }
        changeCurrentSongName(currentSong);
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
        if (mBound) {
            if (!mService.isMusicPlaying()) {
                if (paused)
                {
                    playButton.setText("Pause");
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
                playButton.setText("Play");
                mService.pauseMusic();
                paused=true;
            }
        }
    }
    public void nextSong(View V)
    {
        //updateTables();
        currentSong++;
        if (currentSong == mMusicList.length)
        {
           currentSong=0;
        }
        changeCurrentSongName(currentSong);
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

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (b)
            {
                Log.d("IN ON PROGRESS", "SEEKING: "+i);
                mService.changeMPosition(i);
                seek.setProgress(i);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        public static String POST(String... strings){

            InputStream inputStream = null;
            String result = "";

            try {
                HttpPost request = new HttpPost(strings[0]);

                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

                for(int i = 2; i<strings.length; i=i+2){
                    if(strings[i+1]!=null)
                        params.add(new BasicNameValuePair(strings[i], strings[i + 1]));
                }

                if(!params.isEmpty()){
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                }

                Log.d("ArrayList",params.toString());

                HttpClient client = new DefaultHttpClient();

                HttpResponse httpResponse = client.execute(request);

                inputStream = httpResponse.getEntity().getContent();

                // convert inputstream to string
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";

            }
            catch (Exception e){
                e.printStackTrace();
            }

            return result;
        }

        public static String GET(String url){

            InputStream inputStream = null;
            String result = "";
            try{
                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // make GET request to the given URL
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // convert inputstream to string
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";
            }
            catch (Exception e){
                e.printStackTrace();
            }

            return result;
        }

        private static String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

        }

        private class HttpAsync extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... strings) {
                if(strings[1]=="GET"){
                    return GET(strings[0]);
                }
                else{
                    return POST(strings);
                }
            }

            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(String result) {
                //Toast.makeText(getApplicationContext(),"Received!", Toast.LENGTH_LONG).show();
                Log.d("HEROKU",result);
            }
        }

    }