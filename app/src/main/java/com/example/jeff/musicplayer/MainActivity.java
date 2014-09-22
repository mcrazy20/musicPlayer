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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    EditText songName;
    EditText artistName;
    HttpClient httpclient = new DefaultHttpClient();
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
            String URL = "http://lyricfind.com/api_service/lyric.do?apikey=2233d1d669999ce64ee0eb073d6da191&reqtype=default&trackid=elid:3d294a4831babc7d57169ecda7117a16";
            try
            {
                HttpResponse response = httpclient.execute(new HttpGet(URL));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    String responseString = out.toString();
                    out.close();
                    Toast.makeText(getApplicationContext(), responseString.toString(), Toast.LENGTH_SHORT).show();
                    Log.d(MainActivity.class.getSimpleName(), responseString);
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }

            }
            catch(Exception e)
            {

            }
            Toast.makeText(getApplicationContext(), songName.getText(), Toast.LENGTH_SHORT).show();
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