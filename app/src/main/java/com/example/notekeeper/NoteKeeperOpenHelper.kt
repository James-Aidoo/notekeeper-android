package com.example.notekeeper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NoteKeeperOpenHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(courseTableHelper.SQL_CREATE_TABLE)
        db.execSQL(noteTableHelper.SQL_CREATE_TABLE)
        db.execSQL(courseTableHelper.SQL_CREATE_INDEX)
        db.execSQL(noteTableHelper.SQL_CREATE_INDEX)

        val dbWorker = DatabaseDataWorker(db)
        dbWorker.insertCourses()
        dbWorker.insertSampleNotes()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2){
            db!!.execSQL(courseTableHelper.SQL_CREATE_INDEX)
            db.execSQL(noteTableHelper.SQL_CREATE_INDEX)
        }
    }

    companion object {
        val DATABASE_NAME = "NoteKeeper.db"
        val DATABASE_VERSION = 2

        val courseTableHelper = NoteKeeperDatabaseContract.Companion.CourseInfoEntry()
        val noteTableHelper = NoteKeeperDatabaseContract.Companion.NoteInfoEntry()
    }
}