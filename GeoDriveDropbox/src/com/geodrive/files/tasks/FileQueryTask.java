
package com.geodrive.files.tasks;

import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.util.ArrayList;
import java.util.List;

public class FileQueryTask extends AsyncTask<String, Void, Entry[]> {
    public static final String TAG = FileQueryTask.class.getSimpleName();
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private IFileTaskListener mListener;

    public FileQueryTask(DropboxAPI<AndroidAuthSession> mApi, IFileTaskListener listener) {
        mDBApi = mApi;
        mListener = listener;
    }

    @Override
    protected Entry[] doInBackground(String... dir) {
        Entry dropboxDir;
        ArrayList<Entry> files = new ArrayList<Entry>();
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
                        files.add(e);
                    }
                }
                return files.toArray(new Entry[files.size()]);
            } else {
                Entry e = dropboxDir.contents.get(0);
                return files.toArray(
                        new Entry[] {
                            e
                        });
            }
        } catch (DropboxUnlinkedException e) {
            e.printStackTrace();
        } catch (DropboxException e1) {
            e1.printStackTrace();
        }
        return new Entry[] {};
    }

    @Override
    protected void onPostExecute(Entry[] files) {
        mListener.notifyFileTaskListener(files);
    }
}
