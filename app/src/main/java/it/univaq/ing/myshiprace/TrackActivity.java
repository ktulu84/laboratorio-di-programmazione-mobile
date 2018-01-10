package it.univaq.ing.myshiprace;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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

        FloatingActionButton fab = findViewById(R.id.activity_new_track_fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //TODO
            }
        });


    }
}
