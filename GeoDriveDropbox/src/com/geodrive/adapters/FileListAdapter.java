
package com.geodrive.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.geodrive.R;
import com.geodrive.files.FileInfo;

public class FileListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private FileInfo[] fileList;

    public FileListAdapter(Context context, FileInfo[] fileInfos) {
        mInflater = LayoutInflater.from(context);
        fileList = fileInfos;
    }

    public void updateInfo(FileInfo[] files) {
        fileList = null;
        fileList = files;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return fileList.length;
    }

    @Override
    public Object getItem(int position) {
        return fileList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.folder_list_adapter, parent, false);
            holder = new ViewHolder();
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.name = (TextView) view.findViewById(R.id.list_name);
        holder.description = (TextView) view.findViewById(R.id.list_description);
        holder.description.setVisibility(View.GONE);

        FileInfo file = fileList[position];
        holder.name.setText(file.getFilename());
//        holder.description.setText(file.getMetadata());

        return view;
    }

    private static class ViewHolder {
        TextView name;
        TextView description;
    }

}
