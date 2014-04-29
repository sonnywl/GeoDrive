
package com.locationdocs.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class DriveSyncAdapter extends AbstractThreadedSyncAdapter {

    static final String TAG = DriveSyncAdapter.class.getSimpleName();
    private Context mContext;

    public DriveSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        Log.i(TAG, "DriveSyncAdapter cosntructed");
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        DriverSyncer syncer = new DriverSyncer(mContext, provider, account);
        syncer.performSync();

    }

}
