/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.univaq.ing.myshiprace.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.Database.DBHelper;
import it.univaq.ing.myshiprace.FragmentList;
import it.univaq.ing.myshiprace.MapsActivity;
import it.univaq.ing.myshiprace.R;
import it.univaq.ing.myshiprace.Util.Preferences;
import it.univaq.ing.myshiprace.Util.Request;
import it.univaq.ing.myshiprace.Util.Utils;
import it.univaq.ing.myshiprace.model.Race;
import it.univaq.ing.myshiprace.model.ShipPosition;
import it.univaq.ing.myshiprace.model.Track;

/**
 * Service used to get locations update. When all the activities unbind from this service it is promoted to foreground service.
 * Code based on https://github.com/googlesamples/android-play-location
 */
public class LocationUpdatesService extends Service
{

    private static final String PACKAGE_NAME = "it.univaq.ing.myshiprace.locationupdatesforegroundservice";
    // Actions privided by this service
    public static final String ACTION_SERVICE_GET_NEW_POSITION = PACKAGE_NAME + ".get_position";
    public static final String ACTION_SERVICE_GET_UPDATED_TRACK = PACKAGE_NAME + ".get_update";
    // Intent filters provided by this service
    public static final String INTENT_LOCATION = PACKAGE_NAME + ".location";
    public static final String INTENT_SPEED = PACKAGE_NAME + ".speed";
    public static final String INTENT_BEARING = PACKAGE_NAME + ".bearing";
    public static final String INTENT_STARTED_FROM_NOTIFICATION = PACKAGE_NAME + ".started_from_notification";
    public static final String INTENT_NEAR_BOA = PACKAGE_NAME + ".near_boa";
    public static final String INTENT_BOA_NUMBER = PACKAGE_NAME + ".boa_number";
    public static final String INTENT_PERCORSO_BARCA = PACKAGE_NAME + ".percorso_barca";
    public static final String INTENT_DISTANCE = PACKAGE_NAME + ".distance";
    private static final String TAG = LocationUpdatesService.class.getSimpleName();
    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_01";
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    //    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    private static Race race = null;
    private static Track track = null;
    private static int currentBoa;
    private final IBinder mBinder = new LocalBinder();
    private List<Location> percorsoBarca = new ArrayList<>();
    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;
    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
//            Log.d("RECEIVER", "ho ricevuto qualcosa: " + intent.getAction());
            sendShipPositions();
        }
    };

    public LocationUpdatesService()
    {

    }

    public void startRace(Track t)
    {
        race = new Race();
        race.setTrackID(t.getId());
        race.setStartTime(new Timestamp(System.currentTimeMillis()));
        LocationUpdatesService.race = race;
        DBHelper.get(this).save(race);
        track = t;
        currentBoa = 1;
    }

    @Override
    public void onCreate()
    {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(INTENT_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification)
        {
            removeLocationUpdates();
            stopSelf();
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // Called when a client comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client unbinds from this
        // service. If this method is called due to a configuration change in MapsActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this))
        {
            Log.i(TAG, "Starting foreground service");

            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onRebind(Intent intent)
    {
        // Called when a client returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    /**
     * Returns the notification used as part of the foreground service.
     */
    private Notification getNotification()
    {
        Intent intent = new Intent(this, LocationUpdatesService.class);

        String text;
        StringBuilder bigText = new StringBuilder();

        if (currentBoa < track.length() - 1)
        {
            text = getString(R.string.next_buoy) + currentBoa;
        }
        else
        {
            text = getString(R.string.next_buoy_finish);
        }
        bigText.append(text).append(System.getProperty("line.separator"));
        bigText.append(Utils.getLocationText(mLocation, this));
        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(INTENT_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        Intent activityIntent = new Intent(this, MapsActivity.class);
        activityIntent.putExtra(INTENT_PERCORSO_BARCA, percorsoBarca.toArray(new Location[]{}));
        activityIntent.putExtra(INTENT_BOA_NUMBER, currentBoa);
        activityIntent.putExtra(INTENT_STARTED_FROM_NOTIFICATION, true);
        activityIntent.putExtra(FragmentList.INTENT_TRACK_OBJECT, DBHelper.get(this).getRaceTrack(race.getTrackID()).toJSONArray().toString());
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
                        activityPendingIntent)
                .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(Utils.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_notification_icon_2)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText.toString()));
//
//        // Set the Channel ID for Android O.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//        {
//            builder.setChannelId(CHANNEL_ID); // Channel ID
//        }

        return builder.build();
    }

    /**
     * Removes location updates.
     */
    public void removeLocationUpdates()
    {
        Log.i(TAG, "Removing location updates");
        try
        {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Utils.setRequestingLocationUpdates(this, false);
            unregisterReceiver(mNetworkReceiver);
            stopSelf();
        }
        catch (SecurityException unlikely)
        {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Makes a request for location updates
     */
    public void requestLocationUpdates()
    {
        Log.i(TAG, "Requesting location updates");
        Utils.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), LocationUpdatesService.class));

        try
        {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
            registerReceiver(mNetworkReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        }
        catch (SecurityException unlikely)
        {
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    private void getLastLocation()
    {
        try
        {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Location> task)
                        {
                            if (task.isSuccessful() && task.getResult() != null)
                            {
                                mLocation = task.getResult();
                                if (race != null)
                                    saveShipPosition(mLocation);
                            }
                            else
                            {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        }
        catch (SecurityException unlikely)
        {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location)
    {
        if (Utils.requestingLocationUpdates(this))
        {
            Log.i(TAG, "New location: " + location);

            if (mLocation == null)
            {
                mLocation = location;
            }
            float speed;
            if (location.hasSpeed())
            {
                speed = location.getSpeed();
            }
            else
            {
                speed = location.distanceTo(mLocation) / (location.getTime() - mLocation.getTime());
                //time is in milliseconds
                speed *= 1000;
            }
            float bearing = mLocation.bearingTo(location);
            if (bearing < 0)
                bearing += 180;
            bearing -= 90;
            mLocation = location;

            // Notify anyone listening for broadcasts about the new location.
            Intent intent = new Intent(ACTION_SERVICE_GET_NEW_POSITION);
            intent.putExtra(INTENT_LOCATION, location);
            intent.putExtra(INTENT_SPEED, speed);
            intent.putExtra(INTENT_BEARING, bearing);
            Location boalocation = new Location("");
            boalocation.setLatitude(track.getBoa(currentBoa).getLatitude());
            boalocation.setLongitude(track.getBoa(currentBoa).getLongitude());
            float distance = location.distanceTo(boalocation);
            intent.putExtra(INTENT_DISTANCE, distance);
            if (distance < Preferences.load(this, "pref_key_circle_radius", 20))
            {
                intent.putExtra(INTENT_NEAR_BOA, true);
                intent.putExtra(INTENT_BOA_NUMBER, currentBoa);
                if (currentBoa < track.length() - 1)
                {
                    ++currentBoa;
                }
            }
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            // Update notification content if running as a foreground service.
            if (serviceIsRunningInForeground(this))
            {
                mNotificationManager.notify(NOTIFICATION_ID, getNotification());
            }
            saveShipPosition(location);
            percorsoBarca.add(location);
        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Returns true if this is a foreground service.
     */
    public boolean serviceIsRunningInForeground(Context context)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE))
        {
            if (getClass().getName().equals(service.service.getClassName()))
            {
                if (service.foreground)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * Used to save the ship position (and trying to upload to the server)
     */
    private void saveShipPosition(Location location)
    {
        ShipPosition s = new ShipPosition();
        s.setTimestamp(new Timestamp(location.getTime()));
        s.setLatitude(location.getLatitude());
        s.setLongitude(location.getLongitude());
        s.setRaceID(race.getId());
        s.setShipName(Preferences.load(this, "pref_key_ship_name", ""));
        DBHelper.get(this).save(s);
        sendShipPositions();
    }

    /*
     * Send unsent ship positions to server
     */
    public void sendShipPositions()
    {
        if (isNetworkAvailable())
        {
            ShipPosition[] ships = DBHelper.get(this).getUntrasmitted(race.getId()).toArray(new ShipPosition[]{});
            new MyTask().execute(ships);
        }
    }

    public boolean requestUpdate()
    {
        if (race != null)
        {
            Intent intent = new Intent(ACTION_SERVICE_GET_UPDATED_TRACK);
            intent.putExtra(INTENT_PERCORSO_BARCA, percorsoBarca.toArray(new Location[]{}));
            intent.putExtra(INTENT_BOA_NUMBER, currentBoa);
//            intent.putExtra(FragmentList.INTENT_TRACK_OBJECT, DBHelper.get(this).getRaceTrack(race.getTrackID()).toJSONArray().toString());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            return true;
        }
        else
        {
            return false;
        }
    }

    /*
     * Used by MapsActivity to request updated track path data (after activity resuming)
     */

    private Boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * Class used for the client Binder
     */
    public class LocalBinder extends Binder
    {
        public LocationUpdatesService getService()
        {
            return LocationUpdatesService.this;
        }
    }

    /*
     * called by sendShipPositions to send data to server
     */
    private class MyTask extends AsyncTask<ShipPosition, Void, Void>
    {
        @Override
        protected Void doInBackground(ShipPosition... shipPositions)
        {
            boolean result = true;
            for (ShipPosition s : shipPositions)
            {
                String response = Request.doRequest(Preferences.load(getApplicationContext(), "pref_key_server_address", "http://ktulu.altervista.org"), new String[]{"PIPPO"}, new String[]{s.toJSONObject().toString()});
                result = result && response.equalsIgnoreCase("ok");
                if (result)
                {
                    Log.i(TAG, "INVIATO ID " + s.getId());
                    DBHelper.get(getApplicationContext()).setTransmitted(s);

                }
                else
                {
                    Log.e(TAG, "NON INVIATO ID " + s.getId());
                    break;
                }
            }
            return null;
        }
    }
}
