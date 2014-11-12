package com.example.jeff.musicplayer;

/**
 * Created by J on 10/15/2014.
 */
public class Song {
    private String artist;
    private String path;
    private int albumId;

    public Song(String p, String a,int an)
    {
        artist = a;
        path = p;
        albumId = an;
    }

    public String getArtist()
    {
        return artist;
    }
    public String getPath()
    {
        return path;
    }
    public int getAlbumName()
    {
        return albumId;
    }
}
