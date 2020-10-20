package com.example.notekeeper

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.*
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteCreationTest {
    @Rule @JvmField
    val noteListActivityRule = ActivityTestRule(NoteListActivity::class.java)

    @Test
    fun createNewNote() {
        val course = dm.getCourse("java_lang")
        val title = "Test Note Title"
        val text = "This is the body of the Test Note"

        onView(withId(R.id.fab)).perform(click())

        onView(withId(R.id.spinner_courses)).perform(click())
        onData(allOf(instanceOf(CourseInfo::class.java), equalTo(course))).perform(click())

        onView(withId(R.id.note_title)).perform(typeText(title))
        onView(withId(R.id.note_text)).perform(typeText(text), closeSoftKeyboard())

        pressBack()
    }

    companion object {
        lateinit var dm: DataManager

        @JvmStatic
        @BeforeClass
        fun classSetup(){
            dm = DataManager.instance
        }
    }
}