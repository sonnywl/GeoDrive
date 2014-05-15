
package com.geodrive.files;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.geodrive.StaticInfo;
import com.geodrive.files.tasks.FileDownloadTask;
import com.geodrive.files.tasks.FileQueryTask;
import com.geodrive.files.tasks.FileTaskListener;
import com.geodrive.files.tasks.FileUploadTask;
import com.geodrive.preferences.SharedPreferenceManager;

import java.io.File;
import java.util.ArrayList;

public class FileManager implements FileTaskListener {

    public static final String TAG = FileManager.class.getSimpleName();
    private static FileManager fileManager;
    private LocationManager locManager;
    private IFileLocationListener locListener;
    private SharedPreferenceManager sManager;
    private ArrayList<IFileManagerListener> fileManagerClients;
    private Entry targetEntry;

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
        locManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locListener = new IFileLocationListener(mContext);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, locListener);
    }

    public void getDirectoryInfo(String dir) {
        new FileQueryTask(mDBApi, this).execute(dir);
    }

    public void uploadFile(Entry fileEntry) {
        targetEntry = fileEntry;
        new FileUploadTask(mContext, mDBApi, this).execute(fileEntry);
    }

    public void downloadFile(Entry fileEntry) {
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

    public void openFile(Entry fileEntry, String cacheDir) {
        fileManagerClients.get(0).notifyFileManagerFileReady(fileEntry, cacheDir);
    }

    @Override
    public void notifyFileTaskListener(FileInfo[] files) {
        for (IFileManagerListener listener : fileManagerClients) {
            listener.notifyFileManagerListener(files);
        }
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
        }
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(StaticInfo.APP_KEY,
                StaticInfo.APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    public Location updateLocation() {
        return locListener.updateLocation();
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
