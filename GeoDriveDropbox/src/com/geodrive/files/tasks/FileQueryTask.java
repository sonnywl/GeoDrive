
package com.geodrive.files.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.geodrive.files.FileInfo;

import java.util.ArrayList;
import java.util.List;

public class FileQueryTask extends AsyncTask<String, Void, FileInfo[]> {
    public static final String TAG = FileQueryTask.class.getSimpleName();
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private FileTaskListener mListener;

    public FileQueryTask(DropboxAPI<AndroidAuthSession> mApi, FileTaskListener listener) {
        mDBApi = mApi;
        mListener = listener;
    }

    @Override
    protected FileInfo[] doInBackground(String... dir) {
        Entry dropboxDir;
        ArrayList<FileInfo> files = new ArrayList<FileInfo>();
        if (dir[0].length() == 0) {
            dir[0] = "/";
        }
        try {
            dropboxDir = mDBApi.metadata(dir[0], 0, null, true, null);
            if (dropboxDir.isDir) {
                List<Entry> contents = dropboxDir.contents;
                if (contents != null) {
                    // CREATE NEW ENTRY AND THEN, GET THE FILENAME
                    // OF EVERY FILE
                    for (int i = 0; i < contents.size(); i++) {
                        Entry e = contents.get(i);
                        String a = e.fileName();
                        FileInfo file = new FileInfo(e);
                        files.add(file);
                        Log.d("dropbox", "FileName:" + a);
                    }
                }
                return files.toArray(new FileInfo[files.size()]);
            } else {
                Entry e = dropboxDir.contents.get(0);
                return files.toArray(new FileInfo[] {
                        new FileInfo(e)
                });
            }
        } catch (DropboxUnlinkedException e) {
            e.printStackTrace();
        } catch (DropboxException e1) {
            e1.printStackTrace();
        }
        return new FileInfo[] {};
    }

    @Override
    protected void onPostExecute(FileInfo[] files) {
        mListener.notifyFileTaskListener(files);
    }
}
