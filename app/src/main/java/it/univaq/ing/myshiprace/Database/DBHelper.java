package it.univaq.ing.myshiprace.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.Race;
import it.univaq.ing.myshiprace.model.ShipPosition;
import it.univaq.ing.myshiprace.model.Track;

/**
 * MyService
 * Created by leonardo on 10/11/17.
 * <p>
 * BiTE s.r.l.
 * contact info@bitesrl.it
 */

public class DBHelper extends SQLiteOpenHelper
{

    private static final String NAME = "myDatabase.db";
    private static final int VERSION = 1;

    private static DBHelper instance = null;

    public static DBHelper get(Context context)
    {
        return instance == null ? instance = new DBHelper(context) : instance;
    }

    private DBHelper(Context context)
    {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        if (!db.isReadOnly())
        {
            String SQL = "PRAGMA foreign_keys=ON;";
            db.execSQL(SQL);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        TableTrack.create(db);
        TableShipPosition.create(db);
        TableBoa.create(db);
        TableRace.create(db);
    }

    public boolean setTransmitted(ShipPosition shipPosition)
    {
        return TableShipPosition.setTransmitted(getWritableDatabase(), shipPosition);
    }

    public List<ShipPosition> getUntrasmitted(int raceID)
    {
        return TableShipPosition.getUntrasmitted(getWritableDatabase(), raceID);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        TableTrack.upgrade(db);
        TableShipPosition.upgrade(db);
        TableBoa.upgrade(db);
        TableRace.upgrade(db);
    }

    public void save(Race race)
    {
        TableRace.save(getWritableDatabase(), race);
    }

    public void save(Boa boa)
    {
        TableBoa.save(getWritableDatabase(), boa);
    }

    public void save(ShipPosition shipPosition)
    {
        TableShipPosition.save(getWritableDatabase(), shipPosition);
    }

    public void save(Track track)
    {
        TableTrack.save(getWritableDatabase(), track);
    }

    public void update(Race race)
    {
        TableRace.update(getWritableDatabase(), race);
    }

    public void update(Boa boa)
    {
        TableBoa.update(getWritableDatabase(), boa);
    }

    public void update(ShipPosition shipPosition)
    {
        TableShipPosition.update(getWritableDatabase(), shipPosition);
    }

    public void update(Track track)
    {
        TableTrack.update(getWritableDatabase(), track);
    }

    public void saveOrUpdate(Race race)
    {
        if (race.getId() == -1)
        {
            save(race);
        }
        else
        {
            update(race);
        }
    }

    public void saveOrUpdate(Boa boa)
    {
        if (boa.getId() == -1)
        {
            save(boa);
        }
        else
        {
            update(boa);
        }
    }

    public void saveOrUpdate(ShipPosition shipPosition)
    {
        if (shipPosition.getId() == -1)
        {
            save(shipPosition);
        }
        else
        {
            update(shipPosition);
        }
    }

    public void saveOrUpdate(Track track)
    {
        if (track.getId() == -1)
        {
            save(track);
        }
        else
        {
            update(track);
        }
    }

    public void delete(Boa boa)
    {
        TableBoa.delete(getWritableDatabase(), boa);
    }

    public void delete(ShipPosition shipPosition)
    {
        TableShipPosition.delete(getWritableDatabase(), shipPosition);
    }

    public void delete(Track track)
    {
        TableTrack.delete(getWritableDatabase(), track);
    }

    public List<Boa> getAllBoas()
    {
        return TableBoa.getAll(getReadableDatabase());
    }

    public List<ShipPosition> getAllPositions()
    {
        return TableShipPosition.getAll(getReadableDatabase());
    }

    public List<Track> getAllTracks()
    {
        return TableTrack.getAll(getReadableDatabase());
    }

    public Boa getBoa(int id)
    {
        return TableBoa.getByID(getReadableDatabase(), id);
    }

    public ShipPosition getShipPosition(int id)
    {
        return TableShipPosition.getByID(getReadableDatabase(), id);
    }

    public Track getRaceTrack(int id)
    {
        return TableTrack.getByID(getReadableDatabase(), id);
    }
}
