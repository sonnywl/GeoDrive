
package com.geodrive.files;

import com.dropbox.client2.DropboxAPI.Entry;

public class FileInfo {

    private String metadata;
    private Entry entry;

    public FileInfo(Entry e) {
        entry = e;
        metadata = e.path;
    }

    public Entry getEntry() {
        return entry;
    }

    public String getFilename() {
        return entry.fileName();
    }

    public boolean isDirectory() {
        return entry.isDir;
    }

    public String getMetadata() {
        return metadata;
    }

    public String toString() {
        return entry.fileName() + "  " + entry.isDir + "  " + metadata;
    }
}
