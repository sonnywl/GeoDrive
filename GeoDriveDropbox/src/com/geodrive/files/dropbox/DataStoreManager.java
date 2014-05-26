
package com.geodrive.files.dropbox;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFields;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.geodrive.StaticInfo;
import com.geodrive.db.GeoDriveDBManager;
import com.geodrive.files.FileLocationListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class DataStoreManager {
    private static DataStoreManager dataStore;
    private static String TAG = DataStoreManager.class.getSimpleName();
    static final int REQUEST_LINK_TO_DBX = 0; // This value is up to you

    private Context mContext;
    private DbxAccountManager mAccountManager;
    private DbxAccount mAccount;
    private DbxDatastore store;
    private DbxTable table;
    private GeoDriveDBManager dbManager;
    private FileLocationListener locListener;
    private LocationManager locManager;

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
        dbManager = GeoDriveDBManager.getInstance(context);
        locListener = new FileLocationListener(mContext);
        locManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        try {
            mAccountManager.startLink(activity, 0);
            Log.i(TAG, "Linked account is " + mAccountManager.getLinkedAccount().getUserId() + " "
                    + mAccountManager.hasLinkedAccount());
        } catch (Exception e) {
            Log.i(TAG, "Got " + e);
        }
    }

    public void updateFile(final Entry entry) {
        if (mAccountManager.hasLinkedAccount()) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, locListener);
            mAccount = mAccountManager.getLinkedAccount();
            SimpleDateFormat date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            String mTime = date.format(new Date());
            Log.i(TAG, mTime);

            try {
                dbManager.open();
                store = DbxDatastore.openDefault(mAccount);
                table = store.getTable("GeoData");
                Location loc = updateLocation();
                FileDataStore data = dbManager.queryData(entry.path);
                if (data == null) {
                    DbxRecord firstTask = table.insert()
                            .set(GeoDriveDBManager.LAT, loc.getLatitude())
                            .set(GeoDriveDBManager.LONG, loc.getLongitude())
                            .set(GeoDriveDBManager.RECENT, mTime)
                            .set(GeoDriveDBManager.PATH, entry.path);
                    if (DbxTable.isValidId(firstTask.getId())) {
                        dbManager.createDataPoint(firstTask.getId(),
                                loc.getLongitude(),
                                loc.getLatitude(),
                                mTime,
                                entry.path);
                    }
                } else {
                    Log.i(TAG, "Valid " + data.taskId);
                    if (DbxTable.isValidId(data.taskId)) {
                        DbxFields queryParams = new DbxFields()
                                .set(GeoDriveDBManager.PATH, entry.path);
                        DbxTable.QueryResult results = table.query(queryParams);
                        Iterator<DbxRecord> res = results.iterator();
                        while (res.hasNext()) {
                            DbxRecord rec = res.next();
                            rec.set(GeoDriveDBManager.LAT, loc.getLatitude())
                                    .set(GeoDriveDBManager.LONG, loc.getLongitude())
                                    .set(GeoDriveDBManager.RECENT, mTime)
                                    .set(GeoDriveDBManager.PATH, entry.path);
                            dbManager.updateDataPoint(data.taskId,
                                    loc.getLongitude(),
                                    loc.getLatitude(),
                                    mTime,
                                    entry.path);
                        }
                    }
                }
                store.sync();
                store.close();
                dbManager.close();
                locManager.removeUpdates(locListener);
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteEntry(Entry entry) {
        try {
            dbManager.open();
            store = DbxDatastore.openDefault(mAccount);
            dbManager.deleteAllDataPoints();
            table = store.getTable("GeoData");
            DbxTable.QueryResult results = table.query();
            Iterator<DbxRecord> res = results.iterator();
            while (res.hasNext()) {
                res.next().deleteRecord();
            }
            store.sync();
            store.close();
            dbManager.close();
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public void deleteAll() {
        try {
            mAccount = mAccountManager.getLinkedAccount();

            dbManager.open();
            store = DbxDatastore.openDefault(mAccount);
            dbManager.deleteAllDataPoints();
            table = store.getTable("GeoData");
            DbxTable.QueryResult results = table.query();
            Iterator<DbxRecord> res = results.iterator();
            while (res.hasNext()) {
                res.next().deleteRecord();
            }
            store.sync();
            store.close();
            dbManager.close();
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    private Location updateLocation() {
        return locListener.updateLocation();
    }
}
