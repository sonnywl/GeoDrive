
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
import com.geodrive.fragments.dialog.ShareFileDialog;
import com.geodrive.fragments.dialog.ShareFileDialog.ShareFileDialogOptions;

import java.io.File;

public class FileList extends ListFragment implements
        IFileManagerListener,
        FileDialog.FileDialogOnClickListener,
        ShareFileDialog.ShareFileDialogOnClickListener {
    public static String TAG = FileList.class.getSimpleName();
    public static final String DIR = "CUR_DIR";
    public String directory = "/";
    HomeActivity activity;
    FileDialog dialog;
    ShareFileDialog shareDialog;
    Entry fileTarget;
    FileListAdapter mAdapter;
    FileManager fManager;
    DataStoreManager dManager;
    private boolean immediateLocation = false;

    public enum ListType {
        ROOT_DIRECTORY, IMMEDIATE_LOCATION, ALL_LOCATIONS
    }

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
                updateList(ListType.ROOT_DIRECTORY);
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
        if (id == R.id.action_location) {
            if (immediateLocation) {
                immediateLocation = false;
                item.setTitle(R.string.location_immediate);
                updateList(ListType.IMMEDIATE_LOCATION);
            } else {
                immediateLocation = true;
                item.setTitle(R.string.location_all);
                updateList(ListType.ALL_LOCATIONS);
            }
        } else if (id == R.id.action_up) {
            if (directory.length() > 1) {
                updateList(directory.substring(0, directory.lastIndexOf("/")));
            } else {
                updateList(ListType.ROOT_DIRECTORY);
            }
        } else if (id == R.id.action_root) {
            updateList(ListType.ROOT_DIRECTORY);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DIR, directory);
    }

    public void updateList(Entry[] files) {
        mAdapter.updateInfo(files);
    }

    public void updateList(ListType listType) {
        switch (listType) {
            default:
            case ROOT_DIRECTORY:
                updateList("/");
                break;
            case IMMEDIATE_LOCATION:
                mAdapter.updateInfo(dManager.queryCurrLocationFiles(dManager.getLocation()));
                break;
            case ALL_LOCATIONS:
                mAdapter.updateInfo(dManager.queryAllLocationFiles(dManager.getLocation()));
                break;
        }

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

    private void showShareFileDialog(String[] contacts) {
        shareDialog = ShareFileDialog.newInstance(fileTarget.fileName(), contacts);
        shareDialog.show(activity.getSupportFragmentManager(), "Contacts");
    }

    @Override
    public void notifyFileManagerShareLinkIsReady(String link, String[] recipents) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, recipents);
        i.putExtra(Intent.EXTRA_SUBJECT, "GeoDrive Share File");
        i.putExtra(Intent.EXTRA_TEXT, link);
        try {
            this.startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), "There are no email clients installed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void notifyFileDialogListener(FileDialogOptions options) {
        if (fileTarget != null) {
            switch (options) {
                default:
                case SYNC:
                    fManager.uploadFile(fileTarget);
                    dManager.updateFile(fileTarget);
                    break;
                case SHARE:
                    showShareFileDialog(new String[] {});
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
            Toast.makeText(activity.getBaseContext(),
                    "File Download Not Found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void notifyShareFileDialogListener(ShareFileDialogOptions options) {
        switch (options) {
            default:
            case EMAIL:
                break;
            case SMS:
                break;
        }
    }

    public Entry getFileTarget() {
        return fileTarget;
    }

}
