package it.univaq.ing.myshiprace.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ktulu on 15/01/18.
 */

public class MyLocationService extends Service
{
    public static final String ACTION_GET_POSITION = "action_get_position";
    private static final String TAG = "MyLocationService";
    private static boolean isRunning = false;

    List<LatLng> percorsoBarca;
    private LocationManager locationManager = null;
    private Location locationOld;

    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 5;

    public static boolean isRunning()
    {
        return isRunning;
    }

    private LocationListener locationListener = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            // Called when a new location is found by the network location provider.
            LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());

            Log.i(TAG, "Trying to get a location");
            if (percorsoBarca == null)
            {
                percorsoBarca = new ArrayList<>();
            }
            percorsoBarca.add(coordinate);
            if (locationOld == null)
            {
                locationOld = location;
            }

            double speed;
            if (location.hasSpeed())
            {
                speed = location.getSpeed();
            }
            else
            {
                speed = location.distanceTo(locationOld) / ((location.getTime() - locationOld.getTime()) / 1000);
            }
            float bearing = locationOld.bearingTo(location);
            locationOld = location;

            Intent intent = new Intent(ACTION_GET_POSITION);
            intent.putExtra("longitude", location.getLongitude());
            intent.putExtra("latitude", location.getLatitude());
            intent.putExtra("speed", speed);
            intent.putExtra("bearing", bearing);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        public void onProviderEnabled(String provider)
        {
            if (provider.equals(LocationManager.GPS_PROVIDER))
            {
                try
                {
                    locationManager.removeUpdates(locationListener);
                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    String locationProvider = LocationManager.GPS_PROVIDER;
                    locationManager.requestLocationUpdates(locationProvider, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener);
                }
                catch (SecurityException e)
                {
                    Log.i(TAG, "fail to request location update, ignore", e);
                }
//
            }
        }

        public void onProviderDisabled(String provider)
        {
            if (provider.equals(LocationManager.GPS_PROVIDER))
            {
                //TODO insert notification

            }
        }

    };


    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        isRunning = true;
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String locationProvider = locationManager.getBestProvider(criteria, false);
        try
        {
            Log.w(TAG, "Registering listener, provider: " + locationProvider);
            locationManager.requestLocationUpdates(locationProvider, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener);
        }
        catch (java.lang.SecurityException ex)
        {
            Log.i(TAG, "fail to request location update, ignore", ex);
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (locationManager != null)
        {
            try
            {
                locationManager.removeUpdates(locationListener);
                isRunning = false;
            }
            catch (Exception ex)
            {
                Log.i(TAG, "fail to remove location listener, ignore", ex);
            }
        }
    }

    private void initializeLocationManager()
    {
        Log.e(TAG, "initializeLocationManager - LOCATION_INTERVAL: " + LOCATION_INTERVAL + " LOCATION_DISTANCE: " + LOCATION_DISTANCE);
        if (locationManager == null)
        {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
