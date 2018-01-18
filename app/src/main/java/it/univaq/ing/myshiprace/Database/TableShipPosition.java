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

    static final String TABLE_NAME = "ship_position";

    static final String ID = "id";
    static final String SHIP_NAME = "ship_name";
    static final String TIMESTAMP = "timestamp";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String RACE_ID = "race_id";
    static final String TRANSMITTED = "transmitted";

    public static void create(SQLiteDatabase db)
    {
        String sql = "CREATE TABLE " + TABLE_NAME + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TIMESTAMP + " NUMERIC, " +
                LATITUDE + " NUMERIC, " +
                LONGITUDE + " NUMERIC, " +
                RACE_ID + " INTEGER, " +
                SHIP_NAME + " TEXT, " +
                TRANSMITTED + " INTEGER, " +
                "FOREIGN KEY(" + RACE_ID + ") " +
                "REFERENCES " + TableRace.TABLE_NAME + "(" + TableRace.ID + ")" +
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
        values.put(RACE_ID, shipPosition.getRaceID());
        values.put(SHIP_NAME, shipPosition.getShipName());
        values.put(TRANSMITTED, 0);
        long id = db.insert(TABLE_NAME, null, values);
        if (id != -1) shipPosition.setId((int) id);
    }

    public static boolean update(SQLiteDatabase db, ShipPosition shipPosition)
    {

        ContentValues values = new ContentValues();
        values.put(TIMESTAMP, shipPosition.getTimestamp().getTime());
        values.put(LATITUDE, shipPosition.getLatitude());
        values.put(LONGITUDE, shipPosition.getLongitude());
        values.put(RACE_ID, shipPosition.getRaceID());
        values.put(SHIP_NAME, shipPosition.getShipName());
//        int rows = db.update(TABLE_NAME, values, ID +"= ?", new String[]{ String.valueOf(shipPosition.getId())} );
        return db.update(TABLE_NAME, values, ID + "=" + shipPosition.getId(), null) == 1;
    }

    public static boolean setTransmitted(SQLiteDatabase db, ShipPosition shipPosition)
    {
        ContentValues values = new ContentValues();

        values.put(TRANSMITTED, 1);
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
                shipPosition.setRaceID(cursor.getInt(cursor.getColumnIndex(RACE_ID)));
                shipPosition.setShipName(cursor.getString(cursor.getColumnIndex(SHIP_NAME)));
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

    public static ShipPosition getByID(SQLiteDatabase db, int id)
    {
        ShipPosition shipPosition = new ShipPosition();

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "=" + id;

        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            cursor.moveToNext();
            shipPosition.setId((int) cursor.getLong(cursor.getColumnIndex(ID)));
            shipPosition.setTimestamp(new Timestamp(cursor.getLong(cursor.getColumnIndex(TIMESTAMP))));
            shipPosition.setLatitude(cursor.getDouble(cursor.getColumnIndex(LATITUDE)));
            shipPosition.setLongitude(cursor.getDouble(cursor.getColumnIndex(LONGITUDE)));
            shipPosition.setRaceID(cursor.getInt(cursor.getColumnIndex(RACE_ID)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null) cursor.close();
        }

        return shipPosition;
    }

    public static List<ShipPosition> getByRaceID(SQLiteDatabase db, int id)
    {
        List<ShipPosition> shipPositions = new ArrayList<>();

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + RACE_ID + " = " + id + " ORDER BY " + TIMESTAMP + " ASC";
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext())
            {
                ShipPosition position = new ShipPosition();
                position.setId((int) cursor.getLong(cursor.getColumnIndex(ID)));
                position.setTimestamp(new Timestamp(cursor.getLong(cursor.getColumnIndex(TIMESTAMP))));
                position.setLatitude(cursor.getDouble(cursor.getColumnIndex(LATITUDE)));
                position.setLongitude(cursor.getDouble(cursor.getColumnIndex(LONGITUDE)));
                position.setRaceID(cursor.getInt(cursor.getColumnIndex(RACE_ID)));
                shipPositions.add(position);
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

    public static List<ShipPosition> getUntrasmitted(SQLiteDatabase db, int raceID)
    {
        List<ShipPosition> shipPositions = new ArrayList<>();

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + TRANSMITTED + " = " + 0 + " AND " + RACE_ID + " = " + raceID + " ORDER BY " + TIMESTAMP + " DESC";
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext())
            {
                ShipPosition position = new ShipPosition();
                position.setId((int) cursor.getLong(cursor.getColumnIndex(ID)));
                position.setTimestamp(new Timestamp(cursor.getLong(cursor.getColumnIndex(TIMESTAMP))));
                position.setLatitude(cursor.getDouble(cursor.getColumnIndex(LATITUDE)));
                position.setLongitude(cursor.getDouble(cursor.getColumnIndex(LONGITUDE)));
                position.setRaceID(cursor.getInt(cursor.getColumnIndex(RACE_ID)));
                shipPositions.add(position);
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
