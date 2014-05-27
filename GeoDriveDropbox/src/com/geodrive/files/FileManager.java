
package com.geodrive.files;

import android.content.Context;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxLink;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.geodrive.StaticInfo;
import com.geodrive.files.tasks.FileDownloadTask;
import com.geodrive.files.tasks.FileQueryTask;
import com.geodrive.files.tasks.FileShareTask;
import com.geodrive.files.tasks.FileUploadTask;
import com.geodrive.files.tasks.IFileListenerTask;
import com.geodrive.preferences.SharedPreferenceManager;

import java.io.File;
import java.util.ArrayList;

public class FileManager implements IFileListenerTask {

    public static final String TAG = FileManager.class.getSimpleName();
    private static FileManager fileManager;
    private SharedPreferenceManager sManager;
    private ArrayList<IFileManagerListener> fileManagerClients;
    private Entry targetEntry;
    private String[] recipents;

    public static FileManager getInstance(Context applicationContext) {
        if (fileManager == null) {
            fileManager = new FileManager(applicationContext);
        }
        return fileManager;
    }

    private Context mContext;
    private DropboxAPI<AndroidAuthSession> mDBApi;

    public FileManager(Context context) {
        mContext = context;
        sManager = SharedPreferenceManager.getInstance(mContext);
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        fileManagerClients = new ArrayList<IFileManagerListener>();
    }

    public void getDirectoryInfo(final String dir) {
        new FileQueryTask(mDBApi, this).execute(dir);
    }

    public void shareFile(final Entry entry, String[] recipentList) {
        targetEntry = entry;
        recipents = recipentList;
        new FileShareTask(mDBApi, this).execute(entry.path);
    }

    public void uploadFile(final Entry fileEntry) {
        targetEntry = fileEntry;
        new FileUploadTask(mContext, mDBApi, this).execute(fileEntry);
    }

    public void downloadFile(final Entry fileEntry) {
        targetEntry = fileEntry;
        new FileDownloadTask(mContext, mDBApi, this).execute(fileEntry);
    }

    @Override
    public void notifyFileManagerListener(FileTaskState state) {
        if (state == FileTaskState.COMPLETED_DOWNLOAD) {
            String cachePath = mContext.getExternalCacheDir().getAbsolutePath() + File.separator
                    + targetEntry.fileName();
            openFile(targetEntry, cachePath);
        }
    }

    @Override
    public void notifyFileManagerListenerShareLink(FileTaskState state, DropboxLink link) {
        if (state == FileTaskState.SHARE_LINK && recipents != null) {
            for (IFileManagerListener listener : fileManagerClients) {
                listener.notifyFileManagerShareLinkIsReady(link.url, recipents);
            }
        }
    }

    @Override
    public void notifyFileTaskListener(Entry[] files) {
        for (IFileManagerListener listener : fileManagerClients) {
            listener.notifyFileManagerListener(files);
        }
    }

    public void openFile(Entry fileEntry, String cacheDir) {
        fileManagerClients.get(0).notifyFileManagerFileIsReady(fileEntry, cacheDir);
    }

    private void storeAuth(AndroidAuthSession session) {
        String oauth2AccessToken = session.getOAuth2AccessToken();
        sManager.updateString(StaticInfo.APP_KEY, "oauth2:");
        sManager.updateString(StaticInfo.APP_SECRET, oauth2AccessToken);
        Log.i(TAG, "Stored "
                + sManager.getStringValue(
                        StaticInfo.APP_DATASTORE_SECRET, "Token:" + oauth2AccessToken));
    }

    private void loadAuth(AndroidAuthSession session) {
        String tokenKey = sManager.getStringValue(StaticInfo.APP_KEY, null);
        String secret = sManager.getStringValue(StaticInfo.APP_SECRET, null);
        if (tokenKey == null || secret == null || tokenKey.length() == 0 || secret.length() == 0) {
            return;
        }
        session.setOAuth2AccessToken(secret);
    }

    public void checkAuth() {
        AndroidAuthSession session = mDBApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the
                // session
                session.finishAuthentication();
                Log.i("DbAuthLog", "Auth Success");
                storeAuth(session);
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        } else {
            loadAuth(session);
        }
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(StaticInfo.APP_KEY,
                StaticInfo.APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    public boolean isLinked() {
        return mDBApi.getSession().isLinked();
    }

    public void link() {
        mDBApi.getSession().startOAuth2Authentication(mContext);
    }

    public void unlink() {
        mDBApi.getSession().unlink();
    }

    public void addFileManagerListener(IFileManagerListener listener) {
        fileManagerClients.add(listener);
    }

    public void removeFileManagerListener(IFileManagerListener listener) {
        fileManagerClients.remove(listener);
    }

}
