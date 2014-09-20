package com.example.jeff.musicplayer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

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
          Toast.makeText(getApplicationContext(), "Lyrics Button Clicked", Toast.LENGTH_SHORT).show();
        }
      });

      //choose song button
      Button chooseSongButton = (Button)findViewById(R.id.chooseSongButton);
      chooseSongButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
          Toast.makeText(getApplicationContext(), "Choose Song Button Clicked", Toast.LENGTH_SHORT).show();
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
