package it.univaq.ing.myshiprace;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.Util.Preferences;
import it.univaq.ing.myshiprace.Util.Utils;
import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.Track;
import it.univaq.ing.myshiprace.service.LocationUpdatesService;

/*
 * This activity is just a little more complex than others... it tracks ship movements on the map
 * while showing track to follow and buoys
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult>
{
    //used to show dialog prompting for user to enable GPS
    protected GoogleApiClient mGoogleApiClient;

    // sometimes we intercept LocationManager.PROVIDERS_CHANGED_ACTION two times (maybe because of GPS and network location change)
    // this flag help us showing only one dialog instead of two (and avoid inconsistent behaviour)
    private boolean isShow = false;

    //used as request number to show to show dialog prompting for user to enable GPS
    int REQUEST_CHECK_SETTINGS = 100;

    //same as above. taken from LocationUpdatesService
    protected LocationRequest locationRequest;

    //just a google map
    GoogleMap mMap;

    //list containing buoy coordinates
    List<LatLng> percorsoGara;

    //list containing ship positions during the race
    List<LatLng> percorsoBarca;

    boolean firstLoaded = true;

    // object containing the race track
    private Track track;

    //marker showing our current position
    private Marker currentBoatposition;

    //the polyline representing ship path
    private Polyline actualPath;

    private boolean isRegistered = false;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    //buttons to start and stop race
    private Button startRaceButton;
    private Button stopRaceButton;

    // Array containing buoy markers, used to change colors when approaching a buoy
    private ArrayList<Marker> boaMarkers;

    // index of current target boa, used as the above
    private int currentBoa;

    // check if we are resuming after a pause or we are resuming after onCreate
    private boolean isPaused = false;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // true if we are rotatind screen
    private boolean isConfigurationChanged;
    /*
     * Broadcast receiver, it tracks new position event, handle update request and location mode changes
     */
    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent == null || intent.getAction() == null) return;

            switch (intent.getAction())
            {
                /*
                 * If we got a new position we check if we are approaching a bouy, then we take speed and bearing
                 * data from intent and set a new point in the ship path. We move the marker in our current position
                 * and set informative text according to info received.
                 */
                case LocationUpdatesService.ACTION_SERVICE_GET_NEW_POSITION:

                    Location location = intent.getParcelableExtra(LocationUpdatesService.INTENT_LOCATION);

                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    if (intent.getBooleanExtra(LocationUpdatesService.INTENT_NEAR_BOA, false))
                    {
                        currentBoa = intent.getIntExtra(LocationUpdatesService.INTENT_BOA_NUMBER, -1);
                        if (currentBoa < track.length() - 1)
                        {
                            boaMarkers.get(currentBoa).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            Toast.makeText(context, getString(R.string.approaching_boa) + " " + currentBoa, Toast.LENGTH_LONG).show();

                            currentBoa++;
                            boaMarkers.get(currentBoa).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                        }
                        else
                        {
                            boaMarkers.get(currentBoa).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            Toast.makeText(context, R.string.approaching_finish, Toast.LENGTH_LONG).show();
                        }
                    }
                    float speed = intent.getFloatExtra(LocationUpdatesService.INTENT_SPEED, 0);
                    float bearing = intent.getFloatExtra(LocationUpdatesService.INTENT_BEARING, 0);
                    float distance = intent.getFloatExtra(LocationUpdatesService.INTENT_DISTANCE, 0);
                    if (!(longitude == Double.NaN))
                    {
                        LatLng coordinate = new LatLng(latitude, longitude);

                        if (percorsoBarca == null)
                        {
                            percorsoBarca = new ArrayList<>();

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 16));

                        }

                        percorsoBarca.add(coordinate);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate));

                        if (actualPath == null)
                        {
                            actualPath = mMap.addPolyline(new PolylineOptions()
                                    .width(5)
                                    .color(Color.RED));
                        }
                        actualPath.setPoints(percorsoBarca);

                        putCurrentPositionMarker(coordinate, bearing);
                        setTextView(latitude, longitude, speed, distance);
                    }
                    break;

                /*
                 * If we requested a track update we are resuming the screen. Pass the intent to restorePercorsoBarca
                 * and change buoy markers.
                 */
                case LocationUpdatesService.ACTION_SERVICE_GET_UPDATED_TRACK:
                    restorePercorsoBarca(intent);
                    for (int i = 1; i < boaMarkers.size() - 1; ++i)
                    {
                        Marker m = boaMarkers.get(i);
                        m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }
                    boaMarkers.get(currentBoa).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    break;

                /*
                 * If location providers are changing maybe someone disabled GPS, if yes (you are so mean to us) show a dialog
                 */
                case LocationManager.PROVIDERS_CHANGED_ACTION:
                {
                    if (!isShow)
                    {
                        isShow = true;
                        showGPSDialogIfNeeded();
                    }
                }
            }
        }
    };

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            locationRequest = mService.getLocationRequest();
            if (isConfigurationChanged)
            {
                mService.requestUpdate();
                isConfigurationChanged = false;
            }
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //create an api client, used to show "enable GPS" dialog
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

        //check again for permissions
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
        {
            Intent intent = getIntent();
            String trackJSON;
            trackJSON = intent.getStringExtra(FragmentList.INTENT_TRACK_OBJECT);
            track = Track.parseJSON(trackJSON);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // always check for permissions... you never know
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
        }
        else
        {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .registerOnSharedPreferenceChangeListener(this);

            startRaceButton = findViewById(R.id.start_race_button);
            stopRaceButton = findViewById(R.id.stop_race_button);

            startRaceButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    startRace();
                }
            });

            stopRaceButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    stopRace();
                }
            });

            // Restore the state of the buttons when the activity (re)launches.
            setButtonsState(Utils.requestingLocationUpdates(this));

            // Bind to the service. If the service is in foreground mode, this signals to the service
            // that since this activity is in the foreground, the service can exit foreground mode.
            bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop()
    {
        if (mBound)
        {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /*
     * Pretty obvious
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("first_load", firstLoaded);
        outState.putInt("current_boa", currentBoa);
        if (isChangingConfigurations())
        {
            outState.putBoolean("configuration_change", true);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (isRegistered)
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            isRegistered = false;
        }
        isPaused = true;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // check for permissions... once again
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        else
        {
            if (!isRegistered)
            {
                registerBroadcastReceiver();
                isRegistered = true;
            }
            if (isPaused)
            {
                //if the activity was paused, we stopped broadcast receive, we surely miss some ship path point
                // we don't want to lose them, do we?
                mService.requestUpdate();
                isPaused = false;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //if permission denied show an alert and punish the user closing the app
        if (grantResults.length <= 0
                || grantResults[0] != PackageManager.PERMISSION_GRANTED)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.important);

            final TextView testo = new TextView(this);
            testo.setText(R.string.permission_is_mandatory);
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            testo.setPadding(padding, 0, padding, 0);
            builder.setView(testo);
            builder.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    finishAffinity();
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    finishAffinity();
                }
            });
            builder.show();
        }
        else
        {
            //act accordingly to the request number
            switch (requestCode)
            {
                case 1:
                {
                    Intent intent = getIntent();
                    String trackJSON;
                    trackJSON = intent.getStringExtra(FragmentList.INTENT_TRACK_OBJECT);
                    track = Track.parseJSON(trackJSON);
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                    break;
                }
                case 2:
                {
                    if (!isRegistered)
                    {
                        registerBroadcastReceiver();

                        isRegistered = true;
                    }
                    if (isPaused || isChangingConfigurations())
                    {
                        mService.requestUpdate();
                        isPaused = false;
                    }
                    break;
                }
                case 3:
                {
                    PreferenceManager.getDefaultSharedPreferences(this)
                            .registerOnSharedPreferenceChangeListener(this);

                    startRaceButton = findViewById(R.id.start_race_button);
                    stopRaceButton = findViewById(R.id.stop_race_button);

                    startRaceButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            startRace();
                        }
                    });

                    stopRaceButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            mService.removeLocationUpdates();
                        }
                    });

                    // Restore the state of the buttons when the activity (re)launches.
                    setButtonsState(Utils.requestingLocationUpdates(this));
                    // Bind to the service. If the service is in foreground mode, this signals to the service
                    // that since this activity is in the foreground, the service can exit foreground mode.
                    bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                            Context.BIND_AUTO_CREATE);
//        mService.requestLocationUpdates();
                }
            }
        }
    }

    private void registerBroadcastReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationUpdatesService.ACTION_SERVICE_GET_NEW_POSITION);
        filter.addAction(LocationUpdatesService.ACTION_SERVICE_GET_UPDATED_TRACK);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        registerReceiver(receiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    /*
     * start a race, ask service for location updates.
     */
    private void startRace()
    {
        mService.startRace(track);
        mService.requestLocationUpdates();
        currentBoa = 1;
        boaMarkers.get(currentBoa).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        if (percorsoBarca != null)
        {
            percorsoBarca.clear();
        }
    }

    /*
     * Pretty obvious
     */
    private void stopRace()
    {
        mService.stopRace();
        mService.removeLocationUpdates();
        currentBoa = -1;

    }

    /*
     * set buttons state
     */
    private void setButtonsState(boolean requestingLocationUpdates)
    {
        if (requestingLocationUpdates)
        {
            startRaceButton.setEnabled(false);
            stopRaceButton.setEnabled(true);
        }
        else
        {
            startRaceButton.setEnabled(true);
            stopRaceButton.setEnabled(false);
        }
    }

    /*
     * when map is ready set up all the things. If we are coming from notification we need to restore ship path
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        Intent intent = getIntent();
        setBoas();
        //check if we come from notification, if it's true set track and restore boat path
        if (intent.getBooleanExtra(LocationUpdatesService.INTENT_STARTED_FROM_NOTIFICATION, false))
        {
            restorePercorsoBarca(intent);
        }
    }

    /*
     * We need to set our buoys, otherwise we don't know were to go
     */
    private void setBoas()
    {

        float radius = Preferences.load(this, "pref_key_circle_radius", 20);
        if (boaMarkers == null)
        {
            boaMarkers = new ArrayList<>();
        }
        else
        {
            boaMarkers.clear();
        }
        mMap.clear();
        LatLng latLng;

        // used to zoom map on the track
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        percorsoGara = new ArrayList<>();

        //add a polyline that will show us track path
        Polyline line = mMap.addPolyline(new PolylineOptions()
                .width(5)
                .color(Color.rgb(46, 125, 50)));

        //add buoys to the map, and show track path
        if (track.length() > 0)
        {
            Boa b = track.getBoa(0);
            latLng = new LatLng(b.getLatitude(), b.getLongitude());
            percorsoGara.add(latLng);
            MarkerOptions marker = new MarkerOptions().position(latLng).title(getString(R.string.activity_maps_start_position))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            boaMarkers.add(mMap.addMarker(marker));
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(b.getLatitude(), b.getLongitude()))
                    .radius(radius)
                    .strokeColor(Color.RED)
                    .strokeWidth(2)
                    .fillColor(Color.argb(80, 0, 0, 255)));
            builder.include(marker.getPosition());

            for (int i = 1; i < track.length() - 1; ++i)
            {
                b = track.getBoa(i);
                latLng = new LatLng(b.getLatitude(), b.getLongitude());
                percorsoGara.add(latLng);
                marker = new MarkerOptions().position(latLng).title(getString(R.string.activity_maps_buoy) + " " + i);
                boaMarkers.add(mMap.addMarker(marker));
                mMap.addCircle(new CircleOptions()
                        .center(new LatLng(b.getLatitude(), b.getLongitude()))
                        .radius(radius)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.argb(80, 0, 0, 255)));
                builder.include(marker.getPosition());
                builder.include(marker.getPosition());
            }

            if (track.length() > 1)
            {
                b = track.getBoa(track.length() - 1);
                latLng = new LatLng(b.getLatitude(), b.getLongitude());
                percorsoGara.add(latLng);
                marker = new MarkerOptions().position(latLng).title(getString(R.string.activity_maps_finish_position))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                ;
                boaMarkers.add(mMap.addMarker(marker));
                mMap.addCircle(new CircleOptions()
                        .center(new LatLng(b.getLatitude(), b.getLongitude()))
                        .radius(radius)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.argb(80, 0, 0, 255)));
                builder.include(marker.getPosition());
                builder.include(marker.getPosition());
            }
            if (firstLoaded)
            {
                int width = getSupportFragmentManager()
                        .findFragmentById(R.id.map).getView().getWidth();
                int height = getSupportFragmentManager()
                        .findFragmentById(R.id.map).getView().getHeight();
                if (width == 0 || height == 0)
                {
                    width = getResources().getDisplayMetrics().widthPixels;
                    height = getResources().getDisplayMetrics().heightPixels;
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    {
                        width /= 2;
                    }
                    else
                    {
                        height /= 2;
                    }
                }
                int padding = (int) ((width < height) ? height * 0.10 : width * 0.10);
                LatLngBounds bounds = builder.build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

                firstLoaded = false;
            }
            line.setPoints(percorsoGara);
            // if percorsoBarca contains some points, we are coming from a rotation change (likely). Show them on the map
            if (percorsoBarca != null && percorsoBarca.size() > 0)
            {
                putCurrentPositionMarker(percorsoBarca.get(percorsoBarca.size() - 1), 0);
                if (actualPath == null)
                {
                    actualPath = mMap.addPolyline(new PolylineOptions()
                            .width(5)
                            .color(Color.RED));
                }
                actualPath.setPoints(percorsoBarca);
            }
        }
    }

    /*
     * Called when we need to restore ship path
     */
    private void restorePercorsoBarca(Intent intent)
    {
        Parcelable[] locations = intent.getParcelableArrayExtra(LocationUpdatesService.INTENT_PERCORSO_BARCA);
        currentBoa = intent.getIntExtra(LocationUpdatesService.INTENT_BOA_NUMBER, -1);

        if (currentBoa > -1)
        {
            boaMarkers.get(currentBoa).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        }

        percorsoBarca = new ArrayList<>();

        if (actualPath == null)
        {
            actualPath = mMap.addPolyline(new PolylineOptions()
                    .width(5)
                    .color(Color.RED));
        }

        for (Parcelable location : locations)
        {
            percorsoBarca.add(new LatLng(((Location) location).getLatitude(), ((Location) location).getLongitude()));
        }

        actualPath.setPoints(percorsoBarca);
        LatLng pos = null;
        if (percorsoBarca.size() > 0)
        {
            pos = percorsoBarca.get(percorsoBarca.size() - 1);
        }

        putCurrentPositionMarker(pos, 0);

    }

    // pretty obvious
    private void putCurrentPositionMarker(LatLng pos, float bearing)
    {
        if (pos != null)
        {
            if (currentBoatposition == null)
            {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(getString(R.string.current_position));
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_boat_position));
                markerOptions.position(pos);
                currentBoatposition = mMap.addMarker(markerOptions);
            }
            else
            {
                currentBoatposition.setPosition(pos);
            }
            currentBoatposition.setRotation(bearing);
        }
    }

    // pretty obvious
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null && !savedInstanceState.isEmpty())
        {
            firstLoaded = savedInstanceState.getBoolean("first_load");
            currentBoa = savedInstanceState.getInt("current_boa");
            isConfigurationChanged = savedInstanceState.getBoolean("configuration_change", false);

        }
    }

    //listen for preference change to change button state
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s)
    {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES))
        {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    // used to set informations on screen
    private void setTextView(Double latitude, Double longitude, float speed, float distance)
    {
        DecimalFormat coordinateFormat = new DecimalFormat("0.######");
        DecimalFormat speedFormat = new DecimalFormat("0.##");
        DecimalFormat distanceFormat = new DecimalFormat("0.#");
        TextView textSpeed = findViewById(R.id.activity_maps_speed);
        textSpeed.setText(speedFormat.format(speed) + " m/s");
        TextView textSpeedkmh = findViewById(R.id.activity_maps_speed_kmh);
        textSpeedkmh.setText(speedFormat.format(speed * 3.6) + " km/h");
        TextView textLatitude = findViewById(R.id.activity_maps_latitude);
        textLatitude.setText(coordinateFormat.format(latitude));
        TextView textLongitude = findViewById(R.id.activity_maps_longitude);
        textLongitude.setText(coordinateFormat.format(longitude));
        TextView textDistance = findViewById(R.id.activity_maps_distance);
        textDistance.setText(distanceFormat.format(distance) + " m");
    }

    // these methods are used to show "enable GPS" dialog
    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        showGPSDialogIfNeeded();
    }

    private void showGPSDialogIfNeeded()
    {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        builder.build()
                );
        result.setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    //on result callback. actually shows the dialog if needed
    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult)
    {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode())
        {
            case LocationSettingsStatusCodes.SUCCESS:

                // NO need to show the dialog;
                isShow = false;
                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                //  Location settings are not satisfied. Show the user a dialog
                Log.e("MARONN", "IS SHOW");
                try
                {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);


                }
                catch (IntentSender.SendIntentException e)
                {
                    //failed to show
                }
                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are unavailable so not possible to show any dialog now
                break;
        }
    }

    //handle "enable GPS" dialog result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        isShow = false;
        Log.e("MARONN", "NOT SHOW");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS)
        {
            if (resultCode == RESULT_OK)
            {
                Toast.makeText(getApplicationContext(), R.string.gps_enabled, Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), R.string.gps_not_enabled, Toast.LENGTH_LONG).show();
                finish();
            }

        }
    }


}
