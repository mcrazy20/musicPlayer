package com.example.jeff.musicplayer;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;

import java.util.Arrays;

/**
 * Created by Rohit on 11/13/14.
 */
public class FacebookFragment extends Fragment {

    private static final String TAG = "MainFragment";

    private UiLifecycleHelper uiHelper;
    //private TextView userInfoTextView;
    private static GraphUser currUser;
    //private ProfilePictureView profilePictureView;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.facebook_layout, container, false);
        LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
        authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList("user_location", "user_birthday", "user_likes", "email"));
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.skip_home);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (isChecked){
                    Log.d("CheckBox","isChecked");
                    sharedPreferences.edit().putBoolean("pref_skip",true).apply();
                }
                else{
                    Log.d("CheckBox","isUNChecked");
                    sharedPreferences.edit().putBoolean("pref_skip",false).apply();
                }
            }
        });
        return view;
    }


    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in... yeah");

            //userInfoTextView.setVisibility(View.VISIBLE);

            Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        currUser = user;
                        Log.d("User Set to", currUser.toString());
                        // Display the parsed user info

                        //profilePictureView.setVisibility(ProfilePictureView.VISIBLE);
                        //profilePictureView.setProfileId(user.getId());

//                        s[2] = "facebook_id";
//                        s[3] =  currUser.getId();
//                        s[4] = "name";
//                        s[5] = currUser.getName();
//                        s[6] = "email";
//                        s[7] = (String) currUser.getProperty("email");

                        ((MainActivity) getActivity()).getSessionFromServer(currUser.getId(),currUser.getName(),(String) currUser.getProperty("email"));

                    }

                }
            });

            Log.d("FRagggg", "I am herereererere");
            ((MainActivity) getActivity()).hideFBFrag();

        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
            currUser = null;
            ((MainActivity) getActivity()).clearFBId();
            //userInfoTextView.setVisibility(View.INVISIBLE);
            //profilePictureView.setVisibility(ProfilePictureView.INVISIBLE);
            ((MainActivity)getActivity()).showFBFrag();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
}
