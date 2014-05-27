
package com.geodrive.files.dropbox;

public class FileDataStore {
    public final String taskId;
    public final double longitude;
    public final double latitude;
    public final String path;
    public final String mtime;

    public static FileDataStore newInstance(String task_id,
            double longit, double lat,
            String pathDir, String time) {
        return new FileDataStore(task_id, longit, lat, pathDir, time);
    }

    private FileDataStore(String task_id, double longit, double lat, String pathDir, String time) {
        taskId = task_id;
        longitude = longit;
        latitude = lat;
        path = pathDir;
        mtime = time;
    }

    public String getFileName() {
        return path.substring(path.lastIndexOf("/"), path.length());
    }
}
