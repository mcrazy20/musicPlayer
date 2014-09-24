package com.example.jeff.musicplayer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    EditText songName;
    EditText artistName;
    //HttpClient httpclient = new DefaultHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //pause button
      Button pauseButton = (Button)findViewById(R.id.pauseButton);
      pauseButton.setOnClickListener(new View.OnClickListener()
      {
        @Override
        public void onClick(View arg0)
        {
          Toast.makeText(getApplicationContext(), "Pause Button Clicked", Toast.LENGTH_LONG).show();
        }
      });

      //stop button
      Button stopButton = (Button)findViewById(R.id.stopButton);
      stopButton.setOnClickListener(new View.OnClickListener()
      {
        @Override
        public void onClick(View arg0)
        {
          Toast.makeText(getApplicationContext(), "Stop Button Clicked", Toast.LENGTH_SHORT).show();
        }
      });

      //next button
      Button nextButton = (Button)findViewById(R.id.nextButton);
      nextButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
          Toast.makeText(getApplicationContext(), "Next Button Clicked", Toast.LENGTH_SHORT).show();
        }
      });

      //prev button
      Button prevButton = (Button)findViewById(R.id.prevButton);
      prevButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
          Toast.makeText(getApplicationContext(), "Previous Button Clicked", Toast.LENGTH_SHORT).show();
        }
      });

      //lyrics button
      Button lyricsButton = (Button)findViewById(R.id.lyricsButton);
      lyricsButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
            Log.d(MainActivity.class.getSimpleName(), "TEST LOG");
            songName = (EditText)findViewById(R.id.songTextbox);
            artistName = (EditText)findViewById(R.id.artistTextbox);
            new RequestTask().execute("http://developer.echonest.com/api/v4/song/search?api_key=SG9MKIPWAVBJV83SU&format=json&artist=radiohead&title=creep&bucket=id:lyricfind-US&limit=true&bucket=tracks");
//            String URL = "http://developer.echonest.com/api/v4/song/search?api_key=SG9MKIPWAVBJV83SU&format=json&artist=radiohead&title=creep&bucket=id:lyricfind-US&limit=true&bucket=tracks";
//            try
//            {
//                HttpResponse response = httpclient.execute(new HttpGet(URL));
//                StatusLine statusLine = response.getStatusLine();
//                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
//                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                    response.getEntity().writeTo(out);
//                    String responseString = out.toString();
//                    out.close();
//                    Log.d(MainActivity.class.getSimpleName(), "OUT IS CLOSED");
//                    Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_SHORT).show();
//                    Log.d(MainActivity.class.getSimpleName(), responseString);
//                } else{
//                    //Closes the connection.
//                    response.getEntity().getContent().close();
//                    throw new IOException(statusLine.getReasonPhrase());
//                }
//
//            }
//            catch(Exception e)
//            {
//                Log.d(MainActivity.class.getSimpleName(), e.toString());
//            }
            //Toast.makeText(getApplicationContext(), songName.getText(), Toast.LENGTH_SHORT).show();
        }
      });

      //choose song button
      Button chooseSongButton = (Button)findViewById(R.id.chooseSongButton);
      chooseSongButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
            Toast.makeText(getApplicationContext(), "song button pressed", Toast.LENGTH_SHORT).show();

        }
      });
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
}

class RequestTask extends android.os.AsyncTask<String, String, String>{

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                Log.d(MainActivity.class.getSimpleName(), responseString);
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        } catch (IOException e) {
            //TODO Handle problems..
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..
    }
}