
package com.geodrive.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geodrive.HomeActivity;
import com.geodrive.R;
import com.geodrive.adapters.ContactAdapter;

public class ContactList extends ListFragment {
    public static final String TAG = ContactList.class.getSimpleName();
    private ContactAdapter mAdapter;
    private HomeActivity activity;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Create a projection that limits the result Cursor
        // to the required columns.
        String[] projectionEmail = {
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Email.DATA
        };
        // Get a Cursor over the Contacts Provider.
        Cursor emails = activity.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, projectionEmail,
                null, null, null);
        int emailsNameIdx = emails
                .getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME);
        int emailsDataIdx = emails
                .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA);

        // Get the index of the columns.
        // Initialize the result set.
        String[] result = new String[emails.getCount()];
        // Iterate over the result Cursor.
        while (emails.moveToNext()) {
            String emailName = emails.getString(emailsNameIdx);
            String emailData = emails.getString(emailsDataIdx);
            result[emails.getPosition()] = emailName + "\n" +
                    emailData;
        }
        emails.close();

        mAdapter = new ContactAdapter(activity, result);
        setListAdapter(mAdapter);
        registerForContextMenu(getListView());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        activity = (HomeActivity) getActivity();
        setHasOptionsMenu(true);
        return rootView;
    }

    public String[] getSelectedData() {
        return mAdapter.getSelectedData();
    }

}
