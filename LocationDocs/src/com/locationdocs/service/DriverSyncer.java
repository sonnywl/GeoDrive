
package com.locationdocs.service;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;

public class DriverSyncer {
    static final String TAG = DriverSyncer.class.getSimpleName();
    private Context mContext;
    private ContentProviderClient mProvider;
    private Account mAccount;
    private Drive mService;

    public DriverSyncer(Context context, ContentProviderClient provider, Account account) {
        mContext = context;
        mProvider = provider;
        mAccount = account;
        mService = getDriveService();
    }

    public void performSync() {

    }

    private Drive getDriveService() {
        Log.i(TAG, "Driver Service Logging in");
        if (mService != null) {
            try {
                GoogleAccountCredential credential =
                        GoogleAccountCredential.usingOAuth2(mContext,
                                Arrays.asList(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccountName(mAccount.name);
                // Trying to get a token right away to see if we are authorized
                credential.getToken();
                mService = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(), credential).build();
            } catch (Exception e) {
                Log.e(TAG, "Failed to get token");
                // If the Exception is User Recoverable, we display a
                // notification that will trigger the
                // intent to fix the issue.
                if (e instanceof UserRecoverableAuthException) {
                    UserRecoverableAuthException exception = (UserRecoverableAuthException) e;
                    NotificationManager notificationManager = (NotificationManager) mContext
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent authorizationIntent = exception.getIntent();
                    authorizationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(
                            Intent.FLAG_FROM_BACKGROUND);
                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
                            authorizationIntent, 0);
                    Notification notification = new Notification.Builder(mContext)
                            .setSmallIcon(android.R.drawable.ic_dialog_alert)
                            .setTicker("Permission requested")
                            .setContentTitle("Permission requested")
                            .setContentText("for account " + mAccount.name)
                            .setContentIntent(pendingIntent).setAutoCancel(true).build();
                    notificationManager.notify(0, notification);
                } else {
                    e.printStackTrace();
                }
            }

        }
        return mService;
    }
}
