package com.chrissetiana.notekeeper;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NoteKeeperProviderContract {

    public static final String AUTHORITY = "com.chrissetiana.notekeeper.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public NoteKeeperProviderContract() {

    }

    protected interface CourseIdColumns {
        String COLUMN_COURSE_ID = "course_id";
    }

    protected interface CourseColumns {
        String COLUMN_COURSE_TITLE = "course_title";
    }

    protected interface NotesColumns {
        String COLUMN_NOTE_TITLE = "note_title";
        String COLUMN_NOTE_TEXT = "note_text";
    }

    public static final class Courses implements BaseColumns, CourseIdColumns, CourseColumns {
        public static final String PATH = "courses";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }

    public static final class Notes implements BaseColumns, CourseIdColumns, NotesColumns {
        public static final String PATH = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }
}
