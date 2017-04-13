package com.github.mummyding.ymsecurity.lib_clean.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mummyding.ymbase.model.AppProcessInfo;
import com.github.mummyding.ymbase.util.StorageUtil;
import com.github.mummyding.ymsecurity.lib_clean.R;

import java.util.ArrayList;
import java.util.List;

public class MemoryCleanAdapter extends BaseAdapter {

    private List<AppProcessInfo> mListAppInfo;
    private LayoutInflater mInflater = null;
    private Context mContext;
    private List<Integer> mClearIds;

    public MemoryCleanAdapter(Context context, List<AppProcessInfo> apps) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mClearIds = new ArrayList<Integer>();
        this.mListAppInfo = apps;
    }

    @Override
    public int getCount() {
        return mListAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mListAppInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listview_memory_clean,
                    null);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView
                    .findViewById(R.id.image);
            holder.appName = (TextView) convertView
                    .findViewById(R.id.name);
            holder.memory = (TextView) convertView
                    .findViewById(R.id.memory);

            holder.cb = (RadioButton) convertView
                    .findViewById(R.id.choice_radio);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final AppProcessInfo appInfo = (AppProcessInfo) getItem(position);
        holder.appIcon.setImageDrawable(appInfo.icon);
        holder.appName.setText(appInfo.appName);
        holder.memory.setText(StorageUtil.convertStorage(appInfo.memory));
        if (appInfo.checked) {
            holder.cb.setChecked(true);
        } else {
            holder.cb.setChecked(false);
        }
        holder.cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appInfo.checked) {
                    appInfo.checked = false;
                } else {
                    appInfo.checked = true;
                }
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    class ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView memory;
        RadioButton cb;
    }

}
