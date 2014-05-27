
package com.geodrive.fragments.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ShareFileDialog extends DialogFragment {

    public static final String TAG = ShareFileDialog.class.getSimpleName();
    private static final String FILENAME = "filename";
    private static final String CONTACTS = "contacts";
    private ShareFileDialogOnClickListener callback;
    String fileName;

    public static ShareFileDialog newInstance(String fileName, String[] contacts) {
        Bundle args = new Bundle();
        args.putString(FILENAME, fileName);
        args.putStringArray(CONTACTS, contacts);
        ShareFileDialog dialog = new ShareFileDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (ShareFileDialogOnClickListener) activity;
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
                .setPositiveButton("Email", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callback.notifyShareFileDialogListener(ShareFileDialogOptions.EMAIL);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callback.notifyShareFileDialogListener(ShareFileDialogOptions.CANCEL);
                    }
                });
        return builder.create();
    }

    public enum ShareFileDialogOptions {
        EMAIL, SMS, BLUETOOTH, CANCEL
    };

    public interface ShareFileDialogOnClickListener {

        /**
         * Returns the selected enum of the user
         * 
         * @param options
         */
        void notifyShareFileDialogListener(ShareFileDialogOptions options);
    }

}
