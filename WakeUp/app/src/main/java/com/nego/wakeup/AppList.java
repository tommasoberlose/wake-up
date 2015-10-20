package com.nego.wakeup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nego.wakeup.Adapter.MyAdapter;

public class AppList extends AppCompatActivity {

    private SharedPreferences SP;
    private RecyclerView app_list;
    private MyAdapter mAdapter;

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

        return super.onOptionsItemSelected(item);
    }


    public void doList() {
        findViewById(R.id.loader).setVisibility(View.VISIBLE);
        app_list.setVisibility(View.GONE);
        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            public void run() {

                mAdapter = new MyAdapter(SP, AppList.this);

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
}
