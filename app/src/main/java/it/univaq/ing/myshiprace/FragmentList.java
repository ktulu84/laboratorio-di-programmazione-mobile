package it.univaq.ing.myshiprace;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.List;

import it.univaq.ing.myshiprace.Database.DBHelper;
import it.univaq.ing.myshiprace.Util.ClickListener;
import it.univaq.ing.myshiprace.Util.RecyclerTouchListener;
import it.univaq.ing.myshiprace.adapter.TrackAdapter;
import it.univaq.ing.myshiprace.model.Track;
import it.univaq.ing.myshiprace.service.MyService;

/**
 * Created by ktulu on 15/12/17.
 */

public class FragmentList extends Fragment
{
    //    public static final String ACTION_SERVICE_COMPLETED = "action_service_completed";
    public static final String ACTION_SERVICE_DB_GET_ALL_TRACKS = "action_service_db_get_all_tracks";
    public static final String ACTION_SERVICE_DB_SAVE_UPDATE_TRACK = "action_service_db_save_update_track";
    private Context context;
    private List<Track> tracks;
    private RecyclerView list;

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent == null || intent.getAction() == null) return;
            String data;
            switch (intent.getAction())
            {
                case ACTION_SERVICE_DB_GET_ALL_TRACKS:

                    data = intent.getStringExtra("data");
                    if (data != null)
                    {
                        tracks = Track.fromJSONArray(data);
                        TrackAdapter adapter = new TrackAdapter(tracks);
                        list.setAdapter(adapter);
                    }
                    break;
                case ACTION_SERVICE_DB_SAVE_UPDATE_TRACK:
                    data = intent.getStringExtra("data");
                    if (data != null)
                    {
//                        Track track = Track.parseJSON(data);
                        Intent newIntent = new Intent(context, TrackActivity.class);
                        newIntent.putExtra("track_object", data);
                        context.startActivity(newIntent);
                    }
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();

        IntentFilter filter = new IntentFilter(ACTION_SERVICE_DB_GET_ALL_TRACKS);
        filter.addAction(ACTION_SERVICE_DB_SAVE_UPDATE_TRACK);
        Context c = getActivity().getApplicationContext();
        LocalBroadcastManager.getInstance(c).registerReceiver(receiver, filter);

        Intent newIntent = new Intent(c, MyService.class);
        newIntent.setAction(MyService.ACTION_GETALL_TRACKS);
        getActivity().startService(newIntent);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(receiver);
    }

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

        list = view.findViewById(R.id.track_list);
        list.setLayoutManager(new LinearLayoutManager(view.getContext()));
        list.setAdapter(new TrackAdapter());

        list.addOnItemTouchListener(new RecyclerTouchListener(context,
                list, new ClickListener()
        {
            @Override
            public void onClick(View view, final int position)
            {
                Intent intent = new Intent(view.getContext(), TrackActivity.class);
                Track rt = tracks.get(position);
                intent.putExtra("track_object", rt.toJSONArray().toString());
                view.getContext().startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position)
            {
                deleteTrack(view, position);
            }
        }));

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
//                Intent intent = new Intent(context, TrackActivity.class);
                Track rt = new Track(input.getText().toString());
//                DBHelper.get(context).saveOrUpdate(rt);
                tracks.add(rt);
                Intent newIntent = new Intent(context, MyService.class);
                newIntent.setAction(MyService.ACTION_SAVE_UPDATE);
                newIntent.putExtra("track_object", rt.toJSONArray().toString());
                newIntent.putExtra(MyService.TYPE, MyService.TYPE_TRACK);
                getActivity().startService(newIntent);
//                intent.putExtra("track_object", rt.toJSONArray().toString());
//                context.startActivity(intent);
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
                final Track temp = tracks.get(position);
                tracks.remove(position);
                DBHelper.get(v.getContext()).delete(temp);

                list.getAdapter().notifyItemRemoved(position);
                list.getAdapter().notifyItemRangeChanged(0, tracks.size());
                Snackbar.make(v, R.string.track_removed_text, Snackbar.LENGTH_LONG).setAction(R.string.undo_snackbar, new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {
                        Snackbar snackbar1 = Snackbar.make(view, R.string.undo_track_delete, Snackbar.LENGTH_LONG);
                        snackbar1.show();
                        tracks.add(position, temp);
                        DBHelper.get(v.getContext()).save(temp);
                        list.getAdapter().notifyItemInserted(position);
                        list.getAdapter().notifyItemRangeChanged(0, tracks.size());
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
}
