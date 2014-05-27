
package com.geodrive.files;

import com.dropbox.client2.DropboxAPI.Entry;

public interface IFileManagerListener {
    /**
     * Returns the results of queries that are sent to the file manager via
     * FileTasks
     * 
     * @author Sonny
     */

    void notifyFileManagerShareLinkIsReady(String link, String[] recipents);

    void notifyFileManagerFileIsReady(Entry file, String cacheDir);

    void notifyFileManagerListener(Entry[] files);
}
