package com.chrissetiana.notekeeper;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager ourInstance = null;
    private List<CourseInfo> courses = new ArrayList<>();
    private List<NoteInfo> notes = new ArrayList<>();

    public static DataManager getInstance() {
        if(ourInstance == null) {
            ourInstance = new DataManager();
            ourInstance.initializeCourse();
            ourInstance.initializeExampleNotes();
        }

        return ourInstance;
    }

    public String getCurrentUserName() {
        return "Tiana Aiyemo";
    }

    public String getCurrentUserEmail() {
        return "chrissetiana@gmail.com";
    }

    public List<NoteInfo> getNotes() {
        return notes;
    }

    public int createNewNote() {
        NoteInfo note = new NoteInfo(null, null, null);
        notes.add(note);
        return notes.size() - 1;
    }

    public int findNote(NoteInfo note) {
        for (int i = 0; i < notes.size(); i++) {
            if (note.equals(notes.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public void removeNote(int index) {
    notes.remove(index);
    }

    public List<CourseInfo> getCourses() {
        return courses;
    }

    public CourseInfo getCourse(String id) {
        for (CourseInfo course : courses) {
            if (id.equals(course.getNoteId())) {
                return course;
            }
        }

        return null;
    }
}
