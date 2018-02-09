package app.solution.barcode.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toukirul on 30/1/2018.
 */

public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dbbarcode";
    private static final String TABLE_BARCODE = "tblbarcode";

    private static final String key_id = "_id";
    private static final String key_title = "title";
    private static final String key_query_result = "query_result";
    private static final String key_date_time = "date_time";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_BARCODE_TABLE = "CREATE TABLE " + TABLE_BARCODE + "("
                + key_id + " INTEGER PRIMARY KEY,"
                + key_title + " TEXT,"
                + key_date_time + " TEXT,"
                + key_query_result + " TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_BARCODE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_BARCODE);
        onCreate(sqLiteDatabase);
    }

    public void addItem(DbModelClass modelClass){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(key_title, modelClass.getTitle());
        values.put(key_date_time, modelClass.getDateTime());
        values.put(key_query_result, modelClass.getScanQuery());

        db.insert(TABLE_BARCODE ,null ,values);
        db.close();
    }

    public DbModelClass getItem(int id){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BARCODE, new String[]{key_id, key_title, key_date_time, key_query_result}, key_id + "=?"
                ,new String[]{String.valueOf(id)},null, null, null, null);

        if (cursor!=null){
            cursor.moveToFirst();
        }

        DbModelClass dbModelClass = new DbModelClass();
        dbModelClass.setId(Integer.parseInt(cursor.getString(0)));
        dbModelClass.setTitle(cursor.getString(1));
        dbModelClass.setDateTime(cursor.getString(2));
        dbModelClass.setScanQuery(cursor.getString(3));

        return dbModelClass;
    }

    // Getting All Contacts
    public List<DbModelClass> getAllContacts() {
        List<DbModelClass> contactList = new ArrayList<DbModelClass>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_BARCODE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DbModelClass dbModelClass = new DbModelClass();
                dbModelClass.setId(Integer.parseInt(cursor.getString(0)));
                dbModelClass.setTitle(cursor.getString(1));
                dbModelClass.setDateTime(cursor.getString(2));
                dbModelClass.setScanQuery(cursor.getString(3));
                // Adding contact to list
                contactList.add(dbModelClass);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    // Deleting single contact
    public void deleteContact(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BARCODE, key_id + "=?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_BARCODE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int c = cursor.getCount();
        cursor.close();

        // return count
        return c;
    }
}
