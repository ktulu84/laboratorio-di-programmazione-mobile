package it.univaq.ing.myshiprace;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.adapter.TrackAdapter;
import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.RaceTrack;

/**
 * Created by ktulu on 15/12/17.
 */

public class FragmentList extends Fragment
{
    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.activity_lista, container, false);
        context = view.getContext();
        FloatingActionButton fab = view.findViewById(R.id.activity_lista_fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showDialog();
            }
        });

        List<RaceTrack> raceTracks = new ArrayList<>();
        RecyclerView list = view.findViewById(R.id.track_list);
        list.setLayoutManager(new LinearLayoutManager(view.getContext()));

        //TODO add real data
        for (int i = 0; i < 12; ++i)
        {
            RaceTrack rt = new RaceTrack("Prova " + i);
            Boa b = new Boa(12.1, 12.4, i + 1);
            b.setId(i);
            rt.addBoa(b);
            raceTracks.add(rt);
        }

        list.setAdapter(new TrackAdapter(raceTracks));

        return view;
    }

    private void showDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.track_name_inputbox_title);

        final EditText input = new EditText(context);

        builder.setView(input);

        builder.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(context, TrackActivity.class);
                RaceTrack rt = new RaceTrack(input.getText().toString());
                intent.putExtra("track_object", rt.toJSONArray().toString());
                context.startActivity(intent);
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
}
