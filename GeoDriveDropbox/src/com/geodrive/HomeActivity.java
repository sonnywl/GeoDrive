
package com.geodrive;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AppKeyPair;
import com.geodrive.files.FileInfo;
import com.geodrive.fragments.FileList;
import com.geodrive.preferences.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends FragmentActivity {
    public static String TAG = HomeActivity.class.getSimpleName();
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private SharedPreferenceManager manager;

    public static final int AUTH_INTIAL = 1;
    public static final int AUTH_PROGRESS = 2;
    public static final int AUTH_COMPLETED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new FileList())
                    .commit();
        }
        manager = SharedPreferenceManager.getInstance(getApplicationContext());

        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        Log.i("MAIN", "Starting Authentication ");

    }

    public boolean isLinked() {
        return mDBApi.getSession().isLinked();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_login) {
            mDBApi.getSession().startOAuth2Authentication(this);
            return true;
        } else if (id == R.id.action_logout) {
            mDBApi.getSession().unlink();
            manager.clearAll();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "OnResume");
        checkAuth();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "Onpause");
        super.onPause();
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(StaticInfo.APP_KEY, StaticInfo.APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    public void checkAuth() {
        int authState = manager.getIntValue(StaticInfo.APP_AUTH, AUTH_INTIAL);
        AndroidAuthSession session = mDBApi.getSession();

        Log.i(TAG, "Auth State " + authState + " " + session.authenticationSuccessful());
        if (session.authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the
                // session
                session.finishAuthentication();
                Log.i("DbAuthLog", "Auth Success");

                storeAuth(session);
                manager.updateInt(StaticInfo.APP_AUTH, AUTH_COMPLETED);
                updateList();

            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
        manager.updateInt(StaticInfo.APP_AUTH, authState);
    }

    public void updateList() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Entry dropboxDir;
                ArrayList<FileInfo> files = new ArrayList<FileInfo>();

                try {
                    dropboxDir = mDBApi.metadata("/", 0, null, true, null);
                    if (dropboxDir.isDir) {
                        List<Entry> contents = dropboxDir.contents;
                        if (contents != null) {
                            // ------CREATE NEW ENTRY AND THEN, GET THE FILENAME
                            // OF
                            // EVERY FILE
                            for (int i = 0; i < contents.size(); i++) {
                                Entry e = contents.get(i);
                                String a = e.fileName();
                                FileInfo file = new FileInfo(e.fileName(), e.clientMtime);
                                files.add(file);
                                Log.d("dropbox", "FileName:" + a);
                            }
                        }
                        updateListAdapter(files.toArray(new FileInfo[files.size()]));
                    }
                } catch (DropboxUnlinkedException e) {
                    buildSession();
                } catch (DropboxException e1) {
                    e1.printStackTrace();
                }
            }

        }).start();
    }

    private void updateListAdapter(final FileInfo[] files) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FileList list = (FileList) getSupportFragmentManager().findFragmentById(
                        R.id.container);
                list.updateList(files);
            }

        });
    }

    private void loadAuth(AndroidAuthSession session) {
        String tokenKey = manager.getStringValue(StaticInfo.APP_KEY, null);
        String secret = manager.getStringValue(StaticInfo.APP_SECRET, null);
        if (tokenKey == null || secret == null ||
                tokenKey.length() == 0 || secret.length() == 0) {
            return;
        }
        session.setOAuth2AccessToken(secret);
    }

    private void storeAuth(AndroidAuthSession session) {
        String oauth2AccessToken = session.getOAuth2AccessToken();
        manager.updateString(StaticInfo.APP_KEY, "oauth2:");
        manager.updateString(StaticInfo.APP_SECRET, oauth2AccessToken);
        manager.updateBoolean(StaticInfo.APP_AUTH, true);
        Log.i(TAG,
                "Stored "
                        + manager.getStringValue(StaticInfo.APP_SECRET, "Error:"
                                + oauth2AccessToken));
    }

}
