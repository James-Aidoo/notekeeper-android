package com.example.notekeeper

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test

class NextThroughNotesTest {
    @Rule @JvmField
    val activityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun nextThroughNotes() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_notes))

        onView(withId(R.id.list_items)).perform(RecyclerViewActions.actionOnItemAtPosition<NoteRecyclerAdapter.ViewHolder>(0, click()))

        val notes = DataManager.instance.notes
        for (note in notes){
            onView(withId(R.id.spinner_courses)).check(matches(ViewMatchers.withSpinnerText(note.course!!.title)))
            onView(withId(R.id.note_title)).check(matches(ViewMatchers.withText(note.title)))
            onView(withId(R.id.note_text)).check(matches(ViewMatchers.withText(note.text)))

            if (notes.indexOf(note) < notes.size -1)
                onView(allOf(withId(R.id.action_next), isEnabled())).perform(click())
        }

        onView(withId(R.id.action_next)).check(matches(not(isEnabled())))
        pressBack()
    }
}