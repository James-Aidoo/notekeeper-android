package com.example.notekeeper

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.BaseColumns

class NoteKeeperProvider : ContentProvider() {

    var dbHelper: NoteKeeperOpenHelper? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = dbHelper!!.writableDatabase
        return when (uriMatcher.match(uri)){
            COURSES -> {
                db.delete(courseInfoEntry.TABLE_NAME, selection, selectionArgs)
            }
            NOTES -> {
                db.delete(noteInfoEntry.TABLE_NAME, selection, selectionArgs)
            }
            NOTES_ROW -> {
                db.delete(noteInfoEntry.TABLE_NAME, selection, selectionArgs)
            }
            else -> -1
        }
    }

    override fun getType(uri: Uri): String? {
        //var mimeType: String? = null
        return when(uriMatcher.match(uri)){
            COURSES -> """${ContentResolver.CURSOR_DIR_BASE_TYPE}/$MIME_VENDOR_TYPE${coursesContract.PATH}"""
            NOTES -> """${ContentResolver.CURSOR_DIR_BASE_TYPE}/$MIME_VENDOR_TYPE${notesContract.PATH}"""
            NOTES_EXPANDED -> """${ContentResolver.CURSOR_DIR_BASE_TYPE}/$MIME_VENDOR_TYPE${notesContract.PATH_EXPANDED}"""
            NOTES_ROW -> """${ContentResolver.CURSOR_ITEM_BASE_TYPE}/$MIME_VENDOR_TYPE${notesContract.PATH}"""
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper!!.writableDatabase
        var rowId: Long
        var rowUri: Uri? = null

        when (uriMatcher.match(uri)){
            COURSES -> {
                rowId = db.insert(courseInfoEntry.TABLE_NAME, null, values)
                rowUri = ContentUris.withAppendedId(coursesContract.CONTENT_URI, rowId)
            }
            NOTES -> {
                rowId = db.insert(noteInfoEntry.TABLE_NAME, null, values)
                rowUri = ContentUris.withAppendedId(notesContract.CONTENT_URI, rowId)
            }
            NOTES_EXPANDED -> {
                throw Exception("This is a readonly entry")
            }
        }
        return rowUri
    }

    override fun onCreate(): Boolean {
        dbHelper = NoteKeeperOpenHelper(context)
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val db = dbHelper!!.readableDatabase

        return when (uriMatcher.match(uri)){
            COURSES -> {
                db.query(courseInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            }
            NOTES -> {
                db.query(noteInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            }
            NOTES_EXPANDED -> notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder)
            NOTES_ROW -> {
                val rowId = ContentUris.parseId(uri)
                val rowSelection = noteInfoEntry._ID + " = ?"
                val rowSelectionArgs = arrayOf(rowId.toString())

                db.query(noteInfoEntry.TABLE_NAME, projection, rowSelection, rowSelectionArgs, null, null, null)
            }
            else -> null
        }
    }

    private fun notesExpandedQuery(
        db: SQLiteDatabase?,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val columns = arrayOfNulls<String>(projection!!.size)

        for (idx in projection!!.indices){
            columns[idx] = if (projection[idx] == BaseColumns._ID || projection[idx] == notesContract.COLUMN_COURSE_ID){
                noteInfoEntry.getQColumn(projection[idx])
            }
            else projection[idx]
        }

        val tableWithJoin = """${noteInfoEntry.TABLE_NAME} JOIN ${courseInfoEntry.TABLE_NAME} ON 
            |${noteInfoEntry.getQColumn(noteInfoEntry.COLUMN_COURSE_ID)} = ${courseInfoEntry.getQColumn(courseInfoEntry.COLUMN_COURSE_ID)}
        """.trimMargin()

        return db!!.query(tableWithJoin, columns, selection, selectionArgs, null, null, sortOrder)
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        TODO("Implement this to handle requests to update one or more rows.")
    }

    companion object {
        val MIME_VENDOR_TYPE = """vnd.${NoteKeeperProviderContract.AUTHORITY}."""

        val courseInfoEntry = NoteKeeperDatabaseContract.Companion.CourseInfoEntry()
        val noteInfoEntry = NoteKeeperDatabaseContract.Companion.NoteInfoEntry()

        val coursesContract = NoteKeeperProviderContract.Companion.Courses()
        val notesContract = NoteKeeperProviderContract.Companion.Notes()

        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        val COURSES = 0
        val NOTES = 1
        val NOTES_EXPANDED = 2
        val NOTES_ROW = 3

        init {
            uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, coursesContract.PATH, COURSES)
            uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, notesContract.PATH, NOTES)
            uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, notesContract.PATH_EXPANDED, NOTES_EXPANDED)
            uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, notesContract.PATH + "/#", NOTES_ROW)
        }
    }
}
