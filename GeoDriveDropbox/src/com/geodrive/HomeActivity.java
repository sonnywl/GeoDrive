
package com.geodrive;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.geodrive.files.FileManager;
import com.geodrive.fragments.FileList;
import com.geodrive.preferences.SharedPreferenceManager;

public class HomeActivity extends FragmentActivity {
    public static String TAG = HomeActivity.class.getSimpleName();
    private SharedPreferenceManager sManager;
    private FileManager fManager;

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
        Log.i("MAIN", "Starting Authentication " + fManager.isLinked());
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "OnResume");
        fManager.checkAuth();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "Onpause");
        super.onPause();
    }

    public FileManager getFileManager() {
        return fManager;
    }
}
