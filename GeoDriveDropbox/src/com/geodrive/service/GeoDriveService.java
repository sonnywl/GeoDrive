package com.geodrive.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class GeoDriveService extends Service{

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
