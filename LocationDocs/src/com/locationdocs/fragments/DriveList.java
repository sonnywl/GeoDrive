
package com.locationdocs.fragments;

import android.accounts.Account;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.locationdocs.DriveFileInfo;
import com.locationdocs.MainActivity;
import com.locationdocs.R;
import com.locationdocs.adaptor.DriveListAdapter;

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
    }


}
