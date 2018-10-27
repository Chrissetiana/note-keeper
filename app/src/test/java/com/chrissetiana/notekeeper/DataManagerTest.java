package com.chrissetiana.notekeeper;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataManagerTest {
    static DataManager dataManager;

    @BeforeClass
    public static void classSetup() {
        dataManager = DataManager.getInstance();
    }

    @Before
    public void setup() {
        dataManager.getNotes().clear();
        dataManager.initializeExampleNotes();
    }

    @Test
    public void createNewNote() {
        final CourseInfo course = dataManager.getCourse("android_sync");
        final String title = "Test Note Title";
        final String text = "This is the body of my test note";

        int noteIndex = dataManager.createNewNote();

        NoteInfo newNote = dataManager.getNotes().get(noteIndex);
        newNote.setCourse(course);
        newNote.setTitle(title);
        newNote.setText(text);

        NoteInfo compareNotes = dataManager.getNotes().get(noteIndex);
        assertEquals(course, compareNotes.getCourse());
        assertEquals(title, compareNotes.getTitle());
        assertEquals(text, compareNotes.getText());
    }

    @Test
    public void findSimilarNotes() {
        final CourseInfo course = dataManager.getCourse("android_sync");
        final String title = "Test Note Title";
        final String text1 = "This is the body of my test note";
        final String text2 = "This is the body of my second test note";

        int noteIndex1 = dataManager.createNewNote();
        NoteInfo newNote1 = dataManager.getNotes().get(noteIndex1);
        newNote1.setCourse(course);
        newNote1.setTitle(title);
        newNote1.setText(text1);

        int noteIndex2 = dataManager.createNewNote();
        NoteInfo newNote2 = dataManager.getNotes().get(noteIndex2);
        newNote2.setCourse(course);
        newNote2.setTitle(title);
        newNote2.setText(text2);

        int foundIndex1 = dataManager.findNote(newNote1);
        assertEquals(noteIndex1, foundIndex1);

        int foundIndex2 = dataManager.findNote(newNote1);
        assertEquals(noteIndex2, foundIndex2);
    }

    @Test
    public void createNewNoteOneStepCreation() {
        final CourseInfo course = dataManager.getCourse("android_async");
        final String title = "Test Note Title";
        final String text = "This is the body of my test note";

        int index = dataManager.createNewNote(course, title, text);
        NoteInfo compareNote = dataManager.getNotes().get(index);
        assertEquals(course, compareNote.getCourse());
        assertEquals(title, compareNote.getTitle());
        assertEquals(text, compareNote.getText());
    }
}