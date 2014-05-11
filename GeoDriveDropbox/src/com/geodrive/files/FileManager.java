
package com.geodrive.files;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import com.geodrive.StaticInfo;
import com.geodrive.files.tasks.FileDownloadTask;
import com.geodrive.files.tasks.FileQueryTask;
import com.geodrive.files.tasks.FileTaskListener;
import com.geodrive.files.tasks.FileUploadTask;
import com.geodrive.preferences.SharedPreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class FileManager implements FileTaskListener, LocationListener {

    public static final String TAG = FileManager.class.getSimpleName();
    private static FileManager manager;
    private SharedPreferenceManager sManager;
    private ArrayList<FileManagerListener> fileManagerClients;
    private LocationManager lService;
    private Entry targetEntry;
    private boolean isGPSEnabled = false,
            isNetworkEnabled = false,
            canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    public static FileManager getInstance(Context applicationContext) {
        if (manager == null) {
            manager = new FileManager(applicationContext);
        }
        return manager;
    }

    private Context mContext;
    private DropboxAPI<AndroidAuthSession> mDBApi;

    public FileManager(Context context) {
        mContext = context;
        sManager = SharedPreferenceManager.getInstance(mContext);
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        fileManagerClients = new ArrayList<FileManagerListener>();
        lService = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
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
            String cachePath = mContext.getExternalCacheDir().getAbsolutePath()
                    + File.separator + targetEntry.fileName();
            openFile(targetEntry, cachePath);
        }
    }

    public void openFile(Entry fileEntry, String cacheDir) {
        fileManagerClients.get(0).notifyFileManagerFileReady(fileEntry, cacheDir);
    }

    @Override
    public void notifyFileTaskListener(FileInfo[] files) {
        for (FileManagerListener listener : fileManagerClients) {
            listener.notifyFileManagerListener(files);
        }
    }

    private void storeAuth(AndroidAuthSession session) {
        String oauth2AccessToken = session.getOAuth2AccessToken();
        sManager.updateString(StaticInfo.APP_KEY, "oauth2:");
        sManager.updateString(StaticInfo.APP_SECRET, oauth2AccessToken);
        sManager.updateBoolean(StaticInfo.APP_AUTH, true);
        Log.i(TAG,
                "Stored " + sManager.getStringValue(StaticInfo.APP_SECRET, "Error:"
                        + oauth2AccessToken));
    }

    private void loadAuth(AndroidAuthSession session) {
        String tokenKey = sManager.getStringValue(StaticInfo.APP_KEY, null);
        String secret = sManager.getStringValue(StaticInfo.APP_SECRET, null);
        if (tokenKey == null || secret == null ||
                tokenKey.length() == 0 || secret.length() == 0) {
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
        AppKeyPair appKeyPair = new AppKeyPair(StaticInfo.APP_KEY, StaticInfo.APP_SECRET);
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

    public void addFileManagerListener(FileManagerListener listener) {
        fileManagerClients.add(listener);
    }

    public void removeFileManagerListener(FileManagerListener listener) {
        fileManagerClients.remove(listener);
    }

    private Location updateLocation() {
        try {
            // getting GPS status
            isGPSEnabled = lService
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = lService
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    lService.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (lService != null) {
                        location = lService
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        lService.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (lService != null) {
                            location = lService
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    // ----- Location Based Information
    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

}
