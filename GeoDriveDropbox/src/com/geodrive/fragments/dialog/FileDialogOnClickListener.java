
package com.geodrive.fragments.dialog;

public interface FileDialogOnClickListener {
    public enum FileDialogOptions {
        SHARE, SYNC, CANCEL, OPEN
    };

    /**
     * Returns the selected enum of the user
     * 
     * @param options
     */
    void notifyDialogListener(FileDialogOptions options);
}
