
package com.geodrive.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI.Entry;
import com.geodrive.R;

public class FileListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Entry[] fileEntryList;

    public FileListAdapter(Context context, Entry[] fileEntries) {
        mInflater = LayoutInflater.from(context);
        fileEntryList = fileEntries;
    }

    public void updateInfo(Entry[] fileEntries) {
        fileEntryList = null;
        fileEntryList = fileEntries;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return fileEntryList.length;
    }

    @Override
    public Object getItem(int position) {
        return fileEntryList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.adapter_folder_list, parent, false);
            holder = new ViewHolder();
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.name = (TextView) view.findViewById(R.id.list_name);
        holder.description = (TextView) view.findViewById(R.id.list_description);
        holder.name.setText(fileEntryList[position].fileName());
        holder.description.setVisibility(View.GONE);
        return view;
    }

    private static class ViewHolder {
        TextView name;
        TextView description;
    }

}
