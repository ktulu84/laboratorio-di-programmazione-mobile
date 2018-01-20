package it.univaq.ing.myshiprace;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import it.univaq.ing.myshiprace.Database.DBHelper;
import it.univaq.ing.myshiprace.Util.ClickListener;
import it.univaq.ing.myshiprace.Util.RecyclerTouchListener;
import it.univaq.ing.myshiprace.adapter.BoaAdapter;
import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.Track;

/*
 * This activity show us buoy list in a track.
 */
public class TrackActivity extends AppCompatActivity
{
    private Track rt;
    private RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        // take the relevant track data from json (intent or savedInstanceState)
        String trackJSON;

        if (savedInstanceState != null)
        {
            trackJSON = savedInstanceState.getString(FragmentList.INTENT_TRACK_OBJECT);
        }
        else
        {
            Intent intent = getIntent();
            trackJSON = intent.getStringExtra(FragmentList.INTENT_TRACK_OBJECT);
        }

        if (trackJSON != null)
        {
            rt = Track.parseJSON(trackJSON);
        }

        //show buoys contained in a track
        if (rt != null)
        {
            String trackName = rt.getTrackName();
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
            {
                actionBar.setTitle(trackName);
            }
            list = findViewById(R.id.boa_list);
            list.setLayoutManager(new LinearLayoutManager(this));
            list.setAdapter(new BoaAdapter(rt.getBoas()));

            //on long press prompt for buoy deletion
            list.addOnItemTouchListener(new RecyclerTouchListener(this,
                    list, new ClickListener()
            {
                @Override
                public void onClick(View view, final int position)
                {

                }

                @Override
                public void onLongClick(View view, int position)
                {
                    deleteBoa(view, position);
                }
            }));

            //this button let us add a buoy on the track (only if the track is loaded, if we got here without a valid track it would be a mess)
            FloatingActionButton fabNewBoa = findViewById(R.id.activity_new_track_fab);
            fabNewBoa.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    showDialog();
                }
            });

            // floating action button to "play" the track
            FloatingActionButton fabPlay = findViewById(R.id.activity_play_track_fab);
            fabPlay.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    // if he want to play with us we need location permissions
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(TrackActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                    else
                    {
                        Intent intent = new Intent(view.getContext(), MapsActivity.class);
                        intent.putExtra(FragmentList.INTENT_TRACK_OBJECT, rt.toJSONArray().toString());
                        view.getContext().startActivity(intent);
                    }
                }
            });

            //if track don't contain a buoy, we don't want to play it, do we?
            if (rt.length() <= 1)
            {
                fabPlay.setVisibility(View.INVISIBLE);
            }

        }

    }

    // buoy deletion, as for track show a snackbar for restore it. if this is the last buoy hide
    // floating action butto to play the track
    private void deleteBoa(final View v, final int position)
    {
        AlertDialog.Builder adb = new AlertDialog.Builder(v.getContext());
        adb.setTitle(R.string.alert_boa_remove);
        adb.setIcon(R.drawable.ic_warning);

        adb.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                final Boa temp = rt.getBoa(position);
                rt.removeBoa(position);
                DBHelper.get(v.getContext()).delete(temp);
                list.getAdapter().notifyItemRemoved(position);
                list.getAdapter().notifyItemRangeChanged(0, rt.length());
                final FloatingActionButton fab = findViewById(R.id.activity_play_track_fab);
                if (rt.length() <= 1)
                    fab.setVisibility(View.INVISIBLE);

                Snackbar.make(v, R.string.buoy_removed_text, Snackbar.LENGTH_LONG).setAction(R.string.undo_snackbar, new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {
                        Snackbar snackbar1 = Snackbar.make(view, R.string.undo_buoy_remove, Snackbar.LENGTH_LONG);
                        snackbar1.show();
                        rt.addBoa(temp);
                        DBHelper.get(v.getContext()).save(temp);
                        list.getAdapter().notifyItemInserted(position);
                        list.getAdapter().notifyItemRangeChanged(0, rt.length());
                        if (rt.length() > 1)
                            fab.setVisibility(View.VISIBLE);
                    }
                }).setActionTextColor(Color.RED).show();
            }
        });

        adb.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        adb.show();
    }

    //prompt for add a new buoy
    private void showDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.boa_info_alert_title);
        final View layout = getLayoutInflater().inflate(R.layout.alert_new_boa, null);
        final Context context = this;
        builder.setView(layout);

        builder.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                EditText latitudeText = layout.findViewById(R.id.alert_new_boa_latitude);
                EditText longitudeText = layout.findViewById(R.id.alert_new_boa_longitude);
                EditText orderText = layout.findViewById(R.id.alert_new_boa_order);
                if (!orderText.getText().toString().isEmpty() && !latitudeText.getText().toString().isEmpty() && !longitudeText.getText().toString().isEmpty())
                {
                    double latitude = Double.valueOf(latitudeText.getText().toString());
                    double longitude = Double.valueOf(longitudeText.getText().toString());
                    int order = Integer.valueOf(orderText.getText().toString());
                    Boa b = new Boa(latitude, longitude);
                    b.setTrackID(rt.getId());
                    b.setOrder(order);
                    rt.addBoa(b);
                    DBHelper.get(context).saveOrUpdate(rt);
                    list.getAdapter().notifyDataSetChanged();
                    FloatingActionButton fab = findViewById(R.id.activity_play_track_fab);
                    if (rt.length() > 1)
                        fab.setVisibility(View.VISIBLE);
                }
            }
        });

        builder.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(FragmentList.INTENT_TRACK_OBJECT, rt.toJSONArray().toString());
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //as ever, if the user deny permission we don't let him play with us because he is mean to us
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
            switch (requestCode)
            {
                case 1:
                {
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }
}
