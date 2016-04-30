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
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class Main extends AppCompatActivity {

    private TextView button;
    private SharedPreferences SP;
    private int selected = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);

        button = (TextView) findViewById(R.id.button_title);
        updateUI(SP.getBoolean(Costants.WAKEUP_ACTIVE, false));

        // PACKAGE
        findViewById(R.id.action_list_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, AppList.class));
            }
        });

        // PRIORITY
        updatePriority(SP.getInt(Costants.NOTIFICATION_PRIORITY, 0));
        findViewById(R.id.action_priority).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = SP.getInt(Costants.NOTIFICATION_PRIORITY, 0) + 2;
                String[] items = new String[]{
                        getString(R.string.text_priority_m2),
                        getString(R.string.text_priority_m1),
                        getString(R.string.text_priority_0),
                        getString(R.string.text_priority_1),
                        getString(R.string.text_priority_2)};

                final View custom_title = LayoutInflater.from(Main.this).inflate(R.layout.custom_popup_title, null);
                ((TextView) custom_title.findViewById(R.id.title)).setText(getString(R.string.title_priority));
                ((TextView) custom_title.findViewById(R.id.subtitle)).setText(getString(R.string.subtitle_priority));

                new AlertDialog.Builder(Main.this, R.style.mDialog)
                        .setCustomTitle(custom_title)
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

        // TIMEOUT
        updateTimeout(SP.getInt(Costants.PREFERENCES_TIMEOUT, 5000));
        findViewById(R.id.action_timeout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (SP.getInt(Costants.PREFERENCES_TIMEOUT, 5000)) {
                    case 15000:
                        selected = 1;
                        break;
                    case 30000:
                        selected = 2;
                        break;
                    case 60000:
                        selected = 3;
                        break;
                    case 120000:
                        selected = 4;
                        break;
                    default:
                        selected = 0;
                }
                String[] items = new String[]{
                        getString(R.string.text_timeout_0),
                        getString(R.string.text_timeout_1),
                        getString(R.string.text_timeout_2),
                        getString(R.string.text_timeout_3),
                        getString(R.string.text_timeout_4)};
                new AlertDialog.Builder(Main.this, R.style.mDialog)
                        .setTitle(getString(R.string.title_timeout_preferences))
                        .setSingleChoiceItems(items, selected, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selected = which;
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int timeout;
                                switch (selected) {
                                    case 1:
                                        timeout = 15000;
                                        break;
                                    case 2:
                                        timeout = 30000;
                                        break;
                                    case 3:
                                        timeout = 60000;
                                        break;
                                    case 4:
                                        timeout = 120000;
                                        break;
                                    default:
                                        timeout = 5000;
                                        break;
                                }
                                SP.edit().putInt(Costants.PREFERENCES_TIMEOUT, timeout).apply();
                                updateTimeout(timeout);
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

        // DELAY
        updateDelay(SP.getInt(Costants.PREFERENCES_DELAY, 1000));
        findViewById(R.id.action_delay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (SP.getInt(Costants.PREFERENCES_DELAY, 1000)) {
                    case 0:
                        selected = 0;
                        break;
                    case 500:
                        selected = 1;
                        break;
                    case 1500:
                        selected = 3;
                        break;
                    case 2000:
                        selected = 4;
                        break;
                    default:
                        selected = 2;
                }
                String[] items = new String[]{
                        getString(R.string.text_delay_1),
                        getString(R.string.text_delay_2),
                        getString(R.string.text_delay_0),
                        getString(R.string.text_delay_3),
                        getString(R.string.text_delay_4)};
                new AlertDialog.Builder(Main.this, R.style.mDialog)
                        .setTitle(getString(R.string.title_delay_preferences))
                        .setSingleChoiceItems(items, selected, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selected = which;
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int delay;
                                switch (selected) {
                                    case 0:
                                        delay = 0;
                                        break;
                                    case 1:
                                        delay = 500;
                                        break;
                                    case 3:
                                        delay = 1500;
                                        break;
                                    case 4:
                                        delay = 2000;
                                        break;
                                    default:
                                        delay = 1000;
                                        break;
                                }
                                SP.edit().putInt(Costants.PREFERENCES_DELAY, delay).apply();
                                updateDelay(delay);
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
            button.setText(on ? R.string.action_disable : R.string.action_enable);
            button.setBackgroundColor(on ? ContextCompat.getColor(this, R.color.primary) : ContextCompat.getColor(this, R.color.accent));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(on ? ContextCompat.getColor(this, R.color.primary_dark) : ContextCompat.getColor(this, R.color.accent_d));
            }
            findViewById(R.id.nls_how_to).setVisibility(View.GONE);
        } else {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivityForResult(intent, 1);
                }
            });
            button.setText(R.string.action_activate_nls);
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.accent));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.accent_d));
            }
            findViewById(R.id.nls_how_to).setVisibility(View.VISIBLE);
        }
        findViewById(R.id.card_settings).setVisibility(on ? View.VISIBLE : View.GONE);
        findViewById(R.id.card_how_to).setVisibility(!on ? View.VISIBLE : View.GONE);
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

    public void updateTimeout(int t) {
        switch (t) {
            case 5000:
                ((TextView) findViewById(R.id.subtitle_timeout)).setText(R.string.text_timeout_0);
                break;
            case 15000:
                ((TextView) findViewById(R.id.subtitle_timeout)).setText(R.string.text_timeout_1);
                break;
            case 30000:
                ((TextView) findViewById(R.id.subtitle_timeout)).setText(R.string.text_timeout_2);
                break;
            case 60000:
                ((TextView) findViewById(R.id.subtitle_timeout)).setText(R.string.text_timeout_3);
                break;
            case 120000:
                ((TextView) findViewById(R.id.subtitle_timeout)).setText(R.string.text_timeout_4);
                break;
        }
    }

    public void updateDelay(int t) {
        switch (t) {
            case 0:
                ((TextView) findViewById(R.id.subtitle_delay)).setText(R.string.text_delay_1);
                break;
            case 500:
                ((TextView) findViewById(R.id.subtitle_delay)).setText(R.string.text_delay_2);
                break;
            case 1000:
                ((TextView) findViewById(R.id.subtitle_delay)).setText(R.string.text_delay_0);
                break;
            case 1500:
                ((TextView) findViewById(R.id.subtitle_delay)).setText(R.string.text_delay_3);
                break;
            case 2000:
                ((TextView) findViewById(R.id.subtitle_delay)).setText(R.string.text_delay_4);
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

}
