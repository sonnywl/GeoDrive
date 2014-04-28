
package com.locationdocs.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

public class DriveSyncAdapter extends AbstractThreadedSyncAdapter {

    private Context mContext;

    public DriveSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        DriverSyncer syncer = new DriverSyncer(mContext, provider, account);
        syncer.performSync();

    }

}
