package com.xeno.MusTrip;
import com.xeno.MusTrip.Song;

import java.util.ArrayList;

/**
 * Created by david bernstein on 10/11/16.
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
