package com.example.jeff.musicplayer;

import android.annotation.TargetApi;
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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
    public Boolean inSettings = false;
    private Button lyric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting up the fragments onCreate
        frag = new musicLoaderFragment();
        profileFrag = new ProfileDisplayFragment();

        //Starting the Music Players
        Intent backgroundService = new Intent(this, MusicService.class);
        startService(backgroundService);
        bindService(backgroundService, mConnection, Context.BIND_AUTO_CREATE);

        //Setting up Shuffle and Shake methods
        shuffleStack = new Stack<Integer>();
        shaker = new ShakeListener(this);
        shaker.setOnShakeListener(this);
        lyric = (Button) findViewById(R.id.btn_lyrics);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

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


        //This creates our lyrics view and launches a Async Request to fetch the lyrics
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


        //This is finding the seek bar and running the runnable created earlier
        seek = (SeekBar)findViewById(R.id.mainSeekBar);
        seek.setOnSeekBarChangeListener(this);
        handler = new Handler();
        handler.removeCallbacks(moveSeekBarThread);
        handler.postDelayed(moveSeekBarThread, 100);
        currentSession = new CurrentSession();


        //Some required Facebook code
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

    /*On resume takes care of a lot of variables floating around during run time.
    It gets information saved during on pause and sets the app up to make it look just like it did before it was paused
     */
    protected void onResume()
    {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean fbSkip = sharedPreferences.getBoolean("pref_skip",false);
        if(fbSkip){
            hideFBFrag();
        }
        SharedPreferences share = this.getSharedPreferences("com.example.app", Context.MODE_PRIVATE);
        String songName = share.getString("songName", null);
        String albumPath = share.getString("albumPath", null);
        TextView tv = (TextView) findViewById(R.id.layout_current_song);
        if (songName != null) {
            tv.setText(songName);
        }
        ImageView img = (ImageView)findViewById(R.id.img_albumart);
        if (albumPath!= null) {
            Bitmap bitmap = BitmapFactory.decodeFile(albumPath);
            img.setImageBitmap(bitmap);
        }
        else
        {
            img.setImageDrawable(getResources().getDrawable(R.drawable.music));
        }

        Log.d("MAIN", "ON RESUME");
    }
    /*
    onPause sends the current profile information up to our server and then saves some local variables for when the app is reopened
     */
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

        SharedPreferences share = this.getSharedPreferences("com.example.app", Context.MODE_PRIVATE);
        if (currentSong >= 0) {
            String songName = changeCurrentSongName(currentSong);
            String albumPath = changeAlbumArt(currentSong);
            share.edit().putString("songName", songName).apply();
            share.edit().putString("albumPath", albumPath).apply();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onBackPressed() {
        //This is used to kill fragments in a nice way.
        if(inSettings){
           inSettings = false;
           getFragmentManager().popBackStack();
        }
        super.onBackPressed();
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
            inSettings = true;
            showSettingsFrag();
            return true;
        }
        else if (id == R.id.facebook_logout) {
            showFBFrag();
            return true;
        }
        else if (id == R.id.action_lyrics) {
            lyric.performClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettingsFrag() {

        //This is used to show our settings menu
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .addToBackStack("Settings")
                .commit();

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

    //This function shows the profile information fragment
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

    //This is called whenver we need to pop out Profile fragment from current activity
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

    //This function is used to update the SongName inside the app UI

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected String changeCurrentSongName(int index){
        playButton.setBackground(getResources().getDrawable(R.drawable.pause));
        String name = "";
        if (mMusicList != null) {
            name = mMusicList[index];
            currentSession.updateSongCount(name, 1);
            String artist = musicHash.get(mMusicList[index]).getArtist();
            currentSession.updateArtistCount(artist, 1);
            name += " - " + artist;
            TextView tv = (TextView) findViewById(R.id.layout_current_song);
            tv.setText(name);
            ImageView albumArt = (ImageView) findViewById(R.id.img_albumart);
            FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_placeholder);
            LyricsFragment lyricsFrag = new LyricsFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            albumArt.setVisibility(ImageView.VISIBLE);
            fl.setVisibility(FrameLayout.GONE);
            fragmentTransaction.hide(lyricsFrag).commit();
        }
        return name;

    }

    //This updates the album art inside of the UI
    private String changeAlbumArt(int index)
    {
        String albumPath = "";
        if (mMusicList != null) {
            int albumId = musicHash.get(mMusicList[index]).getAlbumName();
            albumPath = albumHash.get(albumId);
            ImageView img = (ImageView) findViewById(R.id.img_albumart);
            // Log.d("CHANGEALBUMART", albumPath);
            if (albumPath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(albumPath);
                //bitmap=Bitmap.createScaledBitmap(bitmap, 500,500, true);
                img.setImageBitmap(bitmap);
            } else {
                img.setImageDrawable(getResources().getDrawable(R.drawable.music));
            }
        }
        return albumPath;
    }

    public void songFromList(int index) throws IOException{
        changeCurrentSongName(index);
        mService.setPathOfSong(musicHash.get(mMusicList[index]).getPath());
    }
    //This moves to the previous song
    public void previousSong(View V)
    {
        //All of these if statements are used to make sure all the information the app needs is available
        if (mMusicList != null) {
            if (shuffle) {
                if (mBound) {

                    //Changing the song/album art
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

            } else {
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
    }

    //This function is used by the play button
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void playSong(View V)
    {
        if (mBound) {
            if (!mService.isMusicPlaying()) {
                if (paused)
                {
                    playButton.setBackground(getResources().getDrawable(R.drawable.pause));
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
                playButton.setBackground(getResources().getDrawable(R.drawable.play));
                mService.pauseMusic();
                paused=true;
            }
        }
    }

    //This function skips to the next song
    public void nextSong(View V)
    {
        if (mMusicList != null) {
            if (shuffle) {
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

            } else {
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
    }

    //This turns shuffle on/off
    public void shuffleSongs(View V)
    {
        shuffle = !shuffle;
        Button shuffleB = (Button) findViewById(R.id.shuffleButton);
        if (shuffle)
        {
            shuffleB.setBackgroundResource(R.drawable.shuffleon);
        }
        else
        {
            shuffleB.setBackgroundResource(R.drawable.shuffle);
        }
    }
        //Used by the seekbar to update often
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
    //Used by the shake listener
    public void onShake() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String pref = sharedPreferences.getString("pref_shake","Shuffle");
        if(pref.equals("Shuffle")){
            shuffleSongs(findViewById(R.id.shuffleButton));
        }
        else{
            lyric.performClick();
        }

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
    //Used by facebook for sessions
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

    public void hideFbFrag(View view) {
        hideFBFrag();
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
    //This function updates the lyrics inside of the fragment
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

    // This is used to hide the facebook fragment and not kill it
    public void hideFBFrag(){
        Log.d("MainActivity", "Hidiiiing");
        getSupportFragmentManager().beginTransaction().hide(mainFragment).commit();
    }

    // This is used to show a previously hidden facebook fragment
    public void showFBFrag(){
        Log.d("MainActivity","Showing");
        getSupportFragmentManager().beginTransaction().show(mainFragment).commit();
    }


}
