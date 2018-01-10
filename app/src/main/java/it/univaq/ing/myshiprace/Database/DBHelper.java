package it.univaq.ing.myshiprace.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

import it.univaq.ing.myshiprace.model.Boa;

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
        TableBoa.create(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        TableBoa.upgrade(db);
    }

    public void save(Boa boa)
    {
        TableBoa.save(getWritableDatabase(), boa);
    }

    public void update(Boa boa)
    {
        TableBoa.update(getWritableDatabase(), boa);
    }

    public void delete(Boa boa)
    {
        TableBoa.delete(getWritableDatabase(), boa);
    }

    public List<Boa> getAll()
    {
        return TableBoa.getAll(getReadableDatabase());
    }
}
