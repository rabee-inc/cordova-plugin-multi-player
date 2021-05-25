package com.eltonfaust.multiplayer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.exoplayer2.C;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class MultiPlayer extends CordovaPlugin implements RadioListener {
    private static final String LOG_TAG = "MultiPlayer";

    private RadioManager mRadioManager = null;
    private CallbackContext connectionCallbackContext;
    private CallbackContext onEndedCallbackContext;
    private boolean isConnecting = false;
    private boolean isConnected = false;
    private JSONArray requestedPlay = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        log("ACTION - " + action);

        if ("initialize".equals(action)) {
            Handler mainThreadHandler = new Handler(Looper.getMainLooper());
            mainThreadHandler.post(() -> {
                try {
                    this.mRadioManager = RadioManager.with(this.cordova.getActivity(), this);
                    this.mRadioManager.setStreamURL(args.getString(0));

                    this.connectionCallbackContext = callbackContext;

                    PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                    pluginResult.setKeepCallback(true);

                    callbackContext.sendPluginResult(pluginResult);
                    callbackContext.success();
                } catch (Exception e) {
                    log("Exception occurred: ".concat(e.getMessage()));
                    callbackContext.error(e.getMessage());
                }
            });
            return true;
        } else if ("connect".equals(action)) {
            if (!this.isConnected && !this.isConnecting) {
                this.isConnecting = true;
                Handler mainThreadHandler = new Handler(Looper.getMainLooper());
                mainThreadHandler.post(() -> {
                    this.mRadioManager.connect();
                });
            }

            callbackContext.success();
            return true;
        } else if ("disconnect".equals(action)) {
            this.requestedPlay = null;

            if (this.isConnecting || this.isConnected) {
                this.isConnecting = false;
                this.isConnected = false;

                Handler mainThreadHandler = new Handler(Looper.getMainLooper());
                mainThreadHandler.post(() -> {
                    this.mRadioManager.disconnect();
                    log("RADIO STATE - DISCONNECTED...");
                    this.sendListenerResult("DISCONNECTED");
                });
            }

            callbackContext.success();
            return true;
        } else if ("play".equals(action)) {
            if (!this.isConnected) {
                if (!this.isConnecting) {
                    this.isConnecting = true;

                    Handler mainThreadHandler = new Handler(Looper.getMainLooper());
                    mainThreadHandler.post(() -> {
                        this.mRadioManager.connect();
                    });
                }

                this.requestedPlay = args;
            } else {
                this.requestedPlay = null;

                Handler mainThreadHandler = new Handler(Looper.getMainLooper());
                int type = args.getInt(0);
                mainThreadHandler.post(() -> {
                    this.mRadioManager.startRadio(type);
                });
            }

            callbackContext.success();
            return true;
        } else if ("pause".equals(action)) {
            Handler mainThreadHandler = new Handler(Looper.getMainLooper());
            mainThreadHandler.post(() -> {
                this.mRadioManager.pause();
                callbackContext.success();
            });
            return true;
        } else if ("stop".equals(action)) {

            this.requestedPlay = null;

            if (this.isConnected) {
                Handler mainThreadHandler = new Handler(Looper.getMainLooper());
                mainThreadHandler.post(() -> {
                    this.mRadioManager.stopRadio();
                    callbackContext.success();
                });
            }
            else {
                callbackContext.success();
            }
            return true;
        } else if ("getDuration".equals(action)) {
            Handler mainThreadHandler = new Handler(Looper.getMainLooper());
            mainThreadHandler.post(() -> {
                long duration = this.mRadioManager.getDuration();
                if (C.TIME_UNSET == duration) duration = -1;
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, duration);
                callbackContext.sendPluginResult(pluginResult);
            });
            return true;
        } else if ("getCurrentPosition".equals(action)) {
            Handler mainThreadHandler = new Handler(Looper.getMainLooper());
            mainThreadHandler.post(() -> {
                long pos = this.mRadioManager.getCurrentPosition();
                if (C.TIME_UNSET == pos) pos = -1;
                long duration = mRadioManager.getDuration();
                if (duration < pos) {
                    pos = duration;
                }
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, pos);
                callbackContext.sendPluginResult(pluginResult);
            });
            return true;
        } else if ("seekTo".equals(action)) {
            long time = args.getLong(0);
            Handler mainThreadHandler = new Handler(Looper.getMainLooper());
            mainThreadHandler.post(() -> {
                this.mRadioManager.seekTo(time);
                callbackContext.success();
            });
            return true;
        } else if ("setPlaybackRate".equals(action)) {
            double rate = args.getDouble(0);
            Handler mainThreadHandler = new Handler(Looper.getMainLooper());
            mainThreadHandler.post(() -> {
                this.mRadioManager.setPlaybackRate(rate);
                callbackContext.success();
            });
            return true;
        } else if ("setOnEnded".equals(action)) {
            onEndedCallbackContext = callbackContext;
            Handler mainThreadHandler = new Handler(Looper.getMainLooper());
            mainThreadHandler.post(() -> {
                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            });
            return true;
        }

        log("Called invalid action: " + action);
        return false;
    }

    @Override
    public void onRadioLoading() {
        log("RADIO STATE - LOADING...");
        this.sendListenerResult("LOADING");
    }

    @Override
    public void onRadioConnected() {
        this.isConnecting = false;
        this.isConnected = true;

        log("RADIO STATE - CONNECTED...");
        this.sendListenerResult("CONNECTED");

        if (this.requestedPlay != null) {
            try {
                this.mRadioManager.startRadio(this.requestedPlay.getInt(0));
            } catch(JSONException e) {
            }

            this.requestedPlay = null;
        }
    }

    @Override
    public void onRadioStarted() {
        log("RADIO STATE - PLAYING...");
        this.sendListenerResult("STARTED");
    }

    @Override
    public void onRadioStopped() {
        log("RADIO STATE - STOPPED...");
        this.sendListenerResult("STOPPED");
    }

    @Override
    public void onRadioStoppedFocusLoss() {
        log("RADIO STATE - STOPPED FOCUS LOSS...");
        this.sendListenerResult("STOPPED_FOCUS_LOSS");
    }

    @Override
    public void onError() {
        this.sendListenerResult("ERROR");
    }

    @Override
    public void onDestroy() {
        if (this.mRadioManager != null) {
            this.mRadioManager.disconnect();
        }
    }

    @Override
    public void onRadioEnded() {
        this.mRadioManager.pause();
        if (onEndedCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(true);
            onEndedCallbackContext.sendPluginResult(result);
        }
    }

    private void sendListenerResult(String result) {
        if (this.connectionCallbackContext != null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);

            pluginResult.setKeepCallback(true);
            this.connectionCallbackContext.sendPluginResult(pluginResult);
        }
    }

    /**
     * Logger
     * @param log
     */
    private void log(String log) {
        Log.v(LOG_TAG, "Plugin : " + log);
    }
}
