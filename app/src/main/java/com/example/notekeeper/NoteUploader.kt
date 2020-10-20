package com.example.notekeeper

import android.content.Context
import android.net.Uri
import android.util.Log

/**
 * Created by Jim.
 */

class NoteUploader(private val mContext: Context) {
    private val TAG = javaClass.simpleName
    val notesContract = NoteKeeperProviderContract.Companion.Notes()

    var isCanceled: Boolean = false
        private set

    fun cancel() {
        isCanceled = true
    }

    fun doUpload(dataUri: Uri) {
        val columns = arrayOf(notesContract.COLUMN_COURSE_ID, notesContract.COLUMN_NOTE_TITLE, notesContract.COLUMN_NOTE_TEXT)

        val cursor = mContext.contentResolver.query(dataUri, columns, null, null, null)
        val courseIdPos = cursor!!.getColumnIndex(notesContract.COLUMN_COURSE_ID)
        val noteTitlePos = cursor.getColumnIndex(notesContract.COLUMN_NOTE_TITLE)
        val noteTextPos = cursor.getColumnIndex(notesContract.COLUMN_NOTE_TEXT)

        Log.i(TAG, ">>>*** UPLOAD START - $dataUri ***<<<")
        isCanceled = false
        while (!isCanceled && cursor.moveToNext()) {
            val courseId = cursor.getString(courseIdPos)
            val noteTitle = cursor.getString(noteTitlePos)
            val noteText = cursor.getString(noteTextPos)

            if (noteTitle != "") {
                Log.i(TAG, ">>>Uploading Note<<< $courseId|$noteTitle|$noteText")
                simulateLongRunningWork()
            }
        }
        if (isCanceled)
            Log.i(TAG, ">>>*** UPLOAD !!CANCELED!! - $dataUri ***<<<")
        else
            Log.i(TAG, ">>>*** UPLOAD COMPLETE - $dataUri ***<<<")
        cursor.close()
    }

    private fun simulateLongRunningWork() {
        try {
            Thread.sleep(2000)
        } catch (ex: Exception) {
        }

    }
}
