
package com.geodrive.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class GeoDriveService extends Service {
    public static final String TAG = GeoDriveService.class.getSimpleName();
    private final IBinder mBinder = new GeoDriveBinder();
//    private SharedPreferenceManager sManager;
//    private FileManager fManager;

    @Override
    public void onCreate() {
        super.onCreate();
        // sManager =
        // SharedPreferenceManager.getInstance(getApplicationContext());
        // fManager = FileManager.getInstance(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class GeoDriveBinder extends Binder {
        public GeoDriveService getService() {
            return GeoDriveService.this;
        }
    }
}
