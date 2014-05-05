
package com.geodrive;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.geodrive.fragments.DriveList;
import com.geodrive.service.DriveSyncService;
import com.geodrive.service.Preferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;

public class MainActivity extends FragmentActivity implements ConnectionCallbacks,
        OnConnectionFailedListener {
    public static final String AUTHORITY = "com.google.provider.NotePad";
    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_CODE_RESOLUTION = 0;
    private Account mAccount;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new DriveList())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            Log.i(TAG, "Refresh Clicked");
        } else if (id == R.id.action_preference) {
            startActivity(new Intent(getApplicationContext(), Preferences.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "OnResume called");
        bindService();
        requestAccount();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
        }
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
                    requestAccount();
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

        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG, "Google API client connected.");
            DriveFolder folder = Drive.DriveApi.getRootFolder(mGoogleApiClient);
            Log.i(TAG, "Google API folder is " + folder.getDriveId());
            requestSync();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    private void requestAccount() {
        // Called after a file is saved to Drive.
        mAccount = new GoogleAccountManager(getApplicationContext())
                .getAccountByName(PreferenceManager.getDefaultSharedPreferences(
                        getApplicationContext()).getString(
                        Preferences.SELECTED_ACCOUNT_USER, ""));
        Log.i(TAG, "REQUEST_CODE_RESOLUTION " + mAccount);
        if (mAccount == null) {
            startActivity(new Intent(getApplicationContext(), Preferences.class));
        }
    }

    private void requestSync() {
        if (mAccount != null && mAccount.name.length() > 0) {
            final GoogleAccountManager accountManager = new GoogleAccountManager(this);
            Account account = accountManager.getAccountByName(mAccount.name);

            if (account != null) {
                Bundle options = new Bundle();
                options.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                ContentResolver.requestSync(account, "com.geodrive.MainActivity", options);
            }
        }
    }
}
