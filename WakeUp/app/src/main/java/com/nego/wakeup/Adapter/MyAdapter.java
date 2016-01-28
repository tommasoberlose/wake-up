package com.nego.wakeup.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nego.wakeup.AppList;
import com.nego.wakeup.Costants;
import com.nego.wakeup.R;

import java.util.ArrayList;
import java.util.List;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<String[]> mDataset = new ArrayList<>();
    private List<String[]> mAll = new ArrayList<>();
    private Context mContext;
    private PackageManager pm;
    private SharedPreferences SP;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public ViewHolder(View v) {
            super(v);
            mView = v;
        }

        public RelativeLayout container;
        public CheckBox checkBox;
        public ImageView img;
        public TextView title;
        public TextView subtitle;
        public ViewHolder(View v, RelativeLayout container, CheckBox checkBox, ImageView img, TextView title, TextView subtitle) {
            super(v);
            mView = v;
            this.container = container;
            this.checkBox = checkBox;
            this.img = img;
            this.title = title;
            this.subtitle = subtitle;
        }

    }

    public MyAdapter(SharedPreferences SP, Context mContext, boolean filter) {
        this.mContext = mContext;
        pm = mContext.getPackageManager();
        this.SP = SP;
        generate_list(SP, filter);
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        ViewHolder vh;
        View v;

        v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.app_item, parent, false);
        vh = new ViewHolder(v,
                (RelativeLayout) v.findViewById(R.id.container),
                (CheckBox) v.findViewById(R.id.checkbox),
                (ImageView) v.findViewById(R.id.img),
                (TextView) v.findViewById(R.id.title),
                (TextView) v.findViewById(R.id.subtitle));

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
            }
        });

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDataset.get(position)[0] = isChecked ? "1" : "0";
            }
        });

        holder.checkBox.setChecked(mDataset.get(position)[0].equals("1"));
        holder.title.setText(mDataset.get(position)[1]);
        holder.subtitle.setText(mDataset.get(position)[2]);
        try {
            holder.img.setImageDrawable(pm.getApplicationIcon(mDataset.get(position)[2]));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // GENERATE LIST
    public void generate_list(SharedPreferences SP, boolean filter) {
        mDataset.clear();
        ArrayList<AppList.AppItemList> appItemLists = AppList.getAppList(mContext, SP);

        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        for (ApplicationInfo appInfo : apps) {
            String selected = "0";
            if (appItemLists.size() > 0) {
                for (AppList.AppItemList app : appItemLists)
                    if (app.pack.equals(appInfo.packageName))
                        selected = app.getCheck();
            } else {
                selected = "1";
            }

            if (!filter || (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                mDataset.add(new String[]{selected, pm.getApplicationLabel(appInfo).toString(), appInfo.packageName});
            }
            mAll.add(new String[]{selected, pm.getApplicationLabel(appInfo).toString(), appInfo.packageName});
        }

    }

    public void selectAll() {

        for (String[] s : mDataset) {
            s[0] = "1";
        }
        notifyDataSetChanged();
    }

    public List<String[]> getData() {
        for (String[] x : mAll) {
            for (String[] y : mDataset) {
                if (x[2].equals(y[2])) {
                    x[0] = y[0];
                    break;
                }
            }
        }
        return mAll;
    }
}
