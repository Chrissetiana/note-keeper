package com.chrissetiana.notekeeper;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.chrissetiana.notekeeper.NoteKeeperProviderContract.Courses;
import com.chrissetiana.notekeeper.NoteKeeperProviderContract.Notes;

import static com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import static com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String NOTE_ID = "com.chrissetiana.notekeeper.NOTE_ID";
    public static final String ORIGINAL_NOTE_ID = "com.chrissetiana.notekeeper.NOTE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.chrissetiana.notekeeper.NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.chrissetiana.notekeeper.NOTE_TEXT";
    public static final int ID_NOT_SET = -1;
    // public static final int SHOW_CAMERA = 1;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
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
    private Uri noteUri;
    private String NOTE_URI;
    private ModuleStatusView viewModuleStatus;

    public static void simulateLongRunningWork() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

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

            String noteUriStr = savedInstanceState.getString(NOTE_URI);
            noteUri = Uri.parse(noteUriStr);
        }

        textTitle = findViewById(R.id.text_title);
        textNote = findViewById(R.id.text_note);

        if (!isNewNote) {
            getLoaderManager().initLoader(LOADER_NOTES, null, this);

            viewModuleStatus = findViewById(R.id.module_status);

            loadModuleStatusValues();
        }
    }

    private void loadModuleStatusValues() {
        int totalNUmberOfModules = 11;
        int completedNumberOfModules = 7;
        boolean[] moduleStatus = new boolean[totalNUmberOfModules];

        for (int i = 0; i < completedNumberOfModules; i++) {
            moduleStatus[i] = true;
        }

        viewModuleStatus.setModuleStatus(moduleStatus);
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

    private void restoreOriginalStateValues(Bundle savedInstanceState) {
        originalNoteId = savedInstanceState.getString(ORIGINAL_NOTE_ID);
        originalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        originalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    private void saveOriginalStateValues() {
        if (isNewNote) {
            return;
        }

        originalNoteId = note.getCourse().getCourseId();
        originalNoteTitle = note.getTitle();
        originalNoteText = note.getText();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isCancelling) {
            if (isNewNote) {
                deleteNoteFromDatabase();
            } else {
                storePreviousStateValues();
            }
        } else {
            saveNote();
        }
    }

    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ? ";
        final String[] selectionArgs = {Integer.toString(noteId)};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
//                getContentResolver().delete(uriNote, null, null);

                return null;
            }
        };

        task.execute();
    }

    private void storePreviousStateValues() {
        CourseInfo course = DataManager.getInstance().getCourse(originalNoteId);
        note.setCourse(course);
        note.setTitle(originalNoteTitle);
        note.setText(originalNoteText);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(ORIGINAL_NOTE_ID, originalNoteId);
        outState.putString(ORIGINAL_NOTE_TITLE, originalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, originalNoteText);
        outState.putString(NOTE_URI, noteUri.toString());
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
        final String selection = NoteInfoEntry._ID + " = ? ";
        final String[] selectionArgs = {Integer.toString(noteId)};

        final ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);

                return null;
            }
        };

        task.execute();
    }

    private void displayNote() {
        String courseId = noteCursor.getString(cursorIdPos);
        String courseTitle = noteCursor.getString(cursorTitlePos);
        String courseText = noteCursor.getString(cursorTextPos);
        int courseIndex = getCourseIdIndex(courseId);

        spinnerCourses.setSelection(courseIndex);
        textTitle.setText(courseTitle);
        textNote.setText(courseText);

        CourseEventBroadcastHelper.sendEventBroadcast(this, courseId, "Editing Note");
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

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        noteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        isNewNote = noteId == ID_NOT_SET;

        if (isNewNote) {
            createNote();
        }

//        note = DataManager.getInstance().getNotes().get(noteId);
    }

    private void createNote() {
        AsyncTask<ContentValues, Integer, Uri> task = new AsyncTask<ContentValues, Integer, Uri>() {

            private ProgressBar progressBar;

            @Override
            protected void onPreExecute() {
                progressBar = findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(1);
            }

            @Override
            protected Uri doInBackground(ContentValues... params) {
                ContentValues values = params[0];
                Uri uri = getContentResolver().insert(Notes.CONTENT_URI, values);

                simulateLongRunningWork();
                publishProgress(2);

                simulateLongRunningWork();
                publishProgress(3);

                return uri;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue = values[0];
                progressBar.setProgress(progressValue);
            }

            @Override
            protected void onPostExecute(Uri uri) {
                noteUri = uri;
                displaySnackBar(noteUri.toString());
                progressBar.setVisibility(View.GONE);
            }
        };

        ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID, "");
        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");

        task.execute(values);
    }

    private void displaySnackBar(String s) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, s, Snackbar.LENGTH_SHORT);
        snackbar.show();
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
        } else if (id == R.id.action_set_reminder) {
            showReminderNotification();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showReminderNotification() {
        String noteTitle = textTitle.getText().toString().trim();
        String noteText = textNote.getText().toString().trim();
        int noteId = (int) ContentUris.parseId(noteUri);

        Intent intent = new Intent(this, NoteReminderReceiver.class);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, noteId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long currentTimeInMillis = SystemClock.elapsedRealtime();
        long ONE_HOUR = 60 * 60 * 1000;
        long TEN_SECONDS = 10 * 1000;
        long alarmTime = currentTimeInMillis + TEN_SECONDS;

        alarmManager.set(AlarmManager.ELAPSED_REALTIME, alarmTime, pendingIntent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;

        menuItem.setEnabled(noteId < lastNoteIndex);

        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();

        ++noteId;
        note = DataManager.getInstance().getNotes().get(noteId);

        saveOriginalStateValues();
        displayNote();
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

    private CursorLoader createLoaderCourses() {
        courseQueryFinished = false;
        Uri uri = Courses.CONTENT_URI;
        String[] columns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID};

        return new CursorLoader(this, uri, columns, null, null, Courses.COLUMN_COURSE_TITLE);
    }

    private CursorLoader createLoaderNotes() {
        notesQueryFinished = false;

        String[] columns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT};

        ContentUris.withAppendedId(Notes.CONTENT_URI, noteId);

        return new CursorLoader(this, noteUri, columns, null, null, null);
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

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SHOW_CAMERA && resultCode == RESULT_OK) {
            assert data != null;
            Bitmap thumbnail = data.getParcelableExtra("data");
        }
    }*/

    /*private void addImage(Uri photoFile) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile);
        startActivityForResult(intent, SHOW_CAMERA);
    }*/
}
