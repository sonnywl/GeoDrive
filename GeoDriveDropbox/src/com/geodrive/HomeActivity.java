
package com.geodrive;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI.Entry;
import com.geodrive.files.FileManager;
import com.geodrive.files.dropbox.DataStoreManager;
import com.geodrive.fragments.ContactList;
import com.geodrive.fragments.FileList;
import com.geodrive.fragments.dialog.FileDialog;
import com.geodrive.fragments.dialog.FileDialog.FileDialogOptions;
import com.geodrive.fragments.dialog.ShareFileDialog;
import com.geodrive.fragments.dialog.ShareFileDialog.ShareFileDialogOptions;
import com.geodrive.preferences.SharedPreferenceManager;

public class HomeActivity extends ActionBarActivity
        implements FileDialog.FileDialogOnClickListener,
        ShareFileDialog.ShareFileDialogOnClickListener {
    public static String TAG = HomeActivity.class.getSimpleName();
    private SharedPreferenceManager sManager;
    private FileManager fManager;
    private DataStoreManager dManager;
    private Entry fileTarget;

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
        for (int i = 0; i < menu.size(); i++) {
            if (sharedState) {
                if (menu.getItem(i).getItemId() != R.id.action_confirm) {
                    menu.getItem(i).setVisible(false);
                }
            } else {
                if (menu.getItem(i).getItemId() == R.id.action_confirm) {
                    menu.getItem(i).setVisible(false);
                }
            }
        }
        return true;
    }

    boolean sharedState = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_login) {
            fManager.link();
            return true;
        } else if (id == R.id.action_logout) {
            sManager.clearAll();
            fManager.unlink();
            Toast.makeText(this, R.string.logout, Toast.LENGTH_SHORT).show();

        } else if (id == R.id.action_clear) {
            dManager.deleteAll();
            Toast.makeText(this, "Database Cleared", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.action_confirm) {
            sharedState = false;
            ContactList list = (ContactList) getSupportFragmentManager().findFragmentById(
                    R.id.container);
            if (fileTarget != null) {
                fManager.shareFile(fileTarget, list.getSelectedData());
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new FileList())
                    .commit();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        fManager.checkAuth();
    }

    @Override
    public void notifyFileDialogListener(FileDialogOptions options) {
        FileList list = (FileList) getSupportFragmentManager().findFragmentById(R.id.container);
        list.notifyFileDialogListener(options);
    }

    @Override
    public void notifyShareFileDialogListener(ShareFileDialogOptions options) {
        FileList list = (FileList) getSupportFragmentManager().findFragmentById(R.id.container);
        list.notifyShareFileDialogListener(options);
        switch (options) {
            default:
            case SMS:
                break;
            case EMAIL:
                fileTarget = list.getFileTarget();
                invalidateOptionsMenu();
                sharedState = true;
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new ContactList()).commit();
                break;
        }
    }

    public FileManager getFileManager() {
        return fManager;
    }

    public DataStoreManager getDataStoreManager() {
        return dManager;
    }
}
