package com.example.jeff.musicplayer;

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

    private HashMap<String, Integer> songsPlayed = new HashMap<String, Integer>();
    private HashMap<String, Integer> artistPlayed = new HashMap<String, Integer>();

    public void updateSongCount(String song){
        int count = 1;

        if(songsPlayed.containsKey(song)){
            count += songsPlayed.get(song);
        }

        songsPlayed.put(song,count);

    }

    public void updateArtistCount(String artist){
        int count = 1;

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

        artistPlayed = sortByValues(artistPlayed);

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
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
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

}
