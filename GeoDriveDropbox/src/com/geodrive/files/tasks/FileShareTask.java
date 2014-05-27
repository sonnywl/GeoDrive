
package com.geodrive.files.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxLink;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.geodrive.files.tasks.IFileListenerTask.FileTaskState;

public class FileShareTask extends AsyncTask<String, Void, DropboxLink> {
    public static final String TAG = FileShareTask.class.getSimpleName();
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private IFileListenerTask mListener;

    public FileShareTask(DropboxAPI<AndroidAuthSession> mApi,
            IFileListenerTask listener) {
        mDBApi = mApi;
        mListener = listener;
    }

    @Override
    protected DropboxLink doInBackground(String... params) {
        Log.i(TAG, params[0]);
        try {
            return mDBApi.share(params[0]);
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(DropboxLink result) {
        super.onPostExecute(result);
        mListener.notifyFileManagerListenerShareLink(FileTaskState.SHARE_LINK, result);
    }

}
