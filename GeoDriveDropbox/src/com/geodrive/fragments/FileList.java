
package com.geodrive.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI.Entry;
import com.geodrive.HomeActivity;
import com.geodrive.R;
import com.geodrive.adapters.FileListAdapter;
import com.geodrive.files.FileManager;
import com.geodrive.files.IFileManagerListener;
import com.geodrive.files.dropbox.DataStoreManager;
import com.geodrive.fragments.dialog.FileDialog;
import com.geodrive.fragments.dialog.FileDialog.FileDialogOptions;

import java.io.File;

public class FileList extends ListFragment implements IFileManagerListener,
        FileDialog.FileDialogOnClickListener {
    public static String TAG = FileList.class.getSimpleName();
    public static final String DIR = "CUR_DIR";
    public String directory = "/";
    HomeActivity activity;
    FileDialog dialog;
    Entry fileTarget;
    FileListAdapter mAdapter;
    FileManager fManager;
    DataStoreManager dManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new FileListAdapter(activity, new Entry[] {});
        setListAdapter(mAdapter);
        registerForContextMenu(getListView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        activity = (HomeActivity) getActivity();
        fManager = activity.getFileManager();
        fManager.addFileManagerListener(this);
        dManager = activity.getDataStoreManager();
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            updateList(savedInstanceState.getString(DIR));
        } else {
            if (fManager.isLinked()) {
                updateList();
            }
        }
        return rootView;
    }

    @Override
    public void onDestroyView() {
        fManager.removeFileManagerListener(this);
        super.onDestroyView();
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        fileTarget = (Entry) mAdapter.getItem(position);
        if (!fileTarget.isDir) {
            showFileDialog();
        } else {
            updateList(fileTarget.path);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateList();
        } else if (id == R.id.action_up) {
            if (directory.length() > 1) {
                updateList(directory.substring(0, directory.lastIndexOf("/")));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DIR, directory);
        Log.i(TAG, "Saved Instance ");
    }

    public void updateList(Entry[] files) {
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
        dialog = FileDialog.newInstance(fileTarget);
        dialog.show(activity.getSupportFragmentManager(), "Files");
    }

    @Override
    public void notifyDialogListener(FileDialogOptions options) {
        Log.i(TAG, "Received " + options + " " + fileTarget.fileName());
        if (fileTarget != null) {
            switch (options) {
                default:
                case SYNC:
//                    fManager.uploadFile(fileTarget);
                    dManager.updateFile(fileTarget);
                    break;
                case SHARE:
                    break;
                case OPEN:
                    fManager.downloadFile(fileTarget);
                    break;
            }
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    @Override
    public void notifyFileManagerListener(Entry[] files) {
        updateList(files);
    }

    @Override
    public void notifyFileManagerFileIsReady(Entry file, String cacheDir) {
        String path = cacheDir;
        Log.i(TAG, "File path " + path);
        File targetFile = new File(path);
        if (targetFile.exists()) {
            Uri targetUri = Uri.fromFile(targetFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(targetUri, file.mimeType);
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity.getBaseContext(), "File Download Not Found", Toast.LENGTH_SHORT)
                    .show();
        }
    }

}
