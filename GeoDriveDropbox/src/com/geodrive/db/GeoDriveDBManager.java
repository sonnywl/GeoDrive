
package com.geodrive.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.geodrive.files.dropbox.FileDataStore;

/**
 * SQLite Manager for file metadata that we defined for location information
 * recent used path location
 */
public class GeoDriveDBManager {
    public static final String TAG = GeoDriveDBManager.class.getSimpleName();
    public static GeoDriveDBManager manager;
    private DbHelper dbhelper;
    private SQLiteDatabase db;
    private Context context;

    public static GeoDriveDBManager getInstance(Context context) {
        if (manager == null) {
            manager = new GeoDriveDBManager(context);
            return manager;
        }
        return manager;
    }

    private GeoDriveDBManager(Context mContext) {
        context = mContext;
    }

    public GeoDriveDBManager open() {
        dbhelper = new DbHelper(context);
        db = dbhelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbhelper.close();
        db.close();
    }

    public long createDataPoint(String taskId, double longitude, double latitude,
            String mTime, String path) {
        ContentValues cv = new ContentValues();
        cv.put(TASK_ID, taskId);
        cv.put(LONG, longitude);
        cv.put(LAT, latitude);
        cv.put(RECENT, mTime);
        cv.put(PATH, path);
        return db.insert(DB_NAME, null, cv);
    }

    public long updateDataPoint(String taskId, double longitude, double latitude,
            String mTime, String path) {
        ContentValues cv = new ContentValues();
        cv.put(LONG, longitude);
        cv.put(LAT, latitude);
        cv.put(RECENT, mTime);
        cv.put(PATH, path);
        return db.update(DB_NAME, cv, TASK_ID + "='" + taskId + "'", null);
    }

    public long deleteDataPoint(String taskId) {
        return db.delete(DB_NAME, TASK_ID + "='" + taskId + "'", null);
    }

    public long deleteAllDataPoints() {
        return db.delete(DB_NAME, null, null);
    }

    public FileDataStore queryData(String query_path) {
        String[] columns = new String[] {
                TASK_ID, LONG, LAT, PATH, RECENT
        };
        Cursor query = db
                .query(DB_NAME, columns, PATH + " = '" + query_path + "'", null, null, null, null);
        int taskId = query.getColumnIndex(TASK_ID);
        int longit = query.getColumnIndex(LONG);
        int lat = query.getColumnIndex(LAT);
        int path = query.getColumnIndex(PATH);
        int recent = query.getColumnIndex(RECENT);

        for (query.moveToFirst(); !query.isAfterLast();) {
            FileDataStore id = FileDataStore.newInstance(
                    query.getString(taskId),
                    query.getDouble(longit),
                    query.getDouble(lat),
                    query.getString(path),
                    query.getString(recent));
            return id;
        }
        return null;
    }

    private static class DbHelper extends SQLiteOpenHelper {
        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DB_NAME + " ("
                    + TASK_ID + " TEXT,"
                    + LONG + " DOUBLE(5),"
                    + LAT + " DOUBLE(5), "
                    + RECENT + " TEXT, "
                    + PATH + " TEXT);");
            Log.i(TAG, "GeoDriveDB Created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP DATABASE IF EXISTS " + DB_NAME);
            onCreate(db);
        }
    }

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "GeoDriveDB";
    public static final String LONG = "long";
    public static final String LAT = "lat";
    public static final String RECENT = "client_mtime";
    public static final String TASK_ID = "id";
    public static final String PATH = "path";
}
