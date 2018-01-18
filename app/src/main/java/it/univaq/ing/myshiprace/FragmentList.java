package it.univaq.ing.myshiprace;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import it.univaq.ing.myshiprace.Database.DBHelper;
import it.univaq.ing.myshiprace.Util.ClickListener;
import it.univaq.ing.myshiprace.Util.Preferences;
import it.univaq.ing.myshiprace.Util.RecyclerTouchListener;
import it.univaq.ing.myshiprace.Util.Request;
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
    public static final String INTENT_TRACK_OBJECT = "intent_track_object";
    private Context context;
    private List<Track> tracks;
    private RecyclerView list;

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent == null || intent.getAction() == null) return;

            switch (intent.getAction())
            {
                case ACTION_SERVICE_DB_GET_ALL_TRACKS:

                    String data = intent.getStringExtra("data");
                    if (data != null)
                    {
                        tracks = Track.fromJSONArray(data);
                        TrackAdapter adapter = new TrackAdapter(tracks);
                        list.setAdapter(adapter);
                    }
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
//        new MyTask().execute();

        if (Preferences.load(container.getContext(), "first_time", true))
        {
            Preferences.save(container.getContext(), "first_time", false);
            AlertDialog.Builder builder = new AlertDialog.Builder(container.getContext());
            builder.setTitle(R.string.important);

            final TextView testo = new TextView(container.getContext());
            testo.setText(R.string.set_a_ship_name);
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            testo.setPadding(padding, 0, padding, 0);
            builder.setView(testo);
            builder.setPositiveButton(R.string.alert_ok, null);
            builder.show();
        }

        View view = inflater.inflate(R.layout.activity_lista, container, false);
        context = view.getContext();
        FloatingActionButton fabAdd = view.findViewById(R.id.activity_lista_fab);
        fabAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showDialog();
            }
        });

        FloatingActionButton fabDownload = view.findViewById(R.id.activity_download_track_fab);
        fabDownload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showDownload();
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
                intent.putExtra(INTENT_TRACK_OBJECT, rt.toJSONArray().toString());
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

    @Override
    public void onResume()
    {
        super.onResume();

        IntentFilter filter = new IntentFilter(ACTION_SERVICE_DB_GET_ALL_TRACKS);
//        filter.addAction(ACTION_SERVICE_DB_GET_ALL_TRACKS);
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
                Track rt = new Track(input.getText().toString());
                DBHelper.get(context).saveOrUpdate(rt);
                tracks.add(rt);
                intent.putExtra(INTENT_TRACK_OBJECT, rt.toJSONArray().toString());
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

    private void showDownload()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Server da cui scaricare il tracciato");

        final EditText input = new EditText(context);
        input.setText(Preferences.load(context, "pref_key_download_address", "http://ktulu.altervista.org/track"));
        builder.setView(input);

        builder.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(context, TrackActivity.class);
                MyTask task = new MyTask();
                task.execute(input.getText().toString());


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

    private class MyTask extends AsyncTask<String, Void, Track>
    {
        @Override
        protected Track doInBackground(String... strings)
        {
            String req = "";
            req = Request.doRequest(strings[0], null, null);
            if (req != null && !req.isEmpty())
            {
                Track rt = Track.parseJSON(req);

                Log.d("risultato", rt.toJSONArray().toString());
                return rt;
            }
            else
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Track track)
        {
            if (track != null)
            {
                super.onPostExecute(track);
                DBHelper.get(context).saveOrUpdate(track);
                tracks.add(track);
                Intent intent = new Intent(context, TrackActivity.class);
                intent.putExtra(INTENT_TRACK_OBJECT, track.toJSONArray().toString());
                context.startActivity(intent);
            }
            else
            {
                AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
                adb.setTitle("non ho trovato il tracciato");
                adb.setIcon(R.drawable.ic_warning);
                adb.setPositiveButton(R.string.alert_ok, null);
                adb.show();
            }
        }
    }
}
