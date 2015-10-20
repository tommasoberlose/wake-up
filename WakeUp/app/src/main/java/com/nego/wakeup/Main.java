package com.nego.wakeup;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
        button.setActivated(NLService.isNotificationAccessEnabled);

        if (NLService.isNotificationAccessEnabled) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean now = SP.getBoolean(Costants.WAKEUP_ACTIVE, false);
                    SP.edit().putBoolean(Costants.WAKEUP_ACTIVE, !now).apply();
                    updateUI(!now);
                }
            });
            updateUI(SP.getBoolean(Costants.WAKEUP_ACTIVE, false));
        } else {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivityForResult(intent, 1);
                }
            });
            button.setText(R.string.action_activate_nls);
        }

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
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Costants.NLSERVICE_CHANGED);

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI(NLService.isNotificationAccessEnabled);
            }
        };
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1)
            updateUI(NLService.isNotificationAccessEnabled);
    }

    public void updateUI(boolean on) {
        button.setSelected(on);
        button.setText(on ? R.string.action_disable : R.string.action_enable);
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

}
