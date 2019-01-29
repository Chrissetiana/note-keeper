package com.chrissetiana.notekeeper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.chrissetiana.notekeeper.NoteKeeperProviderContract.CourseIdColumns;
import com.chrissetiana.notekeeper.NoteKeeperProviderContract.Courses;
import com.chrissetiana.notekeeper.NoteKeeperProviderContract.Notes;

public class NoteKeeperDatabaseProvider extends ContentProvider {

    public static final int COURSES = 0;
    public static final int NOTES = 1;
    private static final int NOTES_EXPANDED = 2;
    private static final int NOTES_ROW = 3;
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES);
        uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES);
        uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
        uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH + "/#", NOTES_ROW);
    }

    private NoteKeeperDatabaseHelper databaseHelper;

    public NoteKeeperDatabaseProvider() {

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Uri rowUri = null;
        long rowId = -1;
        int uriMatch = uriMatcher.match(uri);

        switch (uriMatch) {
            case NOTES:
                rowId = db.insert(NoteInfoEntry.TABLE_NAME, null, values);
                ContentUris.withAppendedId(Notes.CONTENT_URI, rowId);
                break;
            case COURSES:
                rowId = db.insert(CourseInfoEntry.TABLE_NAME, null, values);
                ContentUris.withAppendedId(Courses.CONTENT_URI, rowId);
                break;
            case NOTES_EXPANDED:
                break;
        }

        return rowUri;
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new NoteKeeperDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = null;

        int uriMatch = uriMatcher.match(uri);

        switch (uriMatch) {
            case COURSES:
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTES:
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder);
                break;
            case NOTES_ROW:
                long rowId = ContentUris.parseId(uri);
                String rowSelection = NoteInfoEntry._ID + "=?";
                String[] rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, rowSelection, rowSelectionArgs, null, null, null);
                break;
        }

        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String tablesJoin = NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME + " ON " +
                NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                CourseInfoEntry.getQualifiedName(CourseInfoEntry.COLUMN_COURSE_ID);

        String[] columns = new String[projection.length];

        for (int i = 0; i < projection.length; i++) {
            columns[i] = projection[i].equals(BaseColumns._ID) || projection[i].equals(CourseIdColumns.COLUMN_COURSE_ID)
                    ? NoteInfoEntry.getQName(projection[i]) : projection[i];
        }

        return db.query(tablesJoin, columns, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
