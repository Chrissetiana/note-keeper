package com.chrissetiana.notekeeper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.chrissetiana.notekeeper.NoteKeeperProviderContract.Notes;

import static com.chrissetiana.notekeeper.NoteActivity.simulateLongRunningWork;

public class NoteUpload {
    private final String TAG = getClass().getSimpleName();

    private final Context context;
    private boolean cancelled;

    public NoteUpload(Context context) {
        this.context = context;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

    public void doUpload(Uri dataUri) {
        String[] columns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT};

        Cursor cursor = context.getContentResolver().query(dataUri, columns, null, null, null);

        int courseIdPos = cursor.getColumnIndex(Notes.COLUMN_COURSE_ID);
        int noteTitlePos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TITLE);
        int noteTextPos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TEXT);

        Log.i(TAG, ">>>***  UPLOAD START - " + dataUri + " ***<<<");

        cancelled = false;

        while (!cancelled && cursor.moveToNext()) {
            String courseId = cursor.getString(courseIdPos);
            String noteTitle = cursor.getString(noteTitlePos);
            String noteText = cursor.getString(noteTextPos);

            if (!noteTitle.equals("")) {
                Log.i(TAG, ">>>Uploading Note<<<" + courseId + " | " + noteTitle + " | " + noteText);
                simulateLongRunningWork();
            }
        }

        Log.i(TAG, ">>>***  UPLOAD COMPLETE ***<<<");
        cursor.close();
    }
}
