
package com.geodrive;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.geodrive.files.FileManager;
import com.geodrive.files.dropbox.DataStoreManager;
import com.geodrive.fragments.FileList;
import com.geodrive.fragments.dialog.FileDialog;
import com.geodrive.fragments.dialog.FileDialog.FileDialogOptions;
import com.geodrive.preferences.SharedPreferenceManager;

public class HomeActivity extends ActionBarActivity
        implements FileDialog.FileDialogOnClickListener {
    public static String TAG = HomeActivity.class.getSimpleName();
    private SharedPreferenceManager sManager;
    private FileManager fManager;
    private DataStoreManager dManager;

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
        sManager = SharedPreferenceManager.getInstance(getApplicationContext());
        fManager = FileManager.getInstance(getApplicationContext());
        dManager = DataStoreManager.getInstance(getApplicationContext(), this);
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
            fManager.link();
            return true;
        } else if (id == R.id.action_logout) {
            sManager.clearAll();
            fManager.unlink();
        } else if (id == R.id.action_refresh) {
            // dManager.getLinkedAccount();
        } else if (id == R.id.action_clear) {
            dManager.deleteAll();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        fManager.checkAuth();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public FileManager getFileManager() {
        return fManager;
    }

    public DataStoreManager getDataStoreManager() {
        return dManager;
    }

    @Override
    public void notifyDialogListener(FileDialogOptions options) {
        FileList list = (FileList) getSupportFragmentManager().findFragmentById(R.id.container);
        list.notifyDialogListener(options);
    }
}
