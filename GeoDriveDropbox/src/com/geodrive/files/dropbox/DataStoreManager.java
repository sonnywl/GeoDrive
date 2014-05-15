
package com.geodrive.files.dropbox;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxTable;
import com.dropbox.sync.android.DbxTable.QueryResult;
import com.geodrive.StaticInfo;

public class DataStoreManager {
    private static DataStoreManager dataStore;
    private static String TAG = DataStoreManager.class.getSimpleName();
    private Context mContext;
    private DbxAccountManager mAccountManager;
    private DbxAccount mAccount;
    static final int REQUEST_LINK_TO_DBX = 0; // This value is up to you

    public static DataStoreManager getInstance(Context context, Activity activity) {
        if (dataStore == null) {
            return new DataStoreManager(context, activity);
        }
        return dataStore;
    }

    private DataStoreManager(Context context, Activity activity) {
        mContext = context;
        mAccountManager = DbxAccountManager.getInstance(
                mContext, StaticInfo.APP_KEY, StaticInfo.APP_SECRET);
        try {
            mAccountManager.startLink(activity, 0);
        } catch (Exception e) {
            Log.i(TAG, "Got " + e);
        }
        Log.i(TAG, "Linked account is " + mAccountManager.hasLinkedAccount());

        if (mAccountManager.hasLinkedAccount()) {
            mAccount = mAccountManager.getLinkedAccount();
            Log.i(TAG, "Linked account is " + mAccount);
            try {
                DbxDatastore store = DbxDatastore.openDefault(mAccount);
                DbxTable table = store.getTable("GeoData");
                QueryResult res = table.query();

                Log.i(TAG, "Store is " + res.asList());

                store.close();
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }
    }

    public void connect() {
        mAccount = mAccountManager.getLinkedAccount();
        Log.i(TAG, "Linked account is " + mAccount);
    }

    public void updateFile(Entry entry) {

    }
}
