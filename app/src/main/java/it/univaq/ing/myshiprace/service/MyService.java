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

/*
 * Class implementing intent service interacting with our DB
 */
public class MyService extends IntentService
{

    public static final String ACTION_SAVE = "action_save";
    public static final String ACTION_GETALL_TRACKS = "action_get_all_tracks";
    public static final String ACTION_SAVE_UPDATE = "action_save_update";

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
            switch (action)
            {
                //TODO
                case ACTION_SAVE:
                    String type = intent.getStringExtra("type");

                    saveInDB(new Boa());
                    break;

                case ACTION_GETALL_TRACKS:
                    getFromDB();
                    break;
                //TODO
                case ACTION_SAVE_UPDATE:
                    saveOrUpdateInDB(new Boa());
                    break;
            }
        }
    }

    private void saveInDB(Boa boa)
    {

        DBHelper.get(getApplicationContext()).save(boa);
    }

    private void getFromDB()
    {

        List<Track> tracks = DBHelper.get(getApplicationContext()).getAllTracks();

        Intent intent = new Intent(FragmentList.ACTION_SERVICE_DB_GET_ALL_TRACKS);
        intent.putExtra("data", Track.toJSONArray(tracks).toString());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void saveOrUpdateInDB(Boa boa)
    {
        DBHelper.get(getApplicationContext()).saveOrUpdate(boa);
    }

    /*
     * These methods are not actually used
     */
    private void saveOrUpdateInDB(ShipPosition shipPosition)
    {
        DBHelper.get(getApplicationContext()).saveOrUpdate(shipPosition);
    }

    private void saveOrUpdateInDB(Track track)
    {
        DBHelper.get(getApplicationContext()).saveOrUpdate(track);
    }

    private void saveInDB(Track track)
    {

        DBHelper.get(getApplicationContext()).save(track);
    }

    private void saveInDB(ShipPosition shipPosition)
    {

        DBHelper.get(getApplicationContext()).save(shipPosition);
    }
}
