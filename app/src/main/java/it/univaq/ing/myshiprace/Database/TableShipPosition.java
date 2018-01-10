package it.univaq.ing.myshiprace.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.model.ShipPosition;

/**
 * MyService
 * Created by leonardo on 10/11/17.
 * <p>
 * BiTE s.r.l.
 * contact info@bitesrl.it
 */

public class TableShipPosition
{

    private static final String TABLE_NAME = "ship_position";

    private static final String ID = "id";
    private static final String TIMESTAMP = "timestamp";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String TRACK_ID = "track_id";

    public static void create(SQLiteDatabase db)
    {
        String sql = "CREATE TABLE " + TABLE_NAME + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TIMESTAMP + "NUMERIC, " +
                LATITUDE + " NUMERIC, " +
                LONGITUDE + " NUMERIC, " +
                TRACK_ID + "INTEGER" +
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

    public static void save(SQLiteDatabase db, ShipPosition shipPosition)
    {

        ContentValues values = new ContentValues();
        values.put(TIMESTAMP, shipPosition.getTimestamp().getTime());
        values.put(LATITUDE, shipPosition.getLatitude());
        values.put(LONGITUDE, shipPosition.getLongitude());
        values.put(TRACK_ID, shipPosition.getTrackID());
        long id = db.insert(TABLE_NAME, null, values);
        if (id != -1) shipPosition.setId((int) id);
    }

    public static boolean update(SQLiteDatabase db, ShipPosition shipPosition)
    {

        ContentValues values = new ContentValues();
        values.put(TIMESTAMP, shipPosition.getTimestamp().getTime());
        values.put(LATITUDE, shipPosition.getLatitude());
        values.put(LONGITUDE, shipPosition.getLongitude());
        values.put(TRACK_ID, shipPosition.getTrackID());
//        int rows = db.update(TABLE_NAME, values, ID +"= ?", new String[]{ String.valueOf(shipPosition.getId())} );
        return db.update(TABLE_NAME, values, ID + "=" + shipPosition.getId(), null) == 1;
    }

    public static boolean delete(SQLiteDatabase db, ShipPosition shipPosition)
    {

        return db.delete(TABLE_NAME, ID + "=" + shipPosition.getId(), null) == 1;
    }

    public static List<ShipPosition> getAll(SQLiteDatabase db)
    {

        List<ShipPosition> shipPositions = new ArrayList<>();

        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + TIMESTAMP + " ASC";
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext())
            {
                ShipPosition shipPosition = new ShipPosition();
                shipPosition.setId((int) cursor.getLong(cursor.getColumnIndex(ID)));
                shipPosition.setTimestamp(new Timestamp(cursor.getLong(cursor.getColumnIndex(TIMESTAMP))));
                shipPosition.setLatitude(cursor.getDouble(cursor.getColumnIndex(LATITUDE)));
                shipPosition.setLongitude(cursor.getDouble(cursor.getColumnIndex(LONGITUDE)));
                shipPosition.setTrackID(cursor.getInt(cursor.getColumnIndex(TRACK_ID)));
                shipPositions.add(shipPosition);
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

        return shipPositions;
    }
}
