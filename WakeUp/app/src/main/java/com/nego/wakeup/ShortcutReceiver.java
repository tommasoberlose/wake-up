package com.nego.wakeup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

public class ShortcutReceiver extends Activity {

    public void onCreate(Bundle savedInstanceState) {

        if (getIntent().getAction().equals("android.intent.action.CREATE_SHORTCUT")) {
            Intent shortcutIntent = new Intent(getApplicationContext(), ShortcutReceiver.class);
            shortcutIntent.setAction(Costants.ACTION_TOGGLE_APP);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.name_shortcut));
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_shortcut));
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

            setResult(RESULT_OK, addIntent);
            finish();
        }

        if (getIntent().getAction().equals(Costants.ACTION_TOGGLE_APP)) {
            SharedPreferences SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
            boolean now = SP.getBoolean(Costants.WAKEUP_ACTIVE, false);
            SP.edit().putBoolean(Costants.WAKEUP_ACTIVE, !now).apply();
            Toast.makeText(this, getString(!now ? R.string.app_name_enabled : R.string.app_name_disabled), Toast.LENGTH_SHORT).show();
            finish();
        }

        super.onCreate(savedInstanceState);
    }
}
