
package com.geodrive.files;

import com.dropbox.client2.DropboxAPI.Entry;

public interface FileManagerListener {
    /**
     * Returns the results of queries that are sent to the file manager via
     * FileTasks
     * 
     * @author Sonny
     */
    void notifyFileManagerFileReady(Entry file, String cacheDir);

    void notifyFileManagerListener(FileInfo[] files);
}
