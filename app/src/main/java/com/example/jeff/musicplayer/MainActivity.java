package com.example.jeff.musicplayer;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.ImageView;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener, ShakeListener.OnShakeListener {
    private static String[] mMusicList;
    private FragmentTransaction ft;
    private musicLoaderFragment frag;
    private ProfileDisplayFragment profileFrag;
    public static MusicService mService;
    boolean mBound = false;
    public static Hashtable<String, Song> musicHash;
    public static HashMap<Integer, String> albumHash;
    public static int currentSong = -1;
    public int lyricsOfSong = -1;
    private boolean paused = false;
    private SeekBar seek;
    private Handler handler;
    private Button playButton;
    private Stack<Integer> shuffleStack;
    private boolean shuffle = false;
    Random rand = new Random();
    ShakeListener shaker;
    private static CurrentSession currentSession;
    private String serverURL = "https://musicplayerserver.herokuapp.com/";
    private String lyricsURL = "http://lyrics.wikia.com/api.php?";
    private FacebookFragment mainFragment;
    private String facebookId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frag = new musicLoaderFragment();
        profileFrag = new ProfileDisplayFragment();
        Intent backgroundService = new Intent(this, MusicService.class);
        startService(backgroundService);
        bindService(backgroundService, mConnection, Context.BIND_AUTO_CREATE);
        shuffleStack = new Stack<Integer>();
        shaker = new ShakeListener(this);
        shaker.setOnShakeListener(this);


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
        Button lyricsButton = (Button) findViewById(R.id.btn_lyrics);

        lyricsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView albumArt= (ImageView) findViewById(R.id.img_albumart);
                FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_placeholder);
                LyricsFragment lyricsFrag = new LyricsFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                if(albumArt.getVisibility() != ImageView.GONE){
                    albumArt.setVisibility(ImageView.GONE);
                    fl.setVisibility(FrameLayout.VISIBLE);
                    fragmentTransaction.add(R.id.fragment_placeholder,lyricsFrag).commit();
                    if(lyricsOfSong != currentSong){
                        try{
                            String[] s = new String[2];
                            s[0] = lyricsURL+"artist=";
                            String artist = musicHash.get(mMusicList[currentSong]).getArtist();
                            s[0] += URLEncoder.encode(artist,"utf-8");
                            s[0] += "&song=";
                            String song = mMusicList[currentSong];
                            s[0] += URLEncoder.encode(song,"utf-8");
                            s[0] += "&fmt=realjson";
                            Log.d("URL req",s[0]);
                            s[1] = "GET";
                            new HttpAsync().execute(s);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        finally {
                            lyricsOfSong = currentSong;
                        }
                    }
                }
                else{
                    albumArt.setVisibility(ImageView.VISIBLE);
                    fl.setVisibility(FrameLayout.GONE);
                    fragmentTransaction.hide(lyricsFrag).commit();
                }
            }
        });


        //This is finding it and running the runnable created earlier
        seek = (SeekBar)findViewById(R.id.mainSeekBar);
        seek.setOnSeekBarChangeListener(this);
        handler = new Handler();
        handler.removeCallbacks(moveSeekBarThread);
        handler.postDelayed(moveSeekBarThread, 100);
        currentSession = new CurrentSession();
//        String[] s = new String[8];
//        s[0] = serverURL+"log_user";
//        s[1] = "POST";
//        s[2] = "facebook_id";
//        s[3] = "1234";
//        s[4] = "name";
//        s[5] = "Roka";
//        s[6] = "email";
//        s[7] = "test@test.com";
//        new HttpAsync().execute(s);
        if (savedInstanceState == null) {
            // Add the fragment on initial activity setup
            mainFragment = new FacebookFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).addToBackStack("FB").commit();
        } else {
            // Or set the fragment from restored state info
            mainFragment = (FacebookFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
        }
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
        s[7] = facebookId;
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
        if (id == R.id.facebook_logout) {
            showFBFrag();
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

    public void moveToSessionData(View V)
    {
        Log.d("Fragment", "Inside Fragment");
        FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_profiledisplay);
        fl.setVisibility(FrameLayout.VISIBLE);
        Log.d("Fragment", "Should be visible");
        ft = getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        ft.add(R.id.fragment_profiledisplay, profileFrag, "Profile").addToBackStack("Profile").commit();
    }

    //This is called inside the fragment to make sure our fragment disappears when the user is done
    public void hideTheFrag(){
        getFragmentManager().popBackStack();
        FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_musicloader);
        fl.setVisibility(FrameLayout.GONE);
    }
    public void hideProfileFrag(){
        getFragmentManager().popBackStack();
        FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_profiledisplay);
        fl.setVisibility(FrameLayout.GONE);
    }

    //This gets the tables from musicLoaderFragment before it is destroyed so we have the information
    public static void updateTables()
    {
        musicHash = musicLoaderFragment.musicHash;
        mMusicList = musicLoaderFragment.aMusicList;
        albumHash = musicLoaderFragment.albumHash;

    }

    //This is used for binding the music service to the UI thread
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.MyBinder binder = (MusicService.MyBinder) service;
            mService = binder.getService();
            mService.getmMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.reset();
                    Log.d("ONCOMPLETETIONLISTENER", "Doing this?");
                    nextSong(findViewById(R.id.btn_next));
                }
            });
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
        currentSession.updateSongCount(name,1);
        String artist = musicHash.get(mMusicList[index]).getArtist();
        currentSession.updateArtistCount(artist,1);
        name += " - " + artist;
        TextView tv = (TextView) findViewById(R.id.layout_current_song);
        tv.setText(name);
        ImageView albumArt= (ImageView) findViewById(R.id.img_albumart);
        FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_placeholder);
        LyricsFragment lyricsFrag = new LyricsFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        albumArt.setVisibility(ImageView.VISIBLE);
        fl.setVisibility(FrameLayout.GONE);
        fragmentTransaction.hide(lyricsFrag).commit();
    }

    private void changeAlbumArt(int index)
    {
        int albumId = musicHash.get(mMusicList[index]).getAlbumName();
        String albumPath = albumHash.get(albumId);
        ImageView img = (ImageView)findViewById(R.id.img_albumart);
       // Log.d("CHANGEALBUMART", albumPath);
        if (albumPath!= null) {
            Bitmap bitmap = BitmapFactory.decodeFile(albumPath);
            //bitmap=Bitmap.createScaledBitmap(bitmap, 500,500, true);
            img.setImageBitmap(bitmap);
        }
        else
        {
            img.setImageDrawable(getResources().getDrawable(R.drawable.music));
        }

    }

    public void songFromList(int index) throws IOException{
        changeCurrentSongName(index);
        mService.setPathOfSong(musicHash.get(mMusicList[index]).getPath());
    }

    public void previousSong(View V)
    {
        //updateTables();
        if (shuffle)
        {
            if (mBound) {
                currentSong = rand.nextInt(mMusicList.length);
                changeAlbumArt(currentSong);
                changeCurrentSongName(currentSong);
                try {
                    if (!(musicHash == null || mMusicList == null)) {
                        mService.setPathOfSong(musicHash.get(mMusicList[currentSong]).getPath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                shuffleStack.push(currentSong);
            }

        }
        else {
            currentSong--;
            if (currentSong < 0) {
                currentSong = mMusicList.length - 1;
            }
            changeAlbumArt(currentSong);
            changeCurrentSongName(currentSong);
            if (mBound) {
                if (mService.isMusicPlaying()) {
                    try {
                        mService.setPathOfSong(musicHash.get(mMusicList[currentSong]).getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        if (shuffle)
        {
            if (mBound) {
            currentSong = rand.nextInt(mMusicList.length);
            changeAlbumArt(currentSong);
            changeCurrentSongName(currentSong);
                    try {
                        if (!(musicHash == null || mMusicList == null)) {
                            mService.setPathOfSong(musicHash.get(mMusicList[currentSong]).getPath());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                shuffleStack.push(currentSong);
            }

        }
        else {
            if (mBound) {
                currentSong++;
                if (currentSong == mMusicList.length) {
                    currentSong = 0;
                }
                changeCurrentSongName(currentSong);
                changeAlbumArt(currentSong);
                if (mBound) {
                    //if (mService.isMusicPlaying()) {
                    try {
                        if (!(musicHash == null || mMusicList == null)) {
                            mService.setPathOfSong(musicHash.get(mMusicList[currentSong]).getPath());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }
    public void shuffleSongs(View V)
    {
        shuffle = !shuffle;
        Button shuffleB = (Button) findViewById(R.id.shuffleButton);
        if (shuffle)
        {
            shuffleB.setBackgroundColor(Color.GREEN);
        }
        else
        {
            shuffleB.setBackgroundColor(Color.RED);
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

    @Override
    public void onShake() {
        shuffleSongs(findViewById(R.id.shuffleButton));
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

    public void getSessionFromServer(String fbID, String fbName, String fbemail) {

        facebookId = fbID;

        String[] s = new String[8];
        s[0] = serverURL+"log_user";
        s[1] = "POST";
        s[2] = "facebook_id";
        s[3] = fbID;
        s[4] = "name";
        s[5] = fbName;
        s[6] = "email";
        s[7] = fbemail;
        new HttpAsync().execute(s);

    }

    public void clearFBId() {

        facebookId = "";

    }

    private class HttpAsync extends AsyncTask<String, Void, String> {

            int flag = 0;

            @Override
            protected String doInBackground(String... strings) {
                if(strings[1]=="GET"){
                    flag = 1;
                    return GET(strings[0]);
                }
                else{
                    return POST(strings);
                }
            }

            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(String result) {
                Log.d("HEROKU",result);
                if(flag!=1){
                    if(result.length()>0)
                        currentSession.setSession(result);
                }
                else {
                    setLyrics(result);
                }
                flag = 0;
            }
        }

    private void setLyrics(String result) {

        try{
            JSONObject lyrics = new JSONObject(result);
            Log.d("LyricsObject",lyrics.toString());
            TextView lyricsView = (TextView) findViewById(R.id.txt_lyrics);
            lyricsView.setText((String)lyrics.get("lyrics"));
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void hideFBFrag(){
        Log.d("MainActivity", "Hidiiiing");
        getSupportFragmentManager().beginTransaction().hide(mainFragment).commit();
    }

    public void showFBFrag(){
        Log.d("MainActivity","Showing");
        getSupportFragmentManager().beginTransaction().show(mainFragment).commit();
    }
}
