package com.example.notekeeper

import android.content.Context
import android.util.Log

/**
 * Created by Jim.
 */

object NoteBackup {
    val ALL_COURSES = "ALL_COURSES"
    private val TAG = NoteBackup::class.java.simpleName
    val noteContract = NoteKeeperProviderContract.Companion.Notes()

    fun doBackup(context: Context, backupCourseId: String) {
        val columns = arrayOf(noteContract.COLUMN_COURSE_ID, noteContract.COLUMN_NOTE_TITLE, noteContract.COLUMN_NOTE_TEXT)

        var selection: String? = null
        var selectionArgs: Array<String>? = null
        if (backupCourseId != ALL_COURSES) {
            selection = noteContract.COLUMN_COURSE_ID + " = ?"
            selectionArgs = arrayOf(backupCourseId)
        }

        val cursor = context.contentResolver.query(noteContract.CONTENT_URI, columns, selection, selectionArgs, null)
        val courseIdPos = cursor!!.getColumnIndex(noteContract.COLUMN_COURSE_ID)
        val noteTitlePos = cursor.getColumnIndex(noteContract.COLUMN_NOTE_TITLE)
        val noteTextPos = cursor.getColumnIndex(noteContract.COLUMN_NOTE_TEXT)

        Log.i(TAG, ">>>***   BACKUP START - Thread: " + Thread.currentThread().id + "   ***<<<")
        while (cursor.moveToNext()) {
            val courseId = cursor.getString(courseIdPos)
            val noteTitle = cursor.getString(noteTitlePos)
            val noteText = cursor.getString(noteTextPos)

            if (noteTitle != "") {
                Log.i(TAG, ">>>Backing Up Note<<< $courseId|$noteTitle|$noteText")
                simulateLongRunningWork()
            }
        }
        Log.i(TAG, ">>>***   BACKUP COMPLETE   ***<<<")
        cursor.close()
    }


    private fun simulateLongRunningWork() {
        try {
            Thread.sleep(1000)
        } catch (ex: Exception) {
        }
    }
}