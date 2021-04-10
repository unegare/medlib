package com.example.medlib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class VisitDB {
    SQLiteDatabase db;
    VisitDBHelper dbHelper;
    Context context;

    public class VisitDBHelper extends SQLiteOpenHelper {
        public static final String DB_NAME = "visits.db";
        public static final int DB_VERSION = 2;
        public static final String TABLE_NAME_VISITS = "visits";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_DOCNAME = "docname";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_ATTACHED_DATA_TYPE = "datatype";
        public static final String COLUMN_DATA_BLOB = "datablob";
        public static final String COLUMN_PROFILE_ID = "profile_id";

        public static final String CREATE_TABLE_VISITS = "CREATE TABLE " + TABLE_NAME_VISITS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DOCNAME + " TEXT NOT NULL, "
                + COLUMN_PROFILE_ID + " INTEGER NOT NULL, "
                + COLUMN_DATE + " INTEGER DEFAULT (strftime('%s', 'now')), "
                + COLUMN_ATTACHED_DATA_TYPE + " TEXT DEFAULT '', "
                + COLUMN_DATA_BLOB + " BLOB"
                + ");";

        public static final String TABLE_NAME_PROFILES = "profiles";
        public static final String COLUMN_PROFILE_NAME = "profile_name";
        public static final String CREATE_TABLE_PROFILES = "CREATE TABLE " + TABLE_NAME_PROFILES + "("
                + COLUMN_PROFILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PROFILE_NAME + " TEXT NOT NULL "
                + ");";

        public static final String TRIGGER_NAME = "visits_profile_check";
        public static final String CREATE_TRIGGER = "CREATE TRIGGER " + TRIGGER_NAME
                + " BEFORE INSERT ON " + TABLE_NAME_VISITS
                + " WHEN 0 = (SELECT count(*) FROM " +  TABLE_NAME_PROFILES + " WHERE " + COLUMN_PROFILE_ID + " = NEW." + COLUMN_PROFILE_ID + ")"
                + " BEGIN SELECT RAISE(FAIL, 'invalid " + COLUMN_PROFILE_ID + " (check result)') ; END ;";


        public VisitDBHelper(Context ctx) {
            super(ctx, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(VisitDBHelper.class.getName(), "CREATING VISITS DB");
            db.execSQL(CREATE_TABLE_PROFILES);
            db.execSQL(CREATE_TABLE_VISITS);
            db.execSQL(CREATE_TRIGGER);

            Log.i(VisitDB.class.getName(), "onCreate: table list:");
            Cursor cur = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table';", null);
            if (cur.moveToFirst()) {
                do {
                    Log.i(VisitDB.class.getName(), "onCreate: " + cur.getString(cur.getColumnIndex("name")));
                } while (cur.moveToNext());
            }

            genSampleDB(db, 4, 10);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(VisitDBHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + " ... (DROP and recreate)");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_VISITS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PROFILES + ";");
            db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_NAME + ";");
            onCreate(db);
        }

        public void addRecord(SQLiteDatabase db, String docname, int profile_id) {
            Log.i(VisitDBHelper.class.getName(), "addRecord: { docname: \"" + docname + "\", profile: " + profile_id + " }");
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_DOCNAME, docname);
            cv.put(COLUMN_PROFILE_ID, profile_id);
            long res = db.insert(TABLE_NAME_VISITS, null, cv);
            Log.i(VisitDB.class.getName(), "res = " + res);
        }

        public void addProfile(SQLiteDatabase db, String profile_name) {
            Log.i(VisitDBHelper.class.getName(), "addProfile: { profile_name: \"" + profile_name + "\" }");
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_PROFILE_NAME, profile_name);
            long res = db.insert(TABLE_NAME_PROFILES, null, cv);
            Log.i(VisitDBHelper.class.getName(), "res = " + res);
        }

        public void genSampleDB(SQLiteDatabase db, int numOfProfiles, int numOfVisits) {
            for (int i = 0; i < numOfProfiles; i++) {
                addProfile(db, "Profile #" + i);
            }
            Random rnd = new Random();
            for (int i = 0; i < numOfVisits; i++) {
                addRecord(db, "Docname #" + i, rnd.nextInt(numOfProfiles) + 1);
            }
        }

        public Cursor getAllUsedProfilesIDs(SQLiteDatabase db) {
            Cursor cur = db.rawQuery("SELECT DISTINCT " + COLUMN_PROFILE_ID + " FROM " + TABLE_NAME_VISITS + ";", null);
            return cur;
        }

        public Cursor getAllUsedProfilesNames(SQLiteDatabase db) {
            Cursor cur = db.rawQuery("SELECT * FROM " + TABLE_NAME_PROFILES + " PR "
                    + " WHERE EXISTS (SELECT 1 FROM " + TABLE_NAME_VISITS + " VS "
                    + " WHERE VS." + COLUMN_PROFILE_ID + " = PR." + COLUMN_PROFILE_ID + ");", null);
            return cur;
        }
    }

    public VisitDB(Context ctx) {
        this.context = ctx;
        dbHelper = new VisitDBHelper(context);
    }

    public VisitDB open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public int getNumOfRecords() {
        Cursor cur = db.rawQuery("SELECT count(*) FROM " + dbHelper.TABLE_NAME_VISITS + ";", null);
        int res = -1;
        if (cur.moveToFirst()) {
                res = cur.getInt(0);
        }
        return res;
    }

    public Cursor getAllRecords() {
        Cursor cur = db.rawQuery("SELECT * FROM " + dbHelper.TABLE_NAME_VISITS + ";", null);
        return cur;
    }

    public void addRecord(String docname, int profile_id) {
        dbHelper.addRecord(db, docname, profile_id);
    }

    public Cursor getAllUsedProfilesIDs() {
        return dbHelper.getAllUsedProfilesIDs(db);
    }

    public Cursor getAllUsedProfilesNames() {
        return dbHelper.getAllUsedProfilesNames(db);
    }

    public void close() throws SQLException {
        db.close();
    }
}
