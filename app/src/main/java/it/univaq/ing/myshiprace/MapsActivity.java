package it.univaq.ing.myshiprace;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.Database.DBHelper;
import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.Race;
import it.univaq.ing.myshiprace.model.Track;
import it.univaq.ing.myshiprace.service.LocationUpdatesService;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback
{
    public static final String ACTION_SERVICE_GET_POSITION = "action_service_get_position";
    GoogleMap mMap;
    List<LatLng> percorsoGara;
    List<LatLng> percorsoBarca;
    boolean firstLoaded = true;
    private Marker currentBoatposition;
    private Polyline actualPath;
    private boolean isRegistered = false;
    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            startRace();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mService = null;
            mBound = false;
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent == null || intent.getAction() == null) return;

            switch (intent.getAction())
            {
                case ACTION_SERVICE_GET_POSITION:
                    Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
                    Double longitude = location.getLongitude();
                    Double latitude = location.getLatitude();
                    float speed = intent.getFloatExtra("speed", Float.NaN);
                    Float bearing = intent.getFloatExtra("bearing", Float.NaN);

                    if (!longitude.equals(Double.NaN) && !latitude.equals(Double.NaN))
                    {
                        LatLng coordinate = new LatLng(latitude, longitude);
                        if (percorsoBarca == null)
                        {
                            percorsoBarca = new ArrayList<>();

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 16));
                            actualPath = mMap.addPolyline(new PolylineOptions()
                                    .width(5)
                                    .color(Color.RED));
                        }
                        percorsoBarca.add(coordinate);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate));
                        if (currentBoatposition != null)
                        {
                            currentBoatposition.setPosition(coordinate);
                            currentBoatposition.setRotation(bearing);
                        }
                        else
                        {
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(coordinate);
                            markerOptions.title("prova");
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_boat_position));
                            currentBoatposition = mMap.addMarker(markerOptions);
                        }
                        actualPath.setPoints(percorsoBarca);
                        DecimalFormat coordinateFormat = new DecimalFormat("0.######");
                        DecimalFormat speedFormat = new DecimalFormat("0.##");
                        TextView textSpeed = findViewById(R.id.activity_maps_speed);
                        textSpeed.setText(speedFormat.format(speed) + " m/s (" + speedFormat.format(speed * 3.6) + " km/h)");
                        TextView textLatitude = findViewById(R.id.activity_maps_latitude);
                        textLatitude.setText(coordinateFormat.format(latitude));
                        TextView textLongitude = findViewById(R.id.activity_maps_longitude);
                        textLongitude.setText(coordinateFormat.format(longitude));
                    }
                    break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        if (!isRegistered)
        {
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                    new IntentFilter(ACTION_SERVICE_GET_POSITION));
            isRegistered = true;
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
        {
            setBoas();

        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(LocationUpdatesService.EXTRA_STARTED_FROM_NOTIFICATION, false))
        {
            Parcelable[] locations = intent.getParcelableArrayExtra("percorso_barca");

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

        }
    }

    private void setBoas()
    {
        Intent intent = getIntent();
        Track t;
        String trackJSON;
        trackJSON = intent.getStringExtra("track_object");
        t = Track.parseJSON(trackJSON);
        LatLng latLng = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        percorsoGara = new ArrayList<>();
        Polyline line = mMap.addPolyline(new PolylineOptions()
                .width(5)
                .color(Color.rgb(46, 125, 50)));
        if (t.length() > 0)
        {
            Boa b = t.getBoa(0);
            latLng = new LatLng(b.getLatitude(), b.getLongitude());
            percorsoGara.add(latLng);
            MarkerOptions marker = new MarkerOptions().position(latLng).title(getString(R.string.activity_maps_start_position))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(marker);
            builder.include(marker.getPosition());

            for (int i = 1; i < t.length() - 1; ++i)
            {
                b = t.getBoa(i);
                latLng = new LatLng(b.getLatitude(), b.getLongitude());
                percorsoGara.add(latLng);
                marker = new MarkerOptions().position(latLng).title(getString(R.string.activity_maps_buoy) + " " + i);
                mMap.addMarker(marker);
                builder.include(marker.getPosition());
            }

            if (t.length() > 1)
            {
                b = t.getBoa(t.length() - 1);
                latLng = new LatLng(b.getLatitude(), b.getLongitude());
                percorsoGara.add(latLng);
                marker = new MarkerOptions().position(latLng).title(getString(R.string.activity_maps_finish_position))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                ;
                mMap.addMarker(marker);
                builder.include(marker.getPosition());
            }
            if (firstLoaded)
            {
                int width = getSupportFragmentManager()
                        .findFragmentById(R.id.map).getView().getWidth();
                int height = getSupportFragmentManager()
                        .findFragmentById(R.id.map).getView().getHeight();
                int padding = (int) ((width < height) ? height * 0.10 : width * 0.10);
                LatLngBounds bounds = builder.build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

                firstLoaded = false;
            }
            line.setPoints(percorsoGara);
//            if (!MyLocationService.isRunning())
//            {
            if (!isRegistered)
            {
                LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                        new IntentFilter(ACTION_SERVICE_GET_POSITION));
                isRegistered = true;
            }
//                Intent newIntent = new Intent(this, MyLocationService.class);
//                newIntent.setAction(MyLocationService.ACTION_GET_POSITION);
//                startService(newIntent);
//            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 1:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    setBoas();
                }
                break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("first_load", firstLoaded);
        if (percorsoBarca != null)
        {
            double[] latitudes = new double[percorsoBarca.size()];
            double[] longitudes = new double[percorsoBarca.size()];

            for (int i = 0; i < percorsoBarca.size(); ++i)
            {
                LatLng l = percorsoBarca.get(i);
                latitudes[i] = l.latitude;
                longitudes[i] = l.longitude;
            }
            outState.putDoubleArray("latitudes", latitudes);
            outState.putDoubleArray("longitudes", longitudes);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        firstLoaded = savedInstanceState.getBoolean("first_load");

        if (savedInstanceState != null && !savedInstanceState.isEmpty())
        {
            double[] latitudes = savedInstanceState.getDoubleArray("latitudes");
            if (latitudes != null)
            {
                double[] longitudes = savedInstanceState.getDoubleArray("longitudes");

                percorsoBarca = new ArrayList<>();
                for (int i = 0; i < latitudes.length; ++i)
                {
                    LatLng c = new LatLng(latitudes[i], longitudes[i]);
                    percorsoBarca.add(c);
                }
            }

        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
//        mService.requestLocationUpdates();

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

        super.onStop();
    }

    private void startRace()
    {
        mService.requestLocationUpdates();
        Race r = new Race();

        Intent intent = getIntent();
        Track t;
        String trackJSON;
        trackJSON = intent.getStringExtra("track_object");
        t = Track.parseJSON(trackJSON);
        r.setTrackID(t.getId());
        r.setStartTime(new Timestamp(System.currentTimeMillis()));
        LocationUpdatesService.race = r;
        DBHelper.get(this).save(r);
    }
}
