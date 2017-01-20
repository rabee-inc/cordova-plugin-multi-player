package com.eltonfaust.multiplayer;
import android.graphics.Bitmap;

public interface IRadioManager {

    void startRadio(String streamURL);
    void stopRadio();
    void setRadioVolume(int volume);

    boolean isPlaying();

    void registerListener(RadioListener mRadioListener);
    void unregisterListener(RadioListener mRadioListener);

    void setLogging(boolean logging);

    void connect();
    void disconnect();
}
