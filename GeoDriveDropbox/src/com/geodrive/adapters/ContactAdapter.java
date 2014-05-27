
package com.geodrive.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.geodrive.R;

import java.util.ArrayList;

public class ContactAdapter extends BaseAdapter implements OnClickListener {
    public static final String TAG = ContactAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private String[] contacts;
    private boolean[] checks;

    public ContactAdapter(Context context, String[] result) {
        mInflater = LayoutInflater.from(context);
        contacts = result;
        checks = new boolean[contacts.length];
        for (int i = 0; i < checks.length; i++) {
            checks[i] = false;
        }
    }

    @Override
    public int getCount() {
        return contacts.length;
    }

    @Override
    public Object getItem(int position) {
        return contacts[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.adapter_contact_list, parent, false);
            holder = new ViewHolder();
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.name = (CheckBox) view.findViewById(R.id.contact_checkbox);
        holder.name.setText(contacts[position]);
        holder.name.setOnClickListener(this);
        holder.name.setTag(String.valueOf(position));
        holder.name.setChecked(checks[position]);
        return view;

    }

    private static class ViewHolder {
        CheckBox name;
    }

    @Override
    public void onClick(View v) {
        Integer position = Integer.valueOf(v.getTag().toString());
        if (checks[position]) {
            checks[position] = false;
        } else {
            checks[position] = true;
        }
    }

    public String[] getSelectedData() {
        ArrayList<String> res = new ArrayList<String>(checks.length);
        for (int i = 0; i < checks.length; i++) {
            if (checks[i]) {
                res.add(contacts[i].split("\n")[1]);
            }
        }
        return res.toArray(new String[res.size()]);
    }
}
