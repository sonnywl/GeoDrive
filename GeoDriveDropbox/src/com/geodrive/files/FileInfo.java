
package com.geodrive.files;

public class FileInfo {
    private String filename;

    private String metadata;

    public FileInfo(String file, String data) {
        filename = file;
        metadata = data;
    }

    public String getFilename() {
        return filename;
    }

    public String getMetadata() {
        return metadata;
    }

    public String toString() {
        return filename + "  " + metadata;
    }
}
