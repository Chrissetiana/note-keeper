package com.chrissetiana.notekeeper;

import android.content.Intent;
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

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_POSITION = "com.chrissetiana.notekeeper.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    public static final int SHOW_CAMERA = 1;
    private NoteInfo note;
    private boolean isNewNote;
    private Spinner spinnerText;
    private EditText textTitle;
    private EditText textNote;
    private int newPosition;
    private boolean isCancelling;
    private String originalNoteId;
    private String originalNoteTitle;
    private String originalNoteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinnerText = findViewById(R.id.spinner_topic);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);

        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerText.setAdapter(adapterCourses);

        readDisplayStateValues();
        saveOriginalStateValues();

        textTitle = findViewById(R.id.text_title);
        textNote = findViewById(R.id.text_note);

        if (!isNewNote) {
            displayNote(spinnerText, textTitle, textNote);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_send_email) {
            sendEmail();
            return true;
        } else if (id == R.id.action_add_image) {
//            addImage();
            return true;
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
                DataManager.getInstance().removeNote(newPosition);
            } else {
                storePreviousStateValues();
            }
        } else {
            saveNote();
        }
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);

        isNewNote = position == POSITION_NOT_SET;

        if (isNewNote) {
            createNote();
        } else {
            note = DataManager.getInstance().getNotes().get(position);
        }
    }

    private void saveOriginalStateValues() {
        if (isNewNote) {
            return;
        }

        originalNoteId = note.getCourse().getNoteId();
        originalNoteTitle = note.getTitle();
        originalNoteText = note.getText();
    }

    private void storePreviousStateValues() {
        CourseInfo course = DataManager.getInstance().getCourse(originalNoteId);
        note.setCourse(course);
        note.setTitle(originalNoteTitle);
        note.setText(originalNoteText);
    }

    private void displayNote(Spinner spinnerText, EditText textTitle, EditText textNote) {
        List<CourseInfo> list = DataManager.getInstance().getCourses();
        int courseIndex = list.indexOf(note.getCourse());

        spinnerText.setSelection(courseIndex);
        textTitle.setText(note.getTitle());
        textNote.setText(note.getText());
    }

    private void createNote() {
        DataManager dataManager = DataManager.getInstance();
        newPosition = dataManager.createNewNote();
        note = dataManager.getNotes().get(newPosition);
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
        String message = "Checkout what I learned in the PluralSight course \"" + course.getNoteTitle() + "\"\n" + note;

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
}
