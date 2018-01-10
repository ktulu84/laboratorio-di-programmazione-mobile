package it.univaq.ing.myshiprace;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import it.univaq.ing.myshiprace.Util.ClickListener;
import it.univaq.ing.myshiprace.Util.RecyclerTouchListener;
import it.univaq.ing.myshiprace.adapter.BoaAdapter;
import it.univaq.ing.myshiprace.model.RaceTrack;

public class TrackActivity extends AppCompatActivity
{
    private RaceTrack rt;
    RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_track);
        String trackJSON = intent.getStringExtra("track_object");
        rt = RaceTrack.parseJSON(trackJSON);
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
                //TODO
                Toast.makeText(view.getContext(), "Boa size: " + rt.length(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void deleteBoa(View v, final int position)
    {
        AlertDialog.Builder adb = new AlertDialog.Builder(v.getContext());
        adb.setTitle(R.string.alert_boa_remove);
        adb.setIcon(R.drawable.ic_warning);

        final Context context = v.getContext();
        adb.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                rt.removeBoa(position);
                Toast.makeText(context, "Cliccato OK", Toast.LENGTH_SHORT).show();
                list.getAdapter().notifyItemRemoved(position);
                list.getAdapter().notifyItemRangeChanged(0, rt.length());
                ;
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
}
