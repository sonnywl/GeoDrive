
package com.geodrive.adaptor;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.geodrive.DriveFileInfo;
import com.geodrive.R;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;

import java.util.ArrayList;
import java.util.Iterator;

public class DriveListAdapter extends BaseAdapter {
    public static final String TAG = DriveListAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private ArrayList<DriveFileInfo> driveFiles;

    public DriveListAdapter(Context context, ArrayList<DriveFileInfo> files) {
        mInflater = LayoutInflater.from(context);
        driveFiles = files;
    }

    @Override
    public int getCount() {
        return driveFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return driveFiles.get(position);
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

        DriveFileInfo file = driveFiles.get(position);
        holder.name.setText(file.getFilename());
        holder.description.setText(file.getMetadata());

        return view;

    }

    private static class ViewHolder {
        TextView name;
        TextView description;
    }

    public void append(MetadataBuffer buffer) {
        Iterator<Metadata> iterator = buffer.iterator();
        while(iterator.hasNext()) {
            Metadata data = iterator.next();
            Log.i(TAG, data.getTitle());
        }
    }

}
