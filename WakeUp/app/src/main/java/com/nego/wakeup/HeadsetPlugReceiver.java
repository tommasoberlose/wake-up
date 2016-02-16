package com.nego.wakeup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;
import android.view.Display;

public class HeadsetPlugReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
            SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
            if (SP.getBoolean(Costants.WAKEUP_ACTIVE, false) && SP.getBoolean(Costants.WAKEUP_HEADSET_PLUG, false) && !isScreenOn(context)) {
                wakeUp(SP, context);
            }
        }
    }public void wakeUp(SharedPreferences SP, Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(context.POWER_SERVICE);
        PowerManager.WakeLock screenLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.ON_AFTER_RELEASE, "TAG");
        screenLock.acquire(SP.getInt(Costants.PREFERENCES_TIMEOUT, 5000));
    }

    public boolean isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }
}