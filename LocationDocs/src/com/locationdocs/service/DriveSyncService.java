
package com.locationdocs.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DriveSyncService extends Service {

    private static final String TAG = DriveSyncService.class.getSimpleName();

    private static final Object sSyncAdapterLock = new Object();

    private static DriveSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.i(TAG, " Service Created");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new DriveSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

}
