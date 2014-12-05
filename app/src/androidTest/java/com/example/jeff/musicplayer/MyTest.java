package com.example.jeff.musicplayer;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.Button;

import com.robotium.solo.Solo;

/**
 * Created by andy on 141201.
 */
public class MyTest extends ActivityInstrumentationTestCase2<MainActivity> {
  private Solo solo;
  private MainActivity activity;
  public MyTest() {
    super(MainActivity.class);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    solo = new Solo(getInstrumentation(), getActivity());
    activity = getActivity();
  }
  @Override
  protected void tearDown() throws Exception {
    solo.finishOpenedActivities();
    super.tearDown();
  }

  public void testTheApp() throws Exception {
    //test main activity is shown (this assumes skipping facebook login for first time app is run)
    solo.assertCurrentActivity("wrong activity", MainActivity.class);
    //show music list
    solo.clickOnView(solo.getView(R.id.main_button));

    solo.clickInList(4);
    //click home button from music loader
    solo.clickOnView(solo.getView(R.id.btn_song_list_back));
    //show lyrics
    solo.clickOnView(solo.getView(R.id.btn_lyrics));
    //go to next song
    solo.clickOnView(solo.getView(R.id.btn_next));
    //hide lyrics
    solo.clickOnView(solo.getView(R.id.btn_lyrics));
    //go to next song
    solo.clickOnView(solo.getView(R.id.btn_next));
    //turn on random
    solo.clickOnView(solo.getView(R.id.shuffleButton));
    //go to next random song
    solo.clickOnView(solo.getView(R.id.btn_next));
    //go back a song
    solo.clickOnView(solo.getView(R.id.btn_previous));
    //go to profile page
    solo.clickOnView(solo.getView(R.id.profiledisplay));
    //go back to main menu
    solo.clickOnView(solo.getView(R.id.backToMain));
    //pause music
    solo.clickOnView(solo.getView(R.id.btn_play));
    //play music
    solo.clickOnView(solo.getView(R.id.btn_play));
    //pause music
    solo.clickOnView(solo.getView(R.id.btn_play));
    //play music
    solo.clickOnView(solo.getView(R.id.btn_play));
  }
}
