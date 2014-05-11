
package com.geodrive.files.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.geodrive.files.tasks.FileTaskListener.FileTaskState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileDownloadTask extends AsyncTask<Entry, Void, Void> {
    public static final String TAG = FileDownloadTask.class.getSimpleName();
    private Context mContext;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private FileTaskListener mListener;

    public FileDownloadTask(
            Context context, DropboxAPI<AndroidAuthSession> mApi,
            FileTaskListener listener) {
        mContext = context;
        mDBApi = mApi;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Entry... params) {

        try {
            String cachePath = mContext.getExternalCacheDir().getAbsolutePath()
                    + File.separator + params[0].fileName();

            File file = new File(cachePath);
            FileOutputStream outputStream = new FileOutputStream(file);
            DropboxFileInfo info = mDBApi.getFile(params[0].path, null, outputStream, null);
            Log.i("DbExampleLog", "The file's rev is: " + info.getMetadata().rev
                    + " " + cachePath);

            Log.i("DOWNLOAD", "File exists " + file.exists());
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mListener.notifyFileManagerListener(FileTaskState.COMPLETED_DOWNLOAD);
    }

}
