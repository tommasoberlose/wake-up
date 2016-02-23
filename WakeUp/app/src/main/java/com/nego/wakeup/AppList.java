package com.nego.wakeup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nego.wakeup.Adapter.MyAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppList extends AppCompatActivity {

    private SharedPreferences SP;
    private RecyclerView app_list;
    private MyAdapter mAdapter;

    private boolean filter_on = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        app_list = (RecyclerView) findViewById(R.id.app_list);
        app_list.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        app_list.setLayoutManager(llm);

        doList();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_applist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_select_all) {
            mAdapter.selectAll();
            return true;
        }

        if (id == R.id.action_filter) {
            filter_on = !filter_on;
            saveAll();
            doList();
            return true;
        }

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void doList() {
        findViewById(R.id.loader).setVisibility(View.VISIBLE);
        app_list.setVisibility(View.GONE);
        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            public void run() {

                mAdapter = new MyAdapter(SP, AppList.this, filter_on);

                mHandler.post(new Runnable() {
                    public void run() {
                        app_list.setAdapter(mAdapter);
                        findViewById(R.id.loader).setVisibility(View.GONE);
                        app_list.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        saveAll();
        super.onBackPressed();
    }

    public void saveAll() {
        if (mAdapter != null) {
            ArrayList<AppItemList> appItemLists = new ArrayList<>();
            for (String[] s : mAdapter.getData()) {
                appItemLists.add(new AppItemList(s[2], s[0].equals("1")));
            }
            saveAll(appItemLists, SP);
        }
    }

    public static void saveAll(ArrayList<AppItemList> appList, SharedPreferences SP) {
        String toSave = "";
        String itemSeparator = "";
        for (AppItemList s : appList) {
            toSave = toSave + itemSeparator + s.pack + Costants.SEPARATOR_CHECK + s.getCheck();
            itemSeparator = Costants.SEPARATOR_ITEM;
        }
        SP.edit().putString(Costants.NOTIFICATION_PACKAGE, toSave).apply();
    }

    public static ArrayList<AppItemList> getAppList(Context context, SharedPreferences SP) {
        String app = SP.getString(Costants.NOTIFICATION_PACKAGE, "");
        ArrayList<AppItemList> appList = new ArrayList<>();
        if (app.equals("")) {
            List<ApplicationInfo> apps = context.getPackageManager().getInstalledApplications(0);
            for (ApplicationInfo appInfo : apps) {
                   appList.add(new AppItemList(appInfo.packageName, true));
            }
        } else {
            String[] old_appList = app.split(";");
            if (old_appList.length > 1) {
                for (String pack : old_appList) {
                    appList.add(new AppItemList(pack, true));
                }
            } else {
                String[] items = app.split(Costants.SEPARATOR_ITEM);
                for (String item : items) {
                    String[] item_divided = item.split(Costants.SEPARATOR_CHECK);
                    appList.add(new AppItemList(item_divided[0], item_divided[1].equals("1")));
                }
            }
        }



        return appList;
    }

    public static class AppItemList {
        public String pack;
        public boolean check;

        public AppItemList(String pack, boolean check) {
            this.pack = pack;
            this.check = check;
        }

        public String getCheck() {
            return (check ? "1" : "0");
        }
    }
}
