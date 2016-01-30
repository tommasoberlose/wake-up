package com.nego.wakeup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Display;
import android.view.View;

import java.util.ArrayList;

public class NLService extends NotificationListenerService implements SensorEventListener {

    private NLServiceReceiver nlservicereciver;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private SensorManager mSensorMgr;
    private SharedPreferences SP;

    @Override
    public IBinder onBind(Intent mIntent) {
        IBinder mIBinder = super.onBind(mIntent);
        return mIBinder;
    }

    @Override
    public boolean onUnbind(Intent mIntent) {
        boolean mOnUnbind = super.onUnbind(mIntent);
        return mOnUnbind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Costants.ACTION_NOTIFICATION_LISTENER_SERVICE);
        registerReceiver(nlservicereciver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Intent i = new  Intent(Costants.ACTION_NOTIFICATION_LISTENER_SERVICE);
        i.putExtra(Costants.NOTIFICATION_PACKAGE, sbn.getPackageName());
        i.putExtra(Costants.NOTIFICATION_PRIORITY, sbn.getNotification().priority);
        sendBroadcast(i);

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i("PROX", event.values[0] + "");
       if (!(event.values[0] < mSensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY).getMaximumRange())) {
           mHandlerThread.quit();
           mSensorMgr.unregisterListener(NLService.this);
           wakeUp(SP);
           Log.i("SENSOR", "END_PROX");
       }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i("ACCURACY", "CHANGED");
    }

    class NLServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Costants.ACTION_NOTIFICATION_LISTENER_SERVICE)) {
                SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
                if (SP.getBoolean(Costants.WAKEUP_ACTIVE, false) && !isScreenOn() && checkSilent(SP) && checkListOk(intent, SP) && checkPriority(intent, SP)) {
                    mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                    if (SP.getBoolean(Costants.PREFERENCE_PROXIMITY, true) && (mSensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null)) {
                        Log.i("SENSOR", "START");
                        try {
                            mHandlerThread = new HandlerThread("sensorThread");
                            mHandlerThread.start();
                            final Handler handler = new Handler(mHandlerThread.getLooper());

                            mSensorMgr.registerListener(NLService.this, mSensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                                    SensorManager.SENSOR_DELAY_FASTEST, handler);


                            mHandler = new Handler();
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    mHandlerThread.quit();
                                    mSensorMgr.unregisterListener(NLService.this);
                                    Log.i("SENSOR", "END_TIME");
                                }
                            }, 6000);
                        } catch (Exception e) {
                            e.printStackTrace();
                            wakeUp(SP);
                        }
                    } else {
                        wakeUp(SP);
                    }

                }
            }
        }
    }

    public boolean checkListOk(Intent intent, SharedPreferences SP) {
        ArrayList<AppList.AppItemList> appItemLists = AppList.getAppList(this, SP);
        if (appItemLists.size() > 0) {
            for (AppList.AppItemList app : appItemLists) {
                if (app.pack.equals(intent.getStringExtra(Costants.NOTIFICATION_PACKAGE)))
                    return app.check;
            }
        }
        return true;
    }

    public boolean checkPriority(Intent intent, SharedPreferences SP) {
        return (intent.getIntExtra(Costants.NOTIFICATION_PRIORITY, -1) >= SP.getInt(Costants.NOTIFICATION_PRIORITY, 1));
    }

    public boolean checkSilent(SharedPreferences SP) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        return !(SP.getBoolean(Costants.PREFERENCE_SILENT, true) && (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT));
    }

    public void wakeUp(SharedPreferences SP) {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock screenLock = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                            | PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE, "TAG");
        screenLock.acquire(SP.getInt(Costants.PREFERENCES_TIMEOUT, 5000));
    }

    public boolean isScreenOn() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }

}