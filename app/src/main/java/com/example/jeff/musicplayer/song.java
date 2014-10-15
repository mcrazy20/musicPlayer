package com.example.jeff.musicplayer;

/**
 * Created by J on 10/15/2014.
 */
public class Song {
    private String artist;
    private String path;
    private String albumName;

    public Song(String p, String a, String an)
    {
        artist = a;
        path = p;
        albumName = an;
    }

    public String getArtist()
    {
        return artist;
    }
    public String getPath()
    {
        return path;
    }
    public String getAlbumName()
    {
        return albumName;
    }
}
