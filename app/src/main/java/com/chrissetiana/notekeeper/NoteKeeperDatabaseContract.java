package com.chrissetiana.notekeeper;

import android.provider.BaseColumns;

final class NoteKeeperDatabaseContract {

    private NoteKeeperDatabaseContract() {
    }

    static final class CourseInfoEntry implements BaseColumns {
        static final String TABLE_NAME = "course_info";
        static final String COLUMN_COURSE_ID = "course_id";
        static final String COLUMN_COURSE_TITLE = "course_title";

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_COURSE_ID + " TEXT UNIQUE NOT NULL, " +
                        COLUMN_COURSE_TITLE + " TEXT NOT NULL)";
    }

    static final class NoteInfoEntry implements BaseColumns {
        static final String TABLE_NAME = "note_info";
        static final String COLUMN_NOTE_TITLE = "note_title";
        static final String COLUMN_NOTE_TEXT = "note_text";
        static final String COLUMN_COURSE_ID = "course_id";

        static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_NOTE_TITLE + " TEXT NOT NULL, " +
                        COLUMN_NOTE_TEXT + " TEXT, " +
                        COLUMN_COURSE_ID + " TEXT NOT NULL)";
    }
}
