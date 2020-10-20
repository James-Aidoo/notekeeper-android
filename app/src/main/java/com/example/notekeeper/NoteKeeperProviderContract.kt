package com.example.notekeeper

import android.net.Uri
import android.provider.BaseColumns

class NoteKeeperProviderContract {

    private interface CoursesIdColumn {
        val COLUMN_COURSE_ID: String
            get() = "course_id"
    }

    private interface CoursesColumns {
        val COLUMN_COURSE_TITLE: String
            get() = "course_title"
    }

    private interface NotesColumns {
        val COLUMN_NOTE_TITLE: String
            get() = "note_title"
        val COLUMN_NOTE_TEXT: String
            get() = "note_text"
    }

    companion object {
        val AUTHORITY = "com.example.notekeeper.provider"
        val AUTHORITY_URI: Uri = Uri.parse("content://$AUTHORITY")

        class Courses : BaseColumns, CoursesColumns, CoursesIdColumn {
            val PATH = "courses"
            val CONTENT_URI: Uri = Uri.withAppendedPath(AUTHORITY_URI, PATH)
            val _ID = "_id"
        }

        class Notes : BaseColumns, NotesColumns, CoursesIdColumn, CoursesColumns {
            val PATH = "notes"
            val CONTENT_URI: Uri = Uri.withAppendedPath(AUTHORITY_URI, PATH)
            val _ID = "_id"
            val PATH_EXPANDED = "notes_expanded"
            val EXPANDED_CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH_EXPANDED)
        }
    }
}