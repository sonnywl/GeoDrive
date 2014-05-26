
package com.geodrive.files.tasks;

import com.dropbox.client2.DropboxAPI.Entry;

public interface IFileTaskListener {
    public enum FileTaskState {
        COMPLETED_DOWNLOAD, FAILED_DOWNLOAD, IN_PROGRESS_DOWNLOAD,
        COMPLETED_UPLOAD, FAILED_UPLOAD, IN_PROGRESS_UPLOAD
    };

    /**
     * Returns the files created from the AsyncTask to the listener
     * 
     * @param files
     */
    void notifyFileManagerListener(FileTaskState state);

    void notifyFileTaskListener(Entry[] files);
}
