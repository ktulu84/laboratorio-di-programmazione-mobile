package it.univaq.ing.myshiprace;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.Track;
import it.univaq.ing.myshiprace.service.MyLocationService;

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

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent == null || intent.getAction() == null) return;

            switch (intent.getAction())
            {
                case ACTION_SERVICE_GET_POSITION:

                    Double longitude = intent.getDoubleExtra("longitude", Double.NaN);
                    Double latitude = intent.getDoubleExtra("latitude", Double.NaN);
                    Double speed = intent.getDoubleExtra("speed", Double.NaN);
                    Float bearing = intent.getFloatExtra("bearing", Float.NaN);
                    if (bearing < 0)
                        bearing += 180;
                    bearing -= 90;
                    if (!longitude.equals(Double.NaN) && !latitude.equals(Double.NaN))
                    {
                        LatLng coordinate = new LatLng(latitude, longitude);
                        if (percorsoBarca == null)
                        {
                            percorsoBarca = new ArrayList<>();

//                mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 16));
//                Log.i("POSIZIONE", "Aggiunto marker nella posizione di partenza");
//                            MarkerOptions markerOptions = new MarkerOptions();
//                            markerOptions.position(coordinate);
//                            markerOptions.title("prova");
//                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_boat_position));
//                            currentBoatposition = mMap.addMarker(markerOptions);
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
                        DecimalFormat decimalFormat = new DecimalFormat("0.######");
                        TextView textSpeed = findViewById(R.id.activity_maps_speed);
                        textSpeed.setText(decimalFormat.format(speed));
                        TextView textLatitude = findViewById(R.id.activity_maps_latitude);
                        textLatitude.setText(decimalFormat.format(latitude));
                        TextView textLongitude = findViewById(R.id.activity_maps_longitude);
                        textLongitude.setText(decimalFormat.format(longitude));
                    }
                    break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter(ACTION_SERVICE_GET_POSITION);
        Context c = this;
        if (!isRegistered)
        {
            LocalBroadcastManager.getInstance(c).registerReceiver(receiver, filter);
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
            if (!MyLocationService.isRunning())
            {
                IntentFilter filter = new IntentFilter(ACTION_SERVICE_GET_POSITION);
                Context c = this;
                if (!isRegistered)
                {
                    LocalBroadcastManager.getInstance(c).registerReceiver(receiver, filter);
                    isRegistered = true;
                }
                Intent newIntent = new Intent(c, MyLocationService.class);
                newIntent.setAction(MyLocationService.ACTION_GET_POSITION);
                startService(newIntent);
            }

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
}
