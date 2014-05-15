
package com.geodrive.files;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Generates the longitude and latitude for this file
 * 
 * @author Sonny
 */
public class IFileLocationListener implements LocationListener {
    private LocationManager locManager;

    private boolean isGPSEnabled = false, isNetworkEnabled = false, canGetLocation = false;
    private LocationListener listener;
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    private Context mContext;

    public IFileLocationListener(Context applicationContext) {
        mContext = applicationContext;
        locManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

    }

    public Location updateLocation() {
        try {
            // getting GPS status
            isGPSEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locManager != null) {
                        location = locManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locManager != null) {
                            location = locManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    // ----- Location Based Information
    @Override
    public void onLocationChanged(Location location) {
        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());
        Toast.makeText(mContext, "location changed: lat=" + lat + ", lon=" + lon
                , Toast.LENGTH_SHORT).show();;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(mContext, "status changed to " + provider + " [" + status + "]"
                , Toast.LENGTH_SHORT).show();;
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(mContext, "provider enabled " + provider
                , Toast.LENGTH_SHORT).show();;
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(mContext, "provider disabled " + provider
                , Toast.LENGTH_SHORT).show();;
    }

}
