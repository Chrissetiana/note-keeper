package com.chrissetiana.notekeeper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager ourInstance = null;

    private final List<CourseInfo> courses = new ArrayList<>();
    private List<NoteInfo> notes = new ArrayList<>();

    private DataManager() {
    }

    public static DataManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new DataManager();
//            ourInstance.initializeCourses();
//            ourInstance.initializeExampleNotes();
        }

        return ourInstance;
    }

    static void loadFromDatabase(NoteKeeperDatabaseHelper helper) {
        SQLiteDatabase db = helper.getReadableDatabase();

        final String[] columnsCourse = {CourseInfoEntry.COLUMN_COURSE_ID, CourseInfoEntry.COLUMN_COURSE_TITLE};
        final Cursor cursorCourses = db.query(CourseInfoEntry.TABLE_NAME, columnsCourse, null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE + " DESC");
        loadCoursesFromDatabase(cursorCourses);

        final String[] columnsNotes = {NoteInfoEntry.COLUMN_NOTE_TITLE, NoteInfoEntry.COLUMN_NOTE_TEXT, NoteInfoEntry.COLUMN_COURSE_ID, NoteInfoEntry._ID};
        String orderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
        final Cursor cursorNotes = db.query(NoteInfoEntry.TABLE_NAME, columnsNotes, null, null, null, null, orderBy);
        loadNotesFromDatabase(cursorNotes);
    }

    private static void loadNotesFromDatabase(Cursor cursor) {
        int noteTitlePos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        int noteTextPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        int courseIdPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        int idPos = cursor.getColumnIndex(NoteInfoEntry._ID);

        DataManager dm = getInstance();
        dm.notes.clear();

        while (cursor.moveToNext()) {
            String noteTitle = cursor.getString(noteTitlePos);
            String noteText = cursor.getString(noteTextPos);
            String courseId = cursor.getString(courseIdPos);
            int id = cursor.getInt(idPos);

            CourseInfo noteCourse = dm.getCourse(courseId);
            NoteInfo noteInfo = new NoteInfo(id, noteCourse, noteTitle, noteText);
            dm.notes.add(noteInfo);
        }

        cursor.close();
    }

    private static void loadCoursesFromDatabase(Cursor cursor) {
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseTitlePos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);

        DataManager dm = getInstance();
        dm.courses.clear();

        while (cursor.moveToNext()) {
            String courseId = cursor.getString(courseIdPos);
            String courseTitle = cursor.getString(courseTitlePos);

            CourseInfo courseInfo = new CourseInfo(courseId, courseTitle, null);
            dm.courses.add(courseInfo);
        }

        cursor.close();
    }

    public String getCurrentUserName() {
        return "Tiana Aiyemo";
    }

    public String getCurrentUserEmail() {
        return "chrissetiana@gmail.com";
    }

    List<NoteInfo> getNotes() {
        return notes;
    }

    int createNewNote() {
        NoteInfo note = new NoteInfo(0, null, null, null);
        notes.add(note);
        return notes.size() - 1;
    }

    int findNote(NoteInfo note) {
        for (int i = 0; i < notes.size(); i++) {
            if (note.equals(notes.get(i))) {
                return i;
            }
        }

        return -1;
    }

    void removeNote(int index) {
        notes.remove(index);
    }

    List<CourseInfo> getCourses() {
        return courses;
    }

    CourseInfo getCourse(String id) {
        for (CourseInfo course : courses) {
            if (id.equals(course.getCourseId())) {
                return course;
            }
        }

        return null;
    }

    public List<NoteInfo> getNotes(CourseInfo course) {
        ArrayList<NoteInfo> notes = new ArrayList<>();

        for (NoteInfo note : notes) {
            if (course.equals(note.getCourse()))
                notes.add(note);
        }

        return notes;
    }

    public int getNoteCount(CourseInfo course) {
        int count = 0;

        for (NoteInfo note : notes) {
            if (course.equals(note.getCourse()))
                count++;
        }

        return count;
    }

    private void initializeCourses() {
        courses.add(initializeCourse1());
        courses.add(initializeCourse2());
        courses.add(initializeCourse3());
        courses.add(initializeCourse4());
    }
/*

    public void initializeExampleNotes() {
        final DataManager dm = getInstance();

        CourseInfo course = dm.getCourse("android_intents");
        course.getModule("android_intents_m01").setComplete(true);
        course.getModule("android_intents_m02").setComplete(true);
        course.getModule("android_intents_m03").setComplete(true);

        notes.add(new NoteInfo(id, course, "Dynamic intent resolution", "Wow, intents allow components to be resolved at runtime"));
        notes.add(new NoteInfo(id, course, "Delegating intents", "PendingIntents are powerful; they delegate much more than just a component invocation"));

        course = dm.getCourse("android_async");
        course.getModule("android_async_m01").setComplete(true);
        course.getModule("android_async_m02").setComplete(true);

        notes.add(new NoteInfo(id, course, "Service default threads", "Did you know that by default an Android Service will tie up the UI thread?"));
        notes.add(new NoteInfo(id, course, "Long running operations", "Foreground Services can be tied to a notification icon"));

        course = dm.getCourse("java_lang");
        course.getModule("java_lang_m01").setComplete(true);
        course.getModule("java_lang_m02").setComplete(true);
        course.getModule("java_lang_m03").setComplete(true);
        course.getModule("java_lang_m04").setComplete(true);
        course.getModule("java_lang_m05").setComplete(true);
        course.getModule("java_lang_m06").setComplete(true);
        course.getModule("java_lang_m07").setComplete(true);

        notes.add(new NoteInfo(id, course, "Parameters", "Leverage variable-length parameter lists"));
        notes.add(new NoteInfo(id, course, "Anonymous classes", "Anonymous classes simplify implementing one-use types"));

        course = dm.getCourse("java_core");
        course.getModule("java_core_m01").setComplete(true);
        course.getModule("java_core_m02").setComplete(true);
        course.getModule("java_core_m03").setComplete(true);

        notes.add(new NoteInfo(id, course, "Compiler options", "The -jar option isn't compatible with with the -cp option"));
        notes.add(new NoteInfo(id, course, "Serialization", "Remember to include SerialVersionUID to assure version compatibility"));
    }
*/

    private CourseInfo initializeCourse1() {
        List<ModuleInfo> modules = new ArrayList<>();

        modules.add(new ModuleInfo("android_intents_m01", "Android Late Binding and Intents"));
        modules.add(new ModuleInfo("android_intents_m02", "Component activation with intents"));
        modules.add(new ModuleInfo("android_intents_m03", "Delegation and Callbacks through PendingIntents"));
        modules.add(new ModuleInfo("android_intents_m04", "IntentFilter data tests"));
        modules.add(new ModuleInfo("android_intents_m05", "Working with Platform Features Through Intents"));

        return new CourseInfo("android_intents", "Android Programming with Intents", modules);
    }

    private CourseInfo initializeCourse2() {
        List<ModuleInfo> modules = new ArrayList<>();

        modules.add(new ModuleInfo("android_async_m01", "Challenges to a responsive user experience"));
        modules.add(new ModuleInfo("android_async_m02", "Implementing long-running operations as a service"));
        modules.add(new ModuleInfo("android_async_m03", "Service lifecycle management"));
        modules.add(new ModuleInfo("android_async_m04", "Interacting with services"));

        return new CourseInfo("android_async", "Android Async Programming and Services", modules);
    }

    private CourseInfo initializeCourse3() {
        List<ModuleInfo> modules = new ArrayList<>();

        modules.add(new ModuleInfo("java_lang_m01", "Introduction and Setting up Your Environment"));
        modules.add(new ModuleInfo("java_lang_m02", "Creating a Simple App"));
        modules.add(new ModuleInfo("java_lang_m03", "Variables, Data Types, and Math Operators"));
        modules.add(new ModuleInfo("java_lang_m04", "Conditional Logic, Looping, and Arrays"));
        modules.add(new ModuleInfo("java_lang_m05", "Representing Complex Types with Classes"));
        modules.add(new ModuleInfo("java_lang_m06", "Class Initializers and Constructors"));
        modules.add(new ModuleInfo("java_lang_m07", "A Closer Look at Parameters"));
        modules.add(new ModuleInfo("java_lang_m08", "Class Inheritance"));
        modules.add(new ModuleInfo("java_lang_m09", "More About Data Types"));
        modules.add(new ModuleInfo("java_lang_m10", "Exceptions and Error Handling"));
        modules.add(new ModuleInfo("java_lang_m11", "Working with Packages"));
        modules.add(new ModuleInfo("java_lang_m12", "Creating Abstract Relationships with Interfaces"));
        modules.add(new ModuleInfo("java_lang_m13", "Static Members, Nested Types, and Anonymous Classes"));

        return new CourseInfo("java_lang", "Java Fundamentals: The Java Language", modules);
    }

    private CourseInfo initializeCourse4() {
        List<ModuleInfo> modules = new ArrayList<>();

        modules.add(new ModuleInfo("java_core_m01", "Introduction"));
        modules.add(new ModuleInfo("java_core_m02", "Input and Output with Streams and Files"));
        modules.add(new ModuleInfo("java_core_m03", "String Formatting and Regular Expressions"));
        modules.add(new ModuleInfo("java_core_m04", "Working with Collections"));
        modules.add(new ModuleInfo("java_core_m05", "Controlling App Execution and Environment"));
        modules.add(new ModuleInfo("java_core_m06", "Capturing Application Activity with the Java Log System"));
        modules.add(new ModuleInfo("java_core_m07", "Multithreading and Concurrency"));
        modules.add(new ModuleInfo("java_core_m08", "Runtime Type Information and Reflection"));
        modules.add(new ModuleInfo("java_core_m09", "Adding Type Metadata with Annotations"));
        modules.add(new ModuleInfo("java_core_m10", "Persisting Objects with Serialization"));

        return new CourseInfo("java_core", "Java Fundamentals: The Core Platform", modules);
    }

    int createNewNote(CourseInfo course, String noteTitle, String noteText) {
        int index = createNewNote();

        NoteInfo note = getNotes().get(index);
        note.setCourse(course);
        note.setTitle(noteTitle);
        note.setText(noteText);

        return index;
    }
}
