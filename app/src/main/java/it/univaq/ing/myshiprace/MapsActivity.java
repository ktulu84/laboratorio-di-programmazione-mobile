package it.univaq.ing.myshiprace;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
    List<LatLng> punti;

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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        Intent intent = getIntent();
        Track t;
        String trackJSON;
        trackJSON = intent.getStringExtra("track_object");
        t = Track.parseJSON(trackJSON);
        // Add a marker in Sydney and move the camera
        LatLng latLng = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        punti = new ArrayList<>();
        Polyline line = mMap.addPolyline(new PolylineOptions()
                .width(5)
                .color(Color.rgb(46, 125, 50)));
//the include method will calculate the min and max bound.
        if (t.length() > 0)
        {
            Boa b = t.getBoa(0);
            latLng = new LatLng(b.getLatitude(), b.getLongitude());
            punti.add(latLng);
            MarkerOptions marker = new MarkerOptions().position(latLng).title(getString(R.string.activity_maps_start_position))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            ;
            mMap.addMarker(marker);
            builder.include(marker.getPosition());

            for (int i = 1; i < t.length() - 1; ++i)
            {
                b = t.getBoa(i);
                latLng = new LatLng(b.getLatitude(), b.getLongitude());
                punti.add(latLng);
                marker = new MarkerOptions().position(latLng).title(getString(R.string.activity_maps_buoy) + " " + i);
                mMap.addMarker(marker);
                builder.include(marker.getPosition());
            }

            if (t.length() > 1)
            {
                b = t.getBoa(t.length() - 1);
                latLng = new LatLng(b.getLatitude(), b.getLongitude());
                punti.add(latLng);
                marker = new MarkerOptions().position(latLng).title(getString(R.string.activity_maps_finish_position))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                ;
                mMap.addMarker(marker);
                builder.include(marker.getPosition());
            }
            int width = getResources().getDisplayMetrics().widthPixels;
            int padding = (int) (width * 0.10);
            LatLngBounds bounds = builder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            line.setPoints(punti);
        }
    }
}
