package it.univaq.ing.myshiprace;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.Track;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback
{

    GoogleMap mMap;
    List<LatLng> percorsoGara;
    List<LatLng> percorsoBarca;
    boolean firstLoaded = true;
    private LocationManager locationManager;
    private String locationProvider;
    private LatLng coordinate;
    private Marker currentBoatposition;
    private Polyline actualPath;

    LocationListener locationListener = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            // Called when a new location is found by the network location provider.
            coordinate = new LatLng(location.getLatitude(), location.getLongitude());
            Log.i("POSIZIONE", "Sto provando a prendere la posizione");
            if (percorsoBarca == null)
            {
                percorsoBarca = new ArrayList<>();

//                mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 16));
//                Log.i("POSIZIONE", "Aggiunto marker nella posizione di partenza");
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(coordinate);
                markerOptions.title("prova");
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_boat_position));
                currentBoatposition = mMap.addMarker(markerOptions);
                actualPath = mMap.addPolyline(new PolylineOptions()
                        .width(5)
                        .color(Color.RED));
            }
            percorsoBarca.add(coordinate);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate));
            if (currentBoatposition != null)
            {
                currentBoatposition.setPosition(coordinate);
            }
            actualPath.setPoints(percorsoBarca);
        }

        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        public void onProviderEnabled(String provider)
        {

        }

        public void onProviderDisabled(String provider)
        {

        }

    };

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
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
        {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            locationProvider = locationManager.getBestProvider(criteria, true);
//        locationProvider = LocationManager.GPS_PROVIDER;
            locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
            Intent intent = getIntent();
            Track t;
            String trackJSON;
            trackJSON = intent.getStringExtra("track_object");
            t = Track.parseJSON(trackJSON);
            // Add a marker in Sydney and move the camera
            LatLng latLng = null;
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            percorsoGara = new ArrayList<>();
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .width(5)
                    .color(Color.rgb(46, 125, 50)));
//the include method will calculate the min and max bound.
            if (t.length() > 0)
            {
                Boa b = t.getBoa(0);
                latLng = new LatLng(b.getLatitude(), b.getLongitude());
                percorsoGara.add(latLng);
                MarkerOptions marker = new MarkerOptions().position(latLng).title(getString(R.string.activity_maps_start_position))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                ;
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
                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    locationProvider = locationManager.getBestProvider(criteria, true);
//        locationProvider = LocationManager.GPS_PROVIDER;
                    try
                    {
                        locationManager.requestLocationUpdates(locationProvider, 2000, 4, locationListener);

                    }
                    catch (SecurityException e)
                    {
                        // avendo appena dato il permesso mi sembra stupido gestire un'eccezione che non si verificherà mai
                    }
//                    permesso = true;

                }
                else
                {
//                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                return;
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
