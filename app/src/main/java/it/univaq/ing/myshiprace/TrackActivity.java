package it.univaq.ing.myshiprace;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import it.univaq.ing.myshiprace.Util.ClickListener;
import it.univaq.ing.myshiprace.Util.RecyclerTouchListener;
import it.univaq.ing.myshiprace.adapter.BoaAdapter;
import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.Track;

public class TrackActivity extends AppCompatActivity
{
    private Track rt;
    RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        String trackJSON;

        if (savedInstanceState != null)
        {
            trackJSON = savedInstanceState.getString("track_object");
        }
        else
        {
            Intent intent = getIntent();
            trackJSON = intent.getStringExtra("track_object");
        }

        if (trackJSON != null)
        {
            rt = Track.parseJSON(trackJSON);
        }
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

            FloatingActionButton fab = findViewById(R.id.activity_new_track_fab);
            fab.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Boa b = new Boa();
                    Intent intent = new Intent(view.getContext(), BoaActivity.class);
                    intent.putExtra("boa_object", b.toJSONObject().toString());
                    view.getContext().startActivity(intent);
                }
            });
        }

    }

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
                list.getAdapter().notifyItemRemoved(position);
                list.getAdapter().notifyItemRangeChanged(0, rt.length());
                Snackbar.make(v, R.string.buoy_removed_text, Snackbar.LENGTH_LONG).setAction(R.string.undo_snackbar, new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {
                        Snackbar snackbar1 = Snackbar.make(view, R.string.undo_buoy_remove, Snackbar.LENGTH_LONG);
                        snackbar1.show();
                        rt.addBoa(temp);
                        list.getAdapter().notifyItemInserted(position);
                        list.getAdapter().notifyItemRangeChanged(0, rt.length());
                    }
                }).setActionTextColor(Color.RED).show();
            }
        });

        adb.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        adb.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString("track_object", rt.toJSONArray().toString());
    }
}
