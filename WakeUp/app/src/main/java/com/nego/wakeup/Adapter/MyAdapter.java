package com.nego.wakeup.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<String[]> mDataset = new ArrayList<>();
    private List<String[]> mAll = new ArrayList<>();
    private Context mContext;
    private PackageManager pm;
    private SharedPreferences SP;
    private int selected;

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
        public CardView priority_container;
        public TextView priority_text;
        public ViewHolder(View v, RelativeLayout container, CheckBox checkBox, ImageView img, TextView title, TextView subtitle, CardView priority_container, TextView priority_text) {
            super(v);
            mView = v;
            this.container = container;
            this.checkBox = checkBox;
            this.img = img;
            this.title = title;
            this.subtitle = subtitle;
            this.priority_container = priority_container;
            this.priority_text = priority_text;
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
                (TextView) v.findViewById(R.id.subtitle),
                (CardView) v.findViewById(R.id.priority_container),
                (TextView) v.findViewById(R.id.priority_text));

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

        holder.priority_container.setVisibility(mDataset.get(position)[3].equals("" + SP.getInt(Costants.NOTIFICATION_PRIORITY, 0)) ? View.GONE : View.VISIBLE);
        holder.priority_text.setText(getPriorityToString(mDataset.get(position)[3]));

        holder.checkBox.setChecked(mDataset.get(position)[0].equals("1"));
        holder.title.setText(mDataset.get(position)[1]);
        holder.subtitle.setText(mDataset.get(position)[2]);
        try {
            holder.img.setImageDrawable(pm.getApplicationIcon(mDataset.get(position)[2]));
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selected = Integer.parseInt(mDataset.get(position)[3]) + 2;
                String[] items = new String[]{
                        mContext.getString(R.string.text_priority_m2),
                        mContext.getString(R.string.text_priority_m1),
                        mContext.getString(R.string.text_priority_0),
                        mContext.getString(R.string.text_priority_1),
                        mContext.getString(R.string.text_priority_2)};

                final View custom_title = LayoutInflater.from(mContext).inflate(R.layout.custom_popup_title, null);
                ((TextView) custom_title.findViewById(R.id.title)).setText(mContext.getString(R.string.title_priority));
                ((TextView) custom_title.findViewById(R.id.subtitle)).setText(mContext.getString(R.string.subtitle_priority));

                new AlertDialog.Builder(mContext, R.style.mDialog)
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
                                mDataset.get(position)[3] =  "" + (selected - 2);
                                notifyItemChanged(position);
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
                return false;
            }
        });

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
            boolean found = false;
            int priority = SP.getInt(Costants.NOTIFICATION_PRIORITY, 0);
            if (appItemLists.size() > 0) {
                for (AppList.AppItemList app : appItemLists)
                    if (app.pack.equals(appInfo.packageName)) {
                        selected = app.getCheck();
                        priority = app.priority;
                        found = true;
                    }
            }

            if (!found) {
                selected = "1";
            }

            if (!filter || (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                mDataset.add(new String[]{selected, pm.getApplicationLabel(appInfo).toString(), appInfo.packageName, "" + priority});
            }
            mAll.add(new String[]{selected, pm.getApplicationLabel(appInfo).toString(), appInfo.packageName, "" +priority});
        }


        Collections.sort(mDataset, new Comparator<String[]>() {
            @Override
            public int compare(String[] lhs, String[] rhs) {
                return lhs[1].compareToIgnoreCase(rhs[1]);
            }
        });

        Collections.sort(mAll, new Comparator<String[]>() {
            @Override
            public int compare(String[] lhs, String[] rhs) {
                return lhs[1].compareToIgnoreCase(rhs[1]);
            }
        });

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
                    x[3] = y[3];
                    break;
                }
            }
        }
        return mAll;
    }

    public String getPriorityToString(String p) {
        switch (p) {
            case "-2":
                return "" + mContext.getString(R.string.text_priority_m2);
            case "-1":
                return "" + mContext.getString(R.string.text_priority_m1);
            case "1":
                return "" + mContext.getString(R.string.text_priority_1);
            case "2":
                return "" + mContext.getString(R.string.text_priority_2);
            default:
                return "" + mContext.getString(R.string.text_priority_0);
        }
    }
}
