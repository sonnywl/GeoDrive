
package com.geodrive.fragments;

import android.accounts.Account;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.geodrive.DriveFileInfo;
import com.geodrive.MainActivity;
import com.geodrive.R;
import com.geodrive.adaptor.DriveListAdapter;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.query.Query;

import java.util.ArrayList;

public class DriveList extends ListFragment {
    public static final String TAG = DriveList.class.getSimpleName();
    DriveListAdapter mResultsAdapter;
    ArrayList<DriveFileInfo> files = new ArrayList<DriveFileInfo>();

    MainActivity mActivity;
    Account mAccount;

    private String mNextPageToken;
    private boolean mHasMore = true;

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
        mResultsAdapter = new DriveListAdapter(getActivity().getApplicationContext(), files);
        setListAdapter(mResultsAdapter);
    }

    public void refreshList() {
        Log.i(TAG, "Refresh list received");
        retrieveNextPage();
    }

    private void retrieveNextPage() {
        // if there are no more results to retrieve,
        // return silently.
        if (!mHasMore) {
            return;
        }
        // retrieve the results for the next page.
        Query query = new Query.Builder()
                .setPageToken(mNextPageToken)
                .build();
        Drive.DriveApi.query(mActivity.getGoogleApiClient(), query)
                .setResultCallback(metadataBufferCallback);
    }

    /**
     * Appends the retrieved results to the result buffer.
     */
    private final ResultCallback<MetadataBufferResult> metadataBufferCallback = new
            ResultCallback<MetadataBufferResult>() {
                @Override
                public void onResult(MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.e(TAG, "Problem while retrieving files");
                        return;
                    }
                    Log.i(TAG, "Adding MetaData");
                    mResultsAdapter.append(result.getMetadataBuffer());
                    mNextPageToken = result.getMetadataBuffer().getNextPageToken();
                    mHasMore = mNextPageToken != null;
                }
            };

}
