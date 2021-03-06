package it.univaq.ing.myshiprace;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import it.univaq.ing.myshiprace.Util.Utils;


/*
 * Just the main activity, it contains navigation drawer and can load fragments
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //reset button status on MapsActivity
        Utils.setRequestingLocationUpdates(this, false);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //get default fragment
        if (savedInstanceState == null)
        {
            getFragmentManager().beginTransaction().add(R.id.main_container, new FragmentList()).commit();
            navigationView.setCheckedItem(R.id.nav_lista);
        }

        //check for permissions
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onBackPressed()
    {
        //if the drawer is opened on back press we close it, otherwise... we consume onBackPressed() event
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //if permission is not granted we show an alert and close the application
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
                break;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.nav_lista:
                getFragmentManager().beginTransaction().replace(R.id.main_container, new FragmentList()).commit();
                break;
            case R.id.nav_info:
                getFragmentManager().beginTransaction().replace(R.id.main_container, new FragmentInfo()).commit();
                break;
            case R.id.nav_settings:
                getFragmentManager().beginTransaction().replace(R.id.main_container, new SettingsFragment()).commit();
                break;
        }
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Runnable mPendingRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                drawer.closeDrawer(GravityCompat.START);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(mPendingRunnable, 5);
        return true;
    }
}
