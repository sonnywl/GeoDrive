
package com.geodrive.fragments.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.dropbox.client2.DropboxAPI.Entry;

/**
 * Dialog fragment to prompt user for: Sharing, Sync
 * 
 * @author Sonny
 */
public class FileDialog extends DialogFragment {
    public static final String TAG = FileDialog.class.getSimpleName();
    private FileDialogOnClickListener callback;
    private String fileName;
    private static final String FILENAME = "filename";

    public static FileDialog newInstance(Entry fileTarget) {
        Bundle args = new Bundle();
        args.putString(FILENAME, fileTarget.fileName());
        FileDialog dialog = new FileDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (FileDialogOnClickListener) activity;
            fileName = getArguments().getString(FILENAME);
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement FileDialogOnClickListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Options").setMessage(fileName)
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
        return builder.create();
    }

    public enum FileDialogOptions {
        SHARE, SYNC, CANCEL, OPEN
    };

    public interface FileDialogOnClickListener {

        /**
         * Returns the selected enum of the user
         * 
         * @param options
         */
        void notifyDialogListener(FileDialogOptions options);
    }

}
