package it.univaq.ing.myshiprace.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.model.Boa;

/**
 * MyService
 * Created by leonardo on 10/11/17.
 * <p>
 * BiTE s.r.l.
 * contact info@bitesrl.it
 */

public class TableBoa
{

    private static final String TABLE_NAME = "boas";

    private static final String ID = "id";
    private static final String ORDER = "order";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String TRACK_ID = "track_id";

    public static void create(SQLiteDatabase db)
    {
        String sql = "CREATE TABLE " + TABLE_NAME + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ORDER + "INTEGER, " +
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

    public static void save(SQLiteDatabase db, Boa boa)
    {

        ContentValues values = new ContentValues();
        values.put(ORDER, boa.getOrder());
        values.put(LATITUDE, boa.getLatitude());
        values.put(LONGITUDE, boa.getLongitude());
        values.put(TRACK_ID, boa.getTrackID());
        long id = db.insert(TABLE_NAME, null, values);
        if (id != -1) boa.setId((int) id);
    }

    public static boolean update(SQLiteDatabase db, Boa boa)
    {

        ContentValues values = new ContentValues();
        values.put(ORDER, boa.getOrder());
        values.put(LATITUDE, boa.getLatitude());
        values.put(LONGITUDE, boa.getLongitude());
        values.put(TRACK_ID, boa.getTrackID());
//        int rows = db.update(TABLE_NAME, values, ID +"= ?", new String[]{ String.valueOf(boa.getId())} );
        return db.update(TABLE_NAME, values, ID + "=" + boa.getId(), null) == 1;
    }

    public static boolean delete(SQLiteDatabase db, Boa boa)
    {

        return db.delete(TABLE_NAME, ID + "=" + boa.getId(), null) == 1;
    }

    public static List<Boa> getAll(SQLiteDatabase db)
    {

        List<Boa> boas = new ArrayList<>();

        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + ORDER + " ASC";
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext())
            {
                Boa boa = new Boa();
                boa.setId((int) cursor.getLong(cursor.getColumnIndex(ID)));
                boa.setOrder(cursor.getInt(cursor.getColumnIndex(ORDER)));
                boa.setLatitude(cursor.getDouble(cursor.getColumnIndex(LATITUDE)));
                boa.setLongitude(cursor.getDouble(cursor.getColumnIndex(LONGITUDE)));
                boa.setTrackID(cursor.getInt(cursor.getColumnIndex(TRACK_ID)));
                boas.add(boa);
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

        return boas;
    }
}
