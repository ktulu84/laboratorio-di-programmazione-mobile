package it.univaq.ing.myshiprace.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.List;

import it.univaq.ing.myshiprace.Database.DBHelper;
import it.univaq.ing.myshiprace.FragmentList;
import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.ShipPosition;
import it.univaq.ing.myshiprace.model.Track;

/**
 * MyService
 * Created by leonardo on 10/11/17.
 * <p>
 * BiTE s.r.l.
 * contact info@bitesrl.it
 */

public class MyService extends IntentService
{

    public static final String ACTION_SAVE = "action_save";
    public static final String ACTION_GETALL_TRACKS = "action_get_all_tracks";
    public static final String ACTION_SAVE_UPDATE = "action_save_update";
    public static final String ACTION_DELETE = "action_delete";
    public static final String TYPE_BOA = "boa";
    public static final String TYPE_TRACK = "track";
    public static final String TYPE_SHIP = "ship";
    public static final String TYPE = "type";


    private static final String NAME = MyService.class.getSimpleName();

    public MyService()
    {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        if (intent != null && intent.getAction() != null)
        {
            String action = intent.getAction();
            String type;
            switch (action)
            {
                //TODO
                case ACTION_SAVE:
                    type = intent.getStringExtra(TYPE);
                    switch (type)
                    {
                        case TYPE_BOA:
                            saveInDB(new Boa());
                            break;
                        case TYPE_TRACK:
                            saveInDB(new Track());
                            break;
                        case TYPE_SHIP:
                            saveInDB(new ShipPosition());
                    }
                    break;
                case ACTION_GETALL_TRACKS:
                    getTracksFromDB();
                    break;
                //TODO
                case ACTION_SAVE_UPDATE:
                    type = intent.getStringExtra(TYPE);
                    switch (type)
                    {
                        case TYPE_BOA:
                            saveOrUpdateInDB(new Boa());
                            break;
                        case TYPE_TRACK:
                            saveOrUpdateInDB(new Track());
                            break;
                        case TYPE_SHIP:
                            saveOrUpdateInDB(new ShipPosition());
                    }
                    break;
                //TODO
                case ACTION_DELETE:
                    type = intent.getStringExtra(TYPE);
                    switch (type)
                    {
                        case TYPE_BOA:
                            deleteInDB(new Boa());
                            break;
                        case TYPE_TRACK:
                            deleteInDB(new Track());
                            break;
                        case TYPE_SHIP:
                            deleteInDB(new ShipPosition());
                    }
                    break;
            }
        }
    }

    private void deleteInDB(ShipPosition shipPosition)
    {
        DBHelper.get(getApplicationContext()).delete(shipPosition);
    }

    private void deleteInDB(Track track)
    {
        DBHelper.get(getApplicationContext()).delete(track);
    }

    private void deleteInDB(Boa boa)
    {
        DBHelper.get(getApplicationContext()).delete(boa);
    }

    private void saveOrUpdateInDB(Boa boa)
    {
        DBHelper.get(getApplicationContext()).saveOrUpdate(boa);
    }

    private void saveOrUpdateInDB(ShipPosition shipPosition)
    {
        DBHelper.get(getApplicationContext()).saveOrUpdate(shipPosition);
    }

    private void saveOrUpdateInDB(Track track)
    {
        DBHelper.get(getApplicationContext()).saveOrUpdate(track);
    }

    private void saveInDB(Boa boa)
    {

        DBHelper.get(getApplicationContext()).save(boa);
    }

    private void saveInDB(Track track)
    {

        DBHelper.get(getApplicationContext()).save(track);
    }

    private void saveInDB(ShipPosition shipPosition)
    {

        DBHelper.get(getApplicationContext()).save(shipPosition);
    }

    private void getTracksFromDB()
    {
        List<Track> tracks = DBHelper.get(getApplicationContext()).getAllTracks();

        Intent intent = new Intent(FragmentList.ACTION_SERVICE_DB_GET_ALL_TRACKS);
        intent.putExtra("data", Track.toJSONArray(tracks).toString());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
