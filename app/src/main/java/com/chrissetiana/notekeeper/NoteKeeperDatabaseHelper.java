package com.chrissetiana.notekeeper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

class NoteKeeperDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notekeeper.db";
    private static final int DATABASE_VERSION = 2;

    NoteKeeperDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CourseInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(NoteInfoEntry.SQL_CREATE_TABLE);

        db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1);
        db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1);

        DatabaseDataWorker dataWorker = new DatabaseDataWorker(db);
        dataWorker.insertCourses();
        dataWorker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1);
            db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1);
        }
    }
}
