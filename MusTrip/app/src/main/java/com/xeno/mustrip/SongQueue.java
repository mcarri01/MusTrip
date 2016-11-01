package com.xeno.mustrip;
import com.xeno.mustrip.Song;

import java.util.ArrayList;

/**
 * Created by davidbernstein on 10/11/16.
 */

public class SongQueue {
    public int numSongs;
    private ArrayList songQueue;
    public int currIndex;

    // Constructors
    public SongQueue() {
        numSongs = 0;
        songQueue = new ArrayList();
        currIndex = 0;
    }

    // Getters and setters
    public int getNumSongs() {return numSongs;}
    public void addSong(Song s) {
        songQueue.add(s);
        numSongs++;
    }

    public Song getCurrSong() {
        return (Song) (songQueue.get(currIndex));
    }

    public Song goBack() {
        if(currIndex != 0) {
            currIndex--;
        }
        return getCurrSong();
    }

    public Song goForward() {
        if(currIndex < numSongs - 1) {
            currIndex++;
        }
        return getCurrSong();
    }

    public ArrayList getSongList() {
        ArrayList list = new ArrayList();
        for(int i = 0; i < numSongs; i++) {
            list.add(i,songQueue.get(i).toString());
        }

        return list;
    }

}
