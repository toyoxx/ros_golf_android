package jp.ac.tohoku.mech.srd.dsquiz;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by Toyo on 6/1/2015.
 */
public class NoisePlayer {
    private static NoisePlayer ourInstance = new NoisePlayer();
    MediaPlayer player;

    private NoisePlayer() {

    }

    public static NoisePlayer getInstance() {
        return ourInstance;
    }

    public void playNoise(Context ctx) {
        try {
            AssetFileDescriptor afd = ctx.getAssets().openFd("PinkNoise_64kb.mp3");
            player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopNoise() {
        if(player != null)
            player.stop();
    }
}
