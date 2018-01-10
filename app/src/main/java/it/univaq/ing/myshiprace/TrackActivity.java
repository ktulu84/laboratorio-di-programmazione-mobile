package it.univaq.ing.myshiprace;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class TrackActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_track);
        TextView textTrackName = findViewById(R.id.activity_new_track_placeholder);
        String trackName = intent.getStringExtra("track_name");
        textTrackName.setText(trackName);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setTitle(trackName);
        }

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
