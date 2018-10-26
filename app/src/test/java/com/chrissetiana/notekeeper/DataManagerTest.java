package com.chrissetiana.notekeeper;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataManagerTest {

    @Before
    public void setup() {
        DataManager dm = DataManager.getInstance();
        dm.getNotes().clear();
        dm.initializeExampleNotes();
    }

    @Test
    public void createNewNote() {
        final DataManager dm = DataManager.getInstance();
        final CourseInfo course = dm.getCourse("android_sync");
        final String title = "Test Note Title";
        final String text = "This is the body of my test note";

        int noteIndex = dm.createNewNote();

        NoteInfo newNote = dm.getNotes().get(noteIndex);
        newNote.setCourse(course);
        newNote.setTitle(title);
        newNote.setText(text);

        NoteInfo compareNotes = dm.getNotes().get(noteIndex);
        assertEquals(course, compareNotes.getCourse());
        assertEquals(title, compareNotes.getTitle());
        assertEquals(text, compareNotes.getText());
    }

    @Test
    public void findSimilarNotes() {
        final DataManager dm = DataManager.getInstance();
        final CourseInfo course = dm.getCourse("android_sync");
        final String title = "Test Note Title";
        final String text1 = "This is the body of my test note";
        final String text2 = "This is the body of my second test note";

        int noteIndex1 = dm.createNewNote();
        NoteInfo newNote1 = dm.getNotes().get(noteIndex1);
        newNote1.setCourse(course);
        newNote1.setTitle(title);
        newNote1.setText(text1);

        int noteIndex2 = dm.createNewNote();
        NoteInfo newNote2 = dm.getNotes().get(noteIndex2);
        newNote2.setCourse(course);
        newNote2.setTitle(title);
        newNote2.setText(text2);

        int foundIndex1 = dm.findNote(newNote1);
        assertEquals(noteIndex1, foundIndex1);

        int foundIndex2 = dm.findNote(newNote1);
        assertEquals(noteIndex2, foundIndex2);
    }
}