
package com.locationdocs;

import android.accounts.Account;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.locationdocs.fragments.DriveList;
import com.locationdocs.service.DriveSyncService;
import com.locationdocs.service.Preferences;

public class MainActivity extends FragmentActivity implements ConnectionCallbacks,
        OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DriveSyncService mService;
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_CODE_RESOLUTION = 0;
    private Account mAccount;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new DriveList())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_refresh:
                break;
            case R.id.action_preference:
                startActivity(new Intent(getApplicationContext(), Preferences.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "OnResume called");
        bindService();
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and
            // connection failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        unBindService();
        super.onPause();
    }

    private void bindService() {
        startService(new Intent(this, DriveSyncService.class));
        mBound = true;
    }

    private void unBindService() {
        if (mBound) {
            stopService(new Intent(this, DriveSyncService.class));
            mBound = false;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.i(TAG, "ACTIVITY RETURNED");
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_RESOLUTION:
                    // Called after a file is saved to Drive.
                    Log.i(TAG, "REQUEST_CODE_RESOLUTION");
                    mAccount = new GoogleAccountManager(getApplicationContext())
                            .getAccountByName(PreferenceManager.getDefaultSharedPreferences(
                                    getApplicationContext()).getString(
                                    Preferences.SELECTED_ACCOUNT_USER, ""));
                    if (mAccount == null) {
                        startActivity(new Intent(getApplicationContext(), Preferences.class));
                    }
                    break;
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Google API client connected.");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

}
