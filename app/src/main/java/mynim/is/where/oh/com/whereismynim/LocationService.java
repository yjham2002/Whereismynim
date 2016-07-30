package mynim.is.where.oh.com.whereismynim;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class LocationService implements LocationListener {

    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10.0f; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    public static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 2014112021;

    private final static boolean forceNetwork = false;
    private static LocationService instance = null;
    private LocationManager locationManager;
    public Location location;
    public double longitude;
    public double latitude;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean locationServiceAvailable = false;

    public static LocationService getLocationManager(Context context) {
        return new LocationService(context);
    }

    private LocationService(Context context) {
        initLocationService(context);
    }

    boolean checkPermission(Context context){
        return !( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    public void updateCoord(){
        if(location != null){
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
        }
    }

    @TargetApi(23)
    private void initLocationService(Context context) {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity)context, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_COURSE_LOCATION);
        }
        try {
            this.longitude = 0.0;
            this.latitude = 0.0;
            this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (forceNetwork) isGPSEnabled = false;
            if (!isNetworkEnabled && !isGPSEnabled){
                this.locationServiceAvailable = false;
            }
            else {
                this.locationServiceAvailable = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null)   {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        updateCoord();
                    }
                }//end if

                if (isGPSEnabled)  {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null)  {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        updateCoord();
                    }
                }
            }
        } catch (Exception ex)  {
            Log.e("GPS", ex.toString());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle b){}

    @Override
    public void onLocationChanged(Location location)     {
        // do stuff here with location object
    }

    @Override
    public void onProviderEnabled(String s){
    }

    @Override
    public void onProviderDisabled(String s){
    }

}
