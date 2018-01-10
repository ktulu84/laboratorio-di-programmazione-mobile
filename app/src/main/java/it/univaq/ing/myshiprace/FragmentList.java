package it.univaq.ing.myshiprace;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.Util.ClickListener;
import it.univaq.ing.myshiprace.Util.RecyclerTouchListener;
import it.univaq.ing.myshiprace.adapter.TrackAdapter;
import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.RaceTrack;

/**
 * Created by ktulu on 15/12/17.
 */

public class FragmentList extends Fragment
{
    private Context context;
    private List<RaceTrack> raceTracks;
    private RecyclerView list;

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

        raceTracks = new ArrayList<>();
        list = view.findViewById(R.id.track_list);
        list.setLayoutManager(new LinearLayoutManager(view.getContext()));

        if (savedInstanceState == null)
        {
            //TODO add real data
            for (int i = 0; i < 12; ++i)
            {
                RaceTrack rt = new RaceTrack("Prova " + i);
                Boa b = new Boa(12.1, 12.4, 1);
                b.setId(i);
                rt.addBoa(b);
                Boa b1 = new Boa(54.1, 74.56, 2);
                rt.addBoa(b1);
                raceTracks.add(rt);
            }
        }
        else
        {
            int i = 0;
            String track = savedInstanceState.getString("track " + i);
            while (track != null)
            {
                raceTracks.add(RaceTrack.parseJSON(track));
                ++i;
                track = savedInstanceState.getString("track " + i);
            }
        }


        list.addOnItemTouchListener(new RecyclerTouchListener(context,
                list, new ClickListener()
        {
            @Override
            public void onClick(View view, final int position)
            {
                Intent intent = new Intent(view.getContext(), TrackActivity.class);
                RaceTrack rt = raceTracks.get(position);
                intent.putExtra("track_object", rt.toJSONArray().toString());
                view.getContext().startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position)
            {
                deleteTrack(view, position);
            }
        }));
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

    private void deleteTrack(final View v, final int position)
    {
        AlertDialog.Builder adb = new AlertDialog.Builder(v.getContext());
        adb.setTitle(R.string.alert_track_remove);
        adb.setIcon(R.drawable.ic_warning);

        adb.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                final RaceTrack temp = raceTracks.get(position);
                raceTracks.remove(position);
                list.getAdapter().notifyItemRemoved(position);
                list.getAdapter().notifyItemRangeChanged(0, raceTracks.size());
                Snackbar.make(v, R.string.track_removed_text, Snackbar.LENGTH_LONG).setAction(R.string.undo_snackbar, new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {
                        Snackbar snackbar1 = Snackbar.make(view, R.string.undo_track_delete, Snackbar.LENGTH_LONG);
                        snackbar1.show();
                        raceTracks.add(position, temp);
                        list.getAdapter().notifyItemInserted(position);
                        list.getAdapter().notifyItemRangeChanged(0, raceTracks.size());
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
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        for (int i = 0; i < raceTracks.size(); ++i)
        {
            outState.putString("track " + i, raceTracks.get(i).toJSONArray().toString());
        }
    }
}
