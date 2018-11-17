package com.chrissetiana.notekeeper;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

import static com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_POSITION = "com.chrissetiana.notekeeper.NOTE_POSITION";
    public static final String ORIGINAL_NOTE_ID = "com.chrissetiana.notekeeper.NOTE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.chrissetiana.notekeeper.NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.chrissetiana.notekeeper.NOTE_TEXT";
    public static final int POSITION_NOT_SET = -1;
    public static final int SHOW_CAMERA = 1;
    private NoteInfo note;
    private boolean isNewNote;
    private Spinner spinnerText;
    private EditText textTitle;
    private EditText textNote;
    private int notePosition;
    private boolean isCancelling;
    private String originalNoteId;
    private String originalNoteTitle;
    private String originalNoteText;
    private NoteKeeperDatabaseHelper databaseHelper;
    private Cursor noteCursor;
    private int cursorIdPos;
    private int cursorTitlePos;
    private int cursorTextPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseHelper = new NoteKeeperDatabaseHelper(this);

        spinnerText = findViewById(R.id.spinner_courses);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);

        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerText.setAdapter(adapterCourses);

        readDisplayStateValues();
        if (savedInstanceState == null) {
            saveOriginalStateValues();
        } else {
            restoreOriginalStateValues(savedInstanceState);
        }

        textTitle = findViewById(R.id.text_title);
        textNote = findViewById(R.id.text_note);

        if (!isNewNote) {
            loadNoteData();
        }
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

        String selection = NoteInfoEntry.COLUMN_COURSE_ID + " = ? AND " + NoteInfoEntry.COLUMN_NOTE_TITLE + " LIKE ? ";
        String[] selectionArgs = {courseId, titleStart + "%"};
        String[] columns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };

        noteCursor = db.query(
                NoteInfoEntry.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

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

        menuItem.setEnabled(notePosition < lastNoteIndex);

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
            Bitmap thumbnail = data.getParcelableExtra("data");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isCancelling) {
            if (isNewNote) {
                DataManager.getInstance().removeNote(notePosition);
            } else {
                storePreviousStateValues();
            }
        } else {
            saveNote();
        }
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        notePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);

        isNewNote = notePosition == POSITION_NOT_SET;

        if (isNewNote) {
            createNote();
        }
        note = DataManager.getInstance().getNotes().get(notePosition);
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

        List<CourseInfo> list = DataManager.getInstance().getCourses();
        CourseInfo courseInfo = DataManager.getInstance().getCourse(courseId);
        int courseIndex = list.indexOf(courseInfo);
        spinnerText.setSelection(courseIndex);

        textTitle.setText(courseTitle);
        textNote.setText(courseText);
    }

    private void createNote() {
        DataManager dataManager = DataManager.getInstance();
        notePosition = dataManager.createNewNote();
    }

    private void saveNote() {
        note.setCourse((CourseInfo) spinnerText.getSelectedItem());
        note.setTitle(textTitle.getText().toString());
        note.setText(textNote.getText().toString());
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) spinnerText.getSelectedItem();

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

        ++notePosition;
        note = DataManager.getInstance().getNotes().get(notePosition);

        saveOriginalStateValues();
        displayNote();
    }
}
