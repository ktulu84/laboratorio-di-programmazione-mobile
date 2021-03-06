package it.univaq.ing.myshiprace.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.Track;

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
    static final String TRACK_NAME = "track_name";

    public static void upgrade(SQLiteDatabase db)
    {
        drop(db);
        create(db);
    }

    public static void drop(SQLiteDatabase db)
    {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
    }

    public static void create(SQLiteDatabase db)
    {
        String sql = "CREATE TABLE " + TABLE_NAME + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TRACK_NAME + " TEXT" +
                ")";
        db.execSQL(sql);
    }

    public static void save(SQLiteDatabase db, Track track)
    {

        ContentValues values = new ContentValues();
        values.put(TRACK_NAME, track.getTrackName());
        long id = db.insert(TABLE_NAME, null, values);
        for (Boa b : track.getBoas())
        {
            b.setTrackID((int) id);
            TableBoa.save(db, b);
        }
        if (id != -1) track.setId((int) id);
    }

    public static boolean update(SQLiteDatabase db, Track track)
    {

        ContentValues values = new ContentValues();
        values.put(TRACK_NAME, track.getTrackName());
//        int rows = db.update(TABLE_NAME, values, ID +"= ?", new String[]{ String.valueOf(shipPosition.getId())} );
        for (Boa b : track.getBoas())
        {
            if (b.getId() == -1)
            {
                TableBoa.save(db, b);
            }
            else
            {
                TableBoa.update(db, b);
            }
        }
        return db.update(TABLE_NAME, values, ID + "=" + track.getId(), null) == 1;
    }

    public static boolean delete(SQLiteDatabase db, Track track)
    {
        return db.delete(TABLE_NAME, ID + "=" + track.getId(), null) == 1;
    }

    public static List<Track> getAll(SQLiteDatabase db)
    {

        List<Track> tracks = new ArrayList<>();

        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + ID + " ASC";
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext())
            {
                Track track = new Track();
                track.setId((int) cursor.getLong(cursor.getColumnIndex(ID)));
                track.setTrackName(cursor.getString(cursor.getColumnIndex(TRACK_NAME)));
                List<Boa> boas = TableBoa.getByTrackID(db, track.getId());
                for (Boa b : boas)
                {
                    track.addBoa(b);
                }
                tracks.add(track);
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

        return tracks;
    }

    public static Track getByID(SQLiteDatabase db, int id)
    {
        Track track = new Track();

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "=" + id;

        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            cursor.moveToNext();
            track.setId((int) cursor.getLong(cursor.getColumnIndex(ID)));
            track.setTrackName(cursor.getString(cursor.getColumnIndex(TRACK_NAME)));
            List<Boa> boas = TableBoa.getByTrackID(db, track.getId());
            for (Boa b : boas)
            {
                track.addBoa(b);
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

        return track;
    }
}
