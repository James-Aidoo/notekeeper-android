package com.example.notekeeper

import android.provider.BaseColumns

class NoteKeeperDatabaseContract : BaseColumns {
    companion object {
        class CourseInfoEntry {
            val TABLE_NAME = "course_info"
            val COLUMN_COURSE_ID = "course_id"
            val COLUMN_COURSE_TITLE = "course_title"
            val _ID = "_id"

            fun getQColumn(columnName: String) : String {
                return """$TABLE_NAME.$columnName"""
            }

            val INDEX1 = TABLE_NAME + "_index1"
            val SQL_CREATE_INDEX = "CREATE INDEX " + INDEX1 + " ON " + TABLE_NAME +
                                "(" + COLUMN_COURSE_TITLE + ")"

            val SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY," +
                        COLUMN_COURSE_ID + " TEXT UNIQUE NOT NULL," +
                        COLUMN_COURSE_TITLE + " TEXT NOT NULL)"
        }

        class NoteInfoEntry {
            val TABLE_NAME = "note_info"
            val COLUMN_NOTE_TITLE = "note_title"
            val COLUMN_NOTE_TEXT = "note_text"
            val COLUMN_COURSE_ID = "course_id"
            val _ID = "_id"

            fun getQColumn(columnName: String) : String {
                return """$TABLE_NAME.$columnName"""
            }

            val INDEX1 = TABLE_NAME + "_index1"
            val SQL_CREATE_INDEX = "CREATE INDEX " + INDEX1 + " ON " + TABLE_NAME +
                    "(" + COLUMN_NOTE_TITLE + ")"

            val SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NOTE_TITLE + " TEXT NOT NULL," +
                        COLUMN_NOTE_TEXT + " TEXT," +
                        COLUMN_COURSE_ID + " TEXT NOT NULL)"
        }
    }
}