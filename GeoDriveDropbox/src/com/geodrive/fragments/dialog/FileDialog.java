
package com.geodrive.fragments.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.geodrive.fragments.dialog.FileDialogOnClickListener.FileDialogOptions;

/**
 * Dialog fragment to prompt user for: Sharing, Sync
 * 
 * @author Sonny
 */
public class FileDialog extends DialogFragment {
    public static final String TAG = FileDialog.class.getSimpleName();
    private FileDialogOnClickListener callback;

    public static FileDialog newInstance(FileDialogOnClickListener listener) {
        return new FileDialog(listener);
    }

    private FileDialog(FileDialogOnClickListener listener) {
        callback = listener;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Options").setMessage("Dropbox Prompt")
                .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callback.notifyDialogListener(FileDialogOptions.SHARE);
                    }
                })
                .setNeutralButton("Sync", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callback.notifyDialogListener(FileDialogOptions.SYNC);
                    }
                })
                .setNegativeButton("Open", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callback.notifyDialogListener(FileDialogOptions.OPEN);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
