
package com.geodrive;

public class DriveFileInfo {

    private String filename;

    private String metadata;

    public DriveFileInfo(String file, String data) {
        filename = file;
        metadata = data;
    }

    public String getFilename() {
        return filename;
    }

    public String getMetadata() {
        return metadata;
    }
}
