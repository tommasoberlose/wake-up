package com.nego.wakeup;

import android.animation.Animator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class Main extends AppCompatActivity {

    private Button button;
    private SharedPreferences SP;
    private int selected = 1;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);

        button = (Button) findViewById(R.id.button_title);
        updateUI(SP.getBoolean(Costants.WAKEUP_ACTIVE, false));

        // PACKAGE
        findViewById(R.id.action_list_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, AppList.class));
            }
        });

        // PRIORITY
        updatePriority(SP.getInt(Costants.NOTIFICATION_PRIORITY, 1));
        findViewById(R.id.action_priority).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = SP.getInt(Costants.NOTIFICATION_PRIORITY, 1) + 2;
                String[] items = new String[]{
                        getString(R.string.text_priority_m2),
                        getString(R.string.text_priority_m1),
                        getString(R.string.text_priority_0),
                        getString(R.string.text_priority_1),
                        getString(R.string.text_priority_2)};
                new AlertDialog.Builder(Main.this, R.style.mDialog)
                        .setTitle(getString(R.string.title_priority))
                        .setSingleChoiceItems(items, selected, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selected = which;
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SP.edit().putInt(Costants.NOTIFICATION_PRIORITY, selected - 2).apply();
                                updatePriority(selected - 2);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        //SILENT
        final CheckBox sC = (CheckBox) findViewById(R.id.silent_check);
        sC.setChecked(SP.getBoolean(Costants.PREFERENCE_SILENT, true));
        sC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_SILENT, isChecked).apply();
            }
        });
        findViewById(R.id.action_silent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sC.setChecked(!sC.isChecked());
            }
        });

        //PROXIMITY
        final CheckBox pC = (CheckBox) findViewById(R.id.proximity_check);
        pC.setChecked(SP.getBoolean(Costants.PREFERENCE_PROXIMITY, true));
        pC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_PROXIMITY, isChecked).apply();
            }
        });
        findViewById(R.id.action_proximity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pC.setChecked(!pC.isChecked());
            }
        });

        // VERSIONE
        String version = getString(R.string.app_name);
        try {
            version += " " + getString(R.string.text_version) + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.version_title)).setText(version);
        findViewById(R.id.action_rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nego.wakeup")));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.nego.wakeup")));
                }
            }
        });

        // COMMUNITY
        findViewById(R.id.action_community).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/communities/100614116200820350356/stream/edf4f722-4c14-4b29-98dc-58a5721dd3a5")));
            }
        });

        welcomeAlert();

    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI(SP.getBoolean(Costants.WAKEUP_ACTIVE, false));
    }

    public void updateUI(boolean on) {
        if (haveNotificationAccess()) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean now = SP.getBoolean(Costants.WAKEUP_ACTIVE, false);
                    SP.edit().putBoolean(Costants.WAKEUP_ACTIVE, !now).apply();
                    updateUI(!now);
                }
            });
            button.setSelected(on);
            button.setText(on ? R.string.app_name_enabled : R.string.app_name_disabled);
            button.setActivated(true);
        } else {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivityForResult(intent, 1);
                }
            });
            button.setText(R.string.action_activate_nls);
            button.setActivated(false);
        }
        findViewById(R.id.card_settings).setVisibility(on ? View.VISIBLE : View.GONE);
    }

    public void updatePriority(int p) {
        switch (p) {
            case 0:
                ((TextView) findViewById(R.id.subtitle_priority)).setText(R.string.text_priority_0);
                break;
            case 1:
                ((TextView) findViewById(R.id.subtitle_priority)).setText(R.string.text_priority_1);
                break;
            case -1:
                ((TextView) findViewById(R.id.subtitle_priority)).setText(R.string.text_priority_m1);
                break;
            case 2:
                ((TextView) findViewById(R.id.subtitle_priority)).setText(R.string.text_priority_2);
                break;
            case -2:
                ((TextView) findViewById(R.id.subtitle_priority)).setText(R.string.text_priority_m2);
                break;
        }
    }

    public boolean haveNotificationAccess() {
        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getPackageName();

        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName))
            return false;
        else
            return true;
    }

    public void welcomeAlert() {
        if (SP.getBoolean(Costants.WELCOME_ALERT, true)) {
            SP.edit().putBoolean(Costants.WELCOME_ALERT, false).apply();
            new AlertDialog.Builder(this, R.style.Dialog_Pop)
                    .setTitle(R.string.text_welcome)
                    .setMessage(R.string.text_welcome_msg)
                    .setPositiveButton(R.string.text_great, null)
                    .show();
        }
    }

}
