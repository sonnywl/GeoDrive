
package com.geodrive.files.tasks;

import com.dropbox.client2.DropboxAPI.DropboxLink;
import com.dropbox.client2.DropboxAPI.Entry;

public interface IFileListenerTask {
    public enum FileTaskState {
        COMPLETED_DOWNLOAD, FAILED_DOWNLOAD, IN_PROGRESS_DOWNLOAD,
        COMPLETED_UPLOAD, FAILED_UPLOAD, IN_PROGRESS_UPLOAD,
        SHARE_LINK
    };

    /**
     * Returns the files created from the AsyncTask to the listener
     * 
     * @param files
     */

    void notifyFileManagerListenerShareLink(FileTaskState state, DropboxLink link);

    void notifyFileManagerListener(FileTaskState state);

    void notifyFileTaskListener(Entry[] files);
}
