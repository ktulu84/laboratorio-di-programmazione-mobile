package it.univaq.ing.myshiprace.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.model.Race;
import it.univaq.ing.myshiprace.model.ShipPosition;

/**
 * MyService
 * Created by leonardo on 10/11/17.
 * <p>
 * BiTE s.r.l.
 * contact info@bitesrl.it
 */

public class TableRace
{

    static final String TABLE_NAME = "races";

    static final String ID = "id";
    static final String TRACK_ID = "track_id";
    static final String START_TIME = "start_time";


    public static void create(SQLiteDatabase db)
    {
        String sql = "CREATE TABLE " + TABLE_NAME + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TRACK_ID + " INTEGER, " +
                START_TIME + " NUMERIC" +
                ")";
        db.execSQL(sql);
    }

    public static void drop(SQLiteDatabase db)
    {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
    }

    public static void upgrade(SQLiteDatabase db)
    {
        drop(db);
        create(db);
    }

    public static void save(SQLiteDatabase db, Race race)
    {

        ContentValues values = new ContentValues();
        values.put(START_TIME, race.getStartTime().getTime());
        values.put(TRACK_ID, race.getTrackID());
        long id = db.insert(TABLE_NAME, null, values);
        for (ShipPosition position : race.getPath())
        {
            position.setRaceID((int) id);
            TableShipPosition.save(db, position);
        }
        if (id != -1) race.setId((int) id);
    }

    public static boolean update(SQLiteDatabase db, Race race)
    {
        ContentValues values = new ContentValues();
        values.put(ID, race.getId());
        values.put(START_TIME, race.getStartTime().getTime());
        values.put(TRACK_ID, race.getTrackID());
//        int rows = db.update(TABLE_NAME, values, ID +"= ?", new String[]{ String.valueOf(shipPosition.getId())} );
        for (ShipPosition position : race.getPath())
        {
            if (position.getId() == -1)
            {
                TableShipPosition.save(db, position);
            }
            else
            {
                TableShipPosition.update(db, position);
            }
        }
        return db.update(TABLE_NAME, values, ID + "=" + race.getId(), null) == 1;
    }

    public static boolean delete(SQLiteDatabase db, Race race)
    {
        return db.delete(TABLE_NAME, ID + "=" + race.getId(), null) == 1;
    }

    public static List<Race> getAll(SQLiteDatabase db)
    {

        List<Race> races = new ArrayList<>();

        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + ID + " ASC";
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext())
            {
                Race race = new Race();
                race.setId((int) cursor.getLong(cursor.getColumnIndex(ID)));
                race.setTrackID(cursor.getInt(cursor.getColumnIndex(TRACK_ID)));
                race.setStartTime(new Timestamp(cursor.getLong(cursor.getColumnIndex(START_TIME))));
                List<ShipPosition> positions = TableShipPosition.getByRaceID(db, race.getId());
                for (ShipPosition p : positions)
                {
                    race.addPosition(p);
                }
                races.add(race);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null) cursor.close();
        }

        return races;
    }

    public static Race getByID(SQLiteDatabase db, int id)
    {
        Race race = new Race();

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "=" + id;

        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            cursor.moveToNext();
            race.setId((int) cursor.getLong(cursor.getColumnIndex(ID)));
            race.setTrackID(cursor.getInt(cursor.getColumnIndex(TRACK_ID)));
            race.setStartTime(new Timestamp(cursor.getLong(cursor.getColumnIndex(START_TIME))));
            List<ShipPosition> positions = TableShipPosition.getByRaceID(db, race.getId());
            for (ShipPosition p : positions)
            {
                race.addPosition(p);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null) cursor.close();
        }

        return race;
    }
}
