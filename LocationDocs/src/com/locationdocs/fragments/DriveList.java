
package com.locationdocs.fragments;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.locationdocs.DriveFileInfo;
import com.locationdocs.MainActivity;
import com.locationdocs.R;
import com.locationdocs.adaptor.DriveListAdapter;
import com.locationdocs.service.Preferences;

public class DriveList extends ListFragment {
    static final String TAG = DriveList.class.getSimpleName();
    DriveListAdapter adapter;
    DriveFileInfo[] files = new DriveFileInfo[] {};

    MainActivity mActivity;
    Account mAccount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.fragment_main, container,
                false);
        mActivity = (MainActivity) getActivity();
        return rl;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new DriveListAdapter(getActivity().getApplicationContext(), files);
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAccount =
                new GoogleAccountManager(getActivity().getApplicationContext())
                        .getAccountByName(PreferenceManager
                                .getDefaultSharedPreferences(getActivity().getApplicationContext())
                                .getString(
                                        Preferences.SELECTED_ACCOUNT_USER,
                                        ""));
        if (mAccount == null) {
            // Show the Preferences screen.
            startActivity(new Intent(mActivity.getApplicationContext(), Preferences.class));
        } else {
            loadDrive();
        }
    }

    private void loadDrive() {
        Log.i(TAG, "Load Drive");
    }

}
