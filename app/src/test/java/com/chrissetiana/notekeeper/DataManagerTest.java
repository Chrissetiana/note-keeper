package com.chrissetiana.notekeeper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataManagerTest {

    @Test
    public void createNewNote() {
        DataManager instance = DataManager.getInstance();
        final CourseInfo course = instance.getCourse("android_sync");
        final String title = "Test Note Title";
        final String text = "This is the body of my test note";

        int noteIndex = instance.createNewNote();
        NoteInfo newNote = instance.getNotes().get(noteIndex);

        newNote.setCourse(course);
        newNote.setTitle(title);
        newNote.setText(text);

        NoteInfo compareNotes = instance.getNotes().get(noteIndex);

        assertEquals(course, compareNotes.getCourse());
        assertEquals(title, compareNotes.getTitle());
        assertEquals(text, compareNotes.getText());
    }
}