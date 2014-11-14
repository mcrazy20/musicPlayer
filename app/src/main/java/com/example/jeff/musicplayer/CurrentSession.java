package com.example.jeff.musicplayer;

import android.util.Log;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Rohit on 11/11/14.
 */
public class CurrentSession {

    public static HashMap<String, Integer> songsPlayed = new HashMap<String, Integer>();
    public static HashMap<String, Integer> artistPlayed = new HashMap<String, Integer>();

    public void updateSongCount(String song, int count){

        if(songsPlayed.containsKey(song)){
            count += songsPlayed.get(song);
        }

        songsPlayed.put(song,count);

    }

    public void updateArtistCount(String artist, int count){

        if(artistPlayed.containsKey(artist)){
            count += artistPlayed.get(artist);
        }

        artistPlayed.put(artist,count);

    }

    public JSONObject getSongs(){

        songsPlayed = sortByValues(songsPlayed);

        JSONObject obj = new JSONObject();
        try{
            int i = 0;
            for (String s:songsPlayed.keySet()){
                if (i<10)
                    obj.put(s,songsPlayed.get(s));
                i++;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return obj;
    }

    public JSONObject getArtists(){

        Log.d("Before","Sort");
        for(String s:artistPlayed.keySet()){
            System.out.println(s+"-->"+artistPlayed.get(s));
        }

        artistPlayed = sortByValues(artistPlayed);

        Log.d("After","Sort");
        for(String s:artistPlayed.keySet()){
            System.out.println(s+"-->"+artistPlayed.get(s));
        }

        JSONObject obj = new JSONObject();
        try{
            int i = 0;
            for (String s:artistPlayed.keySet()){
                if(i<10)
                    obj.put(s,artistPlayed.get(s));
                i++;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return obj;
    }

    private  HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public void setSession(String result) {

        Log.d("CurrentSession","INSIDE");

        String[] ar = result.split(":");

        try {

            for(int i=0;i<3;i++){
                ar[i] = ar[i].replace("=>",":");
                ar[i] = ar[i].replace("\\\"","\"");
                ar[i] = ar[i].substring(0,ar[i].length()-1);
                Log.d("JSON to convert",ar[i]);
            }

            ar[1] = ar[1].substring(7,ar[1].length()-2);
            ar[2] = ar[2].substring(9,ar[2].length()-1);

            Log.d("JSON convert",ar[1]);
            Log.d("JSON convert art",ar[2]);

            JSONObject songObject = new JSONObject(ar[1]);
            Log.d("Current Song", songObject.toString());
            JSONObject artistObject = new JSONObject(ar[2]);
            Log.d("Current Art", artistObject.toString());

            Iterator<?> songKeys = songObject.keys();
            Iterator<?> artistKeys = artistObject.keys();

            while (songKeys.hasNext()){
                String key = (String) songKeys.next();
                updateSongCount(key,(Integer)songObject.get(key));
            }

            while (artistKeys.hasNext()){
                String key = (String) artistKeys.next();
                updateArtistCount(key,(Integer)artistObject.get(key));
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
