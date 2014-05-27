
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
import java.util.ArrayList;
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
        mAccount = mAccountManager.getLinkedAccount();
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

    public Entry[] queryAllLocationFiles(Location loc) {
        dbManager.open();

        Log.i(TAG, loc.getLatitude() + " " + loc.getLongitude());
        FileDataStore[] store = dbManager.queryAllData();
        Entry[] entries = new Entry[store.length];
        for (int i = 0; i < store.length; i++) {
            Entry entry = new Entry();
            entry.path = store[i].path;
            entries[i] = entry;
            Log.i(TAG, entry.path);
        }

        dbManager.close();
        return entries;
    }

    public Entry[] queryCurrLocationFiles(Location loc) {
        dbManager.open();

        ArrayList<Entry> entries = new ArrayList<Entry>();
        FileDataStore[] store = dbManager.queryAllData();
        for (int i = 0; i < store.length; i++) {
            Entry entry = new Entry();
            entry.path = store[i].path;
            double distance = distance(loc.getLatitude(), loc.getLongitude()
                    , store[i].latitude, store[i].longitude, 'M');
            Log.i(TAG, entry.path);
            Log.i(TAG, "Loc " + loc.getLatitude() + " " + loc.getLongitude());
            Log.i(TAG, "Stor " + store[i].latitude + " " + store[i].longitude);
            Log.i(TAG, "Dis " + distance);
            if (distance < 10) {
                entries.add(entry);
            }
        }
        dbManager.close();

        return entries.toArray(new Entry[entries.size()]);
    }

    public FileDataStore[] queryFiles(final Entry[] files) {
        dbManager.open();
        FileDataStore[] res = dbManager.queryDataRange(files);
        dbManager.close();
        return res;
    }

    public void updateFile(final Entry entry) {
        if (mAccountManager.hasLinkedAccount()) {
            SimpleDateFormat date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            String mTime = date.format(new Date());
            Log.i(TAG, mTime);
            try {
                dbManager.open();
                store = DbxDatastore.openDefault(mAccount);
                table = store.getTable("GeoData");
                Location loc = getLocation();
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

    public Location getLocation() {
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2, locListener);
        Location loc = locListener.getLocation();
        locManager.removeUpdates(locListener);
        return loc;
    }

    public String getAccountId() {
        return mAccount.getUserId();
    }

    //@formatter:off
    /*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::                                                                         :*/
    /*::  This routine calculates the distance between two points (given the     :*/
    /*::  latitude/longitude of those points). It is being used to calculate     :*/
    /*::  the distance between two locations using GeoDataSource (TM) prodducts  :*/
    /*::                                                                         :*/
    /*::  Definitions:                                                           :*/
    /*::    South latitudes are negative, east longitudes are positive           :*/
    /*::                                                                         :*/
    /*::  Passed to function:                                                    :*/
    /*::    lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees)  :*/
    /*::    lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees)  :*/
    /*::    unit = the unit you desire for results                               :*/
    /*::           where: 'M' is statute miles                                   :*/
    /*::                  'K' is kilometers (default)                            :*/
    /*::                  'N' is nautical miles                                  :*/
    /*::  Worldwide cities and other features databases with latitude longitude  :*/
    /*::  are available at http://www.geodatasource.com                          :*/
    /*::                                                                         :*/
    /*::  For enquiries, please contact sales@geodatasource.com                  :*/
    /*::                                                                         :*/
    /*::  Official Web site: http://www.geodatasource.com                        :*/
    /*::                                                                         :*/
    /*::           GeoDataSource.com (C) All Rights Reserved 2014                :*/
    /*::                                                                         :*/
    /*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    //@formatter:on
    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
