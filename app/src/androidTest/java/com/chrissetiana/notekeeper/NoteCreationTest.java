package com.chrissetiana.notekeeper;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {

    private static DataManager dataManager;

    @BeforeClass
    public static void classSetUp() {
        dataManager = DataManager.getInstance();
    }

    @Rule
    public ActivityTestRule<NoteListActivity> rule = new ActivityTestRule<>(NoteListActivity.class);

    @Test
    public void createNewNote() {
        final CourseInfo course = dataManager.getCourse("java_lang");
        final String noteTitle = "Test note title";
        final String noteText = "This is the body of our test note";

        onView(withId(R.id.fab)).perform(click());

        onView(withId(R.id.spinner_courses)).perform(click());

        onData(allOf(instanceOf(CourseInfo.class), equalTo(course)))
                .perform(click())
                .check(matches(withSpinnerText(containsString(course.getTitle()))));

        onView(withId(R.id.spinner_courses)).check(matches(withSpinnerText(
                containsString(course.getTitle()))));

        onView(withId(R.id.text_title))
                .perform(typeText(noteTitle))
                .check(matches(withText(containsString(noteTitle))));

        onView(withId(R.id.text_note))
                .perform(typeText(noteText), closeSoftKeyboard())
                .check(matches(withText(containsString(noteTitle))));

        onView(withId(R.id.text_note))
                .check(matches(withText(containsString(noteText))));

        pressBack();

        int noteIndex = dataManager.getNotes().size() - 1;
        NoteInfo note = dataManager.getNotes().get(noteIndex);

        assertEquals(course, note.getCourse());
        assertEquals(noteTitle, note.getTitle());
        assertEquals(noteText, note.getText());
    }
}