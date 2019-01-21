package com.chrissetiana.notekeeper;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import static com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import static com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

//import android.support.v4.content.CursorLoader;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String NOTE_ID = "com.chrissetiana.notekeeper.NOTE_ID";
    public static final String ORIGINAL_NOTE_ID = "com.chrissetiana.notekeeper.NOTE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.chrissetiana.notekeeper.NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.chrissetiana.notekeeper.NOTE_TEXT";
    public static final int ID_NOT_SET = -1;
    public static final int SHOW_CAMERA = 1;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    public static final String TAG = "Note Activity";
    private NoteInfo note;
    private boolean isNewNote;
    private Spinner spinnerCourses;
    private EditText textTitle;
    private EditText textNote;
    private int noteId;
    private boolean isCancelling;
    private String originalNoteId;
    private String originalNoteTitle;
    private String originalNoteText;
    private NoteKeeperDatabaseHelper databaseHelper;
    private Cursor noteCursor;
    private int cursorIdPos;
    private int cursorTitlePos;
    private int cursorTextPos;
    private SimpleCursorAdapter adapterCourses;
    private boolean notesQueryFinished;
    private boolean courseQueryFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseHelper = new NoteKeeperDatabaseHelper(this);

        spinnerCourses = findViewById(R.id.spinner_courses);

        final String[] str = {CourseInfoEntry.COLUMN_COURSE_TITLE};
        final int[] text = {android.R.id.text1};
        final int layout = android.R.layout.simple_spinner_item;

        adapterCourses = new SimpleCursorAdapter(this, layout, null, str, text, 0);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(adapterCourses);

        getLoaderManager().initLoader(LOADER_COURSES, null, this);

        readDisplayStateValues();

        if (savedInstanceState == null) {
            saveOriginalStateValues();
        } else {
            restoreOriginalStateValues(savedInstanceState);
        }

        textTitle = findViewById(R.id.text_title);
        textNote = findViewById(R.id.text_note);

        if (!isNewNote) {
            getLoaderManager().initLoader(LOADER_NOTES, null, this);
        }
    }

    private void loadCourseData() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID};

        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns, null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);

        adapterCourses.changeCursor(cursor);
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

    private void loadNoteData() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String courseId = "android_intents";
        String titleStart = "dynamic";

        String selection = NoteInfoEntry._ID + " = ? AND " + NoteInfoEntry.COLUMN_NOTE_TITLE + " LIKE ? ";
        String[] selectionArgs = {Integer.toString(noteId)};
        String[] columns = {NoteInfoEntry.COLUMN_COURSE_ID, NoteInfoEntry.COLUMN_NOTE_TITLE, NoteInfoEntry.COLUMN_NOTE_TEXT};

        noteCursor = db.query(NoteInfoEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);

        cursorIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        cursorTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        cursorTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        noteCursor.moveToNext();
        displayNote();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(ORIGINAL_NOTE_ID, originalNoteId);
        outState.putString(ORIGINAL_NOTE_TITLE, originalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, originalNoteText);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;

        menuItem.setEnabled(noteId < lastNoteIndex);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_add_image) {
//            addImage();
            return true;
        } else if (id == R.id.action_next) {
            moveNext();
        } else if (id == R.id.action_cancel) {
            isCancelling = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SHOW_CAMERA && resultCode == RESULT_OK) {
            assert data != null;
            Bitmap thumbnail = data.getParcelableExtra("data");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isCancelling) {
            if (isNewNote) {
                Log.i(TAG, "Cancelling note at position: " + noteId);
                DataManager.getInstance().removeNote(noteId);
            } else {
                storePreviousStateValues();
            }
        } else {
            saveNote();
        }

        Log.d(TAG, "onPause");
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        noteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        isNewNote = noteId == ID_NOT_SET;

        if (isNewNote) {
            createNote();
        }

//        note = DataManager.getInstance().getNotes().get(noteId);
    }

    private void saveOriginalStateValues() {
        if (isNewNote) {
            return;
        }

        originalNoteId = note.getCourse().getCourseId();
        originalNoteTitle = note.getTitle();
        originalNoteText = note.getText();
    }

    private void storePreviousStateValues() {
        CourseInfo course = DataManager.getInstance().getCourse(originalNoteId);
        note.setCourse(course);
        note.setTitle(originalNoteTitle);
        note.setText(originalNoteText);
    }

    private void restoreOriginalStateValues(Bundle savedInstanceState) {
        originalNoteId = savedInstanceState.getString(ORIGINAL_NOTE_ID);
        originalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        originalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    private void displayNote() {
        String courseId = noteCursor.getString(cursorIdPos);
        String courseTitle = noteCursor.getString(cursorTitlePos);
        String courseText = noteCursor.getString(cursorTextPos);

        int courseIndex = getCourseIdIndex(courseId);
        spinnerCourses.setSelection(courseIndex);

        textTitle.setText(courseTitle);
        textNote.setText(courseText);
    }

    private int getCourseIdIndex(String courseId) {
        Cursor cursor = adapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;
        boolean more = cursor.moveToFirst();

        while (more) {
            String cursorId = cursor.getString(courseIdPos);

            if (courseId.equals(cursorId)) {
                break;
            }

            courseRowIndex++;
            more = cursor.moveToNext();
        }

        return courseRowIndex;
    }

    private void createNote() {
        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, "");

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        noteId = (int) db.insert(NoteInfoEntry.TABLE_NAME, null, values);
    }

    private void saveNote() {
        String courseId = selectedCourseId();
        String noteTitle = textTitle.getText().toString();
        String noteText = textNote.getText().toString();

        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = spinnerCourses.getSelectedItemPosition();

        Cursor cursor = adapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);

        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);

        return cursor.getString(courseIdPos);
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {
        String selection = NoteInfoEntry._ID + " = ? ";
        String[] selectionArgs = {Integer.toString(noteId)};

        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) spinnerCourses.getSelectedItem();

        String subject = textTitle.getText().toString();
        String note = textNote.getText().toString();
        String message = "Checkout what I learned in the PluralSight course \"" + course.getTitle() + "\"\n" + note;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(intent);
    }

    private void addImage(Uri photoFile) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile);
        startActivityForResult(intent, SHOW_CAMERA);
    }

    private void moveNext() {
        saveNote();

        ++noteId;
        note = DataManager.getInstance().getNotes().get(noteId);

        saveOriginalStateValues();
        displayNote();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;

        if (id == LOADER_NOTES) {
            loader = createLoaderNotes();
        } else if (id == LOADER_COURSES) {
            loader = createLoaderCourses();
        }

        return loader;
    }

    @SuppressLint("StaticFieldLeak")
    private CursorLoader createLoaderCourses() {
        courseQueryFinished = false;

        return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();

                String courseId = "android_intents";
                String titleStart = "dynamic";

                String selection = NoteInfoEntry._ID + " = ? ";
                String[] selectionArgs = {Integer.toString(noteId)};
                String[] columns = {
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT};

                return db.query(NoteInfoEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
            }
        };
    }

    @SuppressLint("StaticFieldLeak")
    private CursorLoader createLoaderNotes() {
        notesQueryFinished = false;

        return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();

                String courseId = "android_intents";
                String titleStart = "dynamic";

                String selection = NoteInfoEntry._ID + " = ? ";
                String[] selectionArgs = {Integer.toString(noteId)};
                String[] columns = {
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT};

                return db.query(NoteInfoEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES) {
            loadFinishedNotes(data);
        } else if (loader.getId() == LOADER_COURSES) {
            adapterCourses.changeCursor(data);
            courseQueryFinished = true;
            displayNotesWhenQueriesFinished();
        }
    }

    private void loadFinishedNotes(Cursor data) {
        noteCursor = data;

        cursorIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        cursorTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        cursorTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        notesQueryFinished = true;

        noteCursor.moveToNext();
        displayNotesWhenQueriesFinished();
    }

    private void displayNotesWhenQueriesFinished() {
        if (notesQueryFinished && courseQueryFinished) {
            displayNote();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES) {
            if (noteCursor != null) {
                noteCursor.close();
            }
        } else if (loader.getId() == LOADER_COURSES) {
            adapterCourses.changeCursor(null);
        }
    }
}
