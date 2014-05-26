
package com.geodrive.files.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.geodrive.files.tasks.IFileTaskListener.FileTaskState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FileUploadTask extends AsyncTask<Entry, Void, Void> {
    public static final String TAG = FileUploadTask.class.getSimpleName();
    private Context mContext;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private IFileTaskListener mListener;
    private String cachePath;

    public FileUploadTask(
            Context context, DropboxAPI<AndroidAuthSession> mApi,
            IFileTaskListener listener) {
        mContext = context;
        mDBApi = mApi;
        mListener = listener;
        cachePath = "";
    }

    @Override
    protected Void doInBackground(Entry... params) {
        cachePath = mContext.getExternalCacheDir().getAbsolutePath()
                + File.separator + params[0].fileName();
        File file = new File(cachePath);
        Log.i(TAG, cachePath);
        try {
            if (file.exists()) {
                FileInputStream inputStream = new FileInputStream(file);
                Entry response = mDBApi.putFile(params[0].path, inputStream,
                        file.length(), params[0].rev, null);
                
                Log.i("DbExampleLog", "The uploaded file's rev is: " + response.rev);
            }
        } catch (DropboxException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mListener.notifyFileManagerListener(FileTaskState.COMPLETED_UPLOAD);
    }
}
