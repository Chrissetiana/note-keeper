package com.chrissetiana.notekeeper;

import android.net.Uri;

public final class NoteKeeperProviderContract {

    public static final String AUTHORITY = "com.chrissetiana.notekeeper.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public NoteKeeperProviderContract() {

    }

    public static final class Courses {
        public static final String PATH = "courses";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }

    public static final class Notes {
        public static final String PATH = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }
}
