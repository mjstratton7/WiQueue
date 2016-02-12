package com.mstratton.wiqueue;

import java.text.DecimalFormat;

public class SongObject {
    private static final String TAG = "SongObject";

    private String id;
    private String name;
    private String artist;
    private String album;
    private String time;
    private String path;

    public SongObject(String inId, String inName, String inArtist, String inAlbum, String inTime, String inPath) {
        id = inId;
        name = inName;
        artist = inArtist;
        album = inAlbum;
        path = inPath;

        // Convert int seconds to proper time - OLD METHOD
        int rawtime = Integer.parseInt(inTime);
        int min = 0;
        double sec = 0;

        // Divide to convert from ms to minutes
        min = (int)rawtime/60000;
        // Isolate the remainder, multiply by 60sec to get the seconds
        sec = ((double)rawtime/60000 - Math.floor(rawtime/60000)) * 60;

        // Format output of seconds to round to whole number, and force printing to 2 places
        DecimalFormat df = new DecimalFormat("00");
        time = min + ":" + df.format(sec);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getTime() {
        return time;
    }

    public String getPath() {
        return path;
    }

}
