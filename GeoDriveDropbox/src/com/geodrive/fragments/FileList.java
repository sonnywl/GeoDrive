
package com.geodrive.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI.Entry;
import com.geodrive.HomeActivity;
import com.geodrive.R;
import com.geodrive.adapters.FileListAdapter;
import com.geodrive.files.FileInfo;
import com.geodrive.files.FileManager;
import com.geodrive.files.FileManagerListener;
import com.geodrive.fragments.dialog.FileDialog;
import com.geodrive.fragments.dialog.FileDialogOnClickListener;

import java.io.File;

public class FileList extends ListFragment implements OnClickListener,
        FileManagerListener,
        FileDialogOnClickListener {
    public static String TAG = FileList.class.getSimpleName();
    public static final String DIR = "CUR_DIR";
    public String directory = "/";
    HomeActivity activity;
    FileDialog dialog;
    FileInfo fileTarget;
    FileListAdapter mAdapter;
    FileManager fManager;
    Button btnRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        btnRefresh = (Button) rootView.findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(this);
        activity = (HomeActivity) getActivity();
        fManager = activity.getFileManager();
        fManager.addFileManagerListener(this);

        checkLink();
        if (savedInstanceState != null) {
            updateList(savedInstanceState.getString(DIR));
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new FileListAdapter(activity, new FileInfo[] {});
        setListAdapter(mAdapter);
        registerForContextMenu(getListView());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        fileTarget = (FileInfo) mAdapter.getItem(position);
        if (!fileTarget.isDirectory()) {
            showFileDialog();
        } else {
            updateList(fileTarget.getEntry().path);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DIR, directory);
        Log.i(TAG, "Saved Instance ");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
            case R.id.btn_refresh:
                updateList();
                break;
        }
    }

    public void updateList(FileInfo[] files) {
        mAdapter.updateInfo(files);
    }

    public void updateList() {
        updateList("/");
    }

    public void updateList(final String dir) {
        directory = dir;
        new Thread(new Runnable() {
            @Override
            public void run() {
                fManager.getDirectoryInfo(dir);
            }

        }).start();
    }

    private void showFileDialog() {
        dialog = FileDialog.newInstance(this);
        dialog.show(activity.getSupportFragmentManager(), "Files");
    }

    private void checkLink() {
        if (!fManager.isLinked()) {
            btnRefresh.setText("Dropbox Not Linked");
        } else {
            btnRefresh.setText("Refresh List");
        }
    }

    @Override
    public void notifyDialogListener(FileDialogOptions options) {
        Log.i(TAG, "Received " + options + " " + fileTarget);
        if (fileTarget != null) {
            switch (options) {
                default:
                case SYNC:
                    fManager.uploadFile(fileTarget.getEntry());
                    break;
                case SHARE:
                    break;
                case OPEN:
                    fManager.downloadFile(fileTarget.getEntry());
                    break;
            }
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    @Override
    public void notifyFileManagerListener(FileInfo[] files) {
        updateList(files);
    }

    @Override
    public void notifyFileManagerFileReady(Entry file, String cacheDir) {
        String path = cacheDir;
        Log.i(TAG, "File path " + path);
        File targetFile = new File(path);
        if (targetFile.exists()) {
            Uri targetUri = Uri.fromFile(targetFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(targetUri, file.mimeType);
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity.getBaseContext(), "File Download Not Found", Toast.LENGTH_SHORT).show();
        }
    }

}
