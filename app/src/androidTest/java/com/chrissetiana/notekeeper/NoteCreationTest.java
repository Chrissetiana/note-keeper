package com.chrissetiana.notekeeper;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {

    @Rule
    public ActivityTestRule<NoteListActivity> rule = new ActivityTestRule<>(NoteListActivity.class);

    @Test
    public void createNewNote() {
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.text_title)).perform(typeText("Test note title"));
        onView(withId(R.id.text_note)).perform(typeText("This is the body of our test note"));
        closeSoftKeyboard();
    }
}