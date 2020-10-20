package com.example.notekeeper

import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.lang.Exception

class DataManagerTest {

    @Before
    fun setUp() {
        dm.notes.clear()
        dm.initializeExampleNotes()
    }

    @After
    fun tearDown() {
    }

    @Test
    @Throws(Exception::class)
    fun createNewNote() {
        val course = dm.getCourse("android_async")
        val title = "Test Note Title"
        val text = "This is the body text of my test note"

        val courseIndex = dm.createNewNote()
        val note = dm.notes[courseIndex]
        note.course = course
        note.title = title
        note.text = text

        val storedNote = dm.notes[courseIndex]
        assertEquals(note.course, storedNote.course)
        assertEquals(note.title, storedNote.title)
        assertEquals(note.text, storedNote.text)
    }

    @Test
    @Throws(Exception::class)
    fun findNoteTest() {
        val course = dm.getCourse("android_async")
        val title = "Test Note Title"
        val body1 = "This is the body text of my test note"
        val body2 = "This is the body text of my Second test note"

        val noteIndex1 = dm.createNewNote()
        val note1 = dm.notes[noteIndex1]
        note1.course = course
        note1.title = title
        note1.text = body1

        val noteIndex2 = dm.createNewNote()
        val note2 = dm.notes[noteIndex2]
        note2.course = course
        note2.title = title
        note2.text = body2

        val storedNoteIndex1 = dm.findNote(note1)
        assertEquals(noteIndex1, storedNoteIndex1)

        val storedNoteIndex2 = dm.findNote(note2)
        assertEquals(noteIndex2, storedNoteIndex2)
    }

    companion object {
        @JvmStatic
        private lateinit var dm: DataManager

        @BeforeClass
        @JvmStatic
        fun classSetup() {
            dm = DataManager.instance
        }
    }
}
