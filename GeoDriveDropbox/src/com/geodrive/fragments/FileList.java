
package com.geodrive.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.geodrive.HomeActivity;
import com.geodrive.R;
import com.geodrive.adapters.FileListAdapter;
import com.geodrive.files.FileInfo;
import com.geodrive.fragments.dialog.FileDialog;

public class FileList extends ListFragment implements OnClickListener {
    public static String TAG = FileList.class.getSimpleName();
    HomeActivity activity;
    FileListAdapter mAdapter;
    Button btnRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        btnRefresh = (Button) rootView.findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(this);
        activity = (HomeActivity) getActivity();
        checkLink();
        return rootView;
    }
    
    public void checkLink() {
        if(!activity.isLinked()) {
            btnRefresh.setText("Dropbox Not Linked");
        } else {
            btnRefresh.setText("Refresh List");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new FileListAdapter(activity, new FileInfo[] {});
        setListAdapter(mAdapter);
        registerForContextMenu(getListView());
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        showFileDialog((FileInfo) mAdapter.getItem(position));
    }

    public void updateList(FileInfo[] files) {
        mAdapter.updateInfo(files);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_refresh:
                activity.updateList();
                Log.i(TAG, "Updating List");
        }
    }

    
    private void showFileDialog(FileInfo fileInfo) {
        
        FileDialog dialog = new FileDialog();
        dialog.show(activity.getSupportFragmentManager(), "Files");
    }
}
