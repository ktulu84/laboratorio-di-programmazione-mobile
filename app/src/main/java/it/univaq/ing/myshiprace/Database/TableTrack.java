package it.univaq.ing.myshiprace.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.model.RaceTrack;

/**
 * MyService
 * Created by leonardo on 10/11/17.
 * <p>
 * BiTE s.r.l.
 * contact info@bitesrl.it
 */

public class TableTrack
{

    static final String TABLE_NAME = "tracks";

    static final String ID = "id";
    private static final String TRACK_NAME = "track_name";

    public static void create(SQLiteDatabase db)
    {
        String sql = "CREATE TABLE " + TABLE_NAME + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TRACK_NAME + " TEXT, " +
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

    public static void save(SQLiteDatabase db, RaceTrack raceTrack)
    {

        ContentValues values = new ContentValues();
        values.put(TRACK_NAME, raceTrack.getTrackName());
        long id = db.insert(TABLE_NAME, null, values);
        if (id != -1) raceTrack.setId((int) id);
    }

    public static boolean update(SQLiteDatabase db, RaceTrack raceTrack)
    {

        ContentValues values = new ContentValues();
        values.put(TRACK_NAME, raceTrack.getTrackName());
//        int rows = db.update(TABLE_NAME, values, ID +"= ?", new String[]{ String.valueOf(shipPosition.getId())} );
        return db.update(TABLE_NAME, values, ID + "=" + raceTrack.getId(), null) == 1;
    }

    public static boolean delete(SQLiteDatabase db, RaceTrack raceTrack)
    {

        return db.delete(TABLE_NAME, ID + "=" + raceTrack.getId(), null) == 1;
    }

    public static List<RaceTrack> getAll(SQLiteDatabase db)
    {

        List<RaceTrack> raceTracks = new ArrayList<>();

        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + ID + " ASC";
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext())
            {
                RaceTrack raceTrack = new RaceTrack();
                raceTrack.setId((int) cursor.getLong(cursor.getColumnIndex(ID)));
                raceTrack.setTrackName(cursor.getString(cursor.getColumnIndex(TRACK_NAME)));
                raceTracks.add(raceTrack);
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

        return raceTracks;
    }
}
