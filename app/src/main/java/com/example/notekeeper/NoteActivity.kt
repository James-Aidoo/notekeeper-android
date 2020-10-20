package com.example.notekeeper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_note.*
import kotlinx.android.synthetic.main.content_note.*


const val NOTE_ID = "com.example.notekeeper.NOTE_INFO"
const val ORIGINAL_NOTE_COURSE_ID = "com.example.notekeeper.NOTE_COURSE"
const val ORIGINAL_NOTE_TITLE = "com.example.notekeeper.NOTE_TITLE"
const val ORIGINAL_NOTE_TEXT = "com.example.notekeeper.NOTE_TEXT"
const val NOTE_URI = "com.example.notekeeper.NOTE_URI"
const val LOADER_NOTE = 0

class NoteActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private var note: NoteInfo = NoteInfo(null, "", "")
    var noteUri: Uri? = null
    private var isNewNote: Boolean = true
    private var isCancelling: Boolean = false
    private val TAG = javaClass.simpleName
    private var dbHelper: NoteKeeperOpenHelper? = null
    var cursor: Cursor? = null
    private var spinnerCourses: Spinner? = null
    private var textNoteTitle: EditText? = null
    private var textNoteText: EditText? = null
    private var adapterCourses: SimpleCursorAdapter? = null
    private var viewModuleStatus: ModuleStatusView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        setSupportActionBar(toolbar)

        spinnerCourses = findViewById<Spinner>(R.id.spinner_courses)
        textNoteTitle = findViewById<EditText>(R.id.note_title)
        textNoteText = findViewById<EditText>(R.id.note_text)
        viewModuleStatus = findViewById<ModuleStatusView>(R.id.module_status)

        dbHelper = NoteKeeperOpenHelper(this)

        adapterCourses = SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                        arrayOf(courseInfoEntry.COLUMN_COURSE_TITLE), intArrayOf(android.R.id.text1), 0)
        adapterCourses!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourses!!.adapter = adapterCourses

        LoaderManager.getInstance(this).initLoader(LOADER_COURSES, null, this)

        readDisplayStateValues()
        if (savedInstanceState == null) {
            saveOriginalValues()
        }
        else {
            restoreSavedInstance(savedInstanceState)
//            noteUri = Uri.parse(savedInstanceState.getString(NOTE_URI))
            //noteID = savedInstanceState.getInt(NOTE_ID)
        }
        if (!isNewNote) {
            LoaderManager.getInstance(this).restartLoader(LOADER_NOTE, null, this)
        }
        loadModuleStatus()
    }

    private fun loadModuleStatus() {
        val totalNumberOfModules = 11
        val numberOfCompletedModules = 7
        val moduleStatus = Array(totalNumberOfModules) {false}
        for (moduleIndex in 0 until numberOfCompletedModules)
            moduleStatus[moduleIndex] = true
        if (viewModuleStatus == null)
            viewModuleStatus = findViewById(R.id.module_status)

        module_status.moduleStatus = moduleStatus
    }

    override fun onDestroy() {
        dbHelper!!.close()
        super.onDestroy()
    }

    private fun restoreSavedInstance(savedInstanceState: Bundle) {
        originalNoteCourseId = savedInstanceState[ORIGINAL_NOTE_COURSE_ID] as String?
        originalNoteTitle = savedInstanceState[ORIGINAL_NOTE_TITLE] as String?
        originalNoteText = savedInstanceState[ORIGINAL_NOTE_TEXT] as String?

        Log.i(TAG, "Instance State Restored")
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)

        outState?.putString(ORIGINAL_NOTE_COURSE_ID, originalNoteCourseId)
        outState?.putString(ORIGINAL_NOTE_TITLE, originalNoteTitle)
        outState?.putString(ORIGINAL_NOTE_TEXT, originalNoteText)

        outState?.putString(NOTE_URI, noteUri.toString())
        outState?.putInt(NOTE_ID, noteID)
        Log.i(TAG, "Instance State Saved")
    }

    private fun saveOriginalValues() {
        if (isNewNote)
            return
        originalNoteCourseId = note.course?.courseId
        originalNoteTitle = note.title!!
        originalNoteText = note.text!!
    }

    override fun onPause() {
        super.onPause()
        if (isCancelling)
            if (isNewNote)
                deleteNoteFromDatabase()
            else {
                //restoreOriginalValues()
            }
        else {
            saveNote()
        }
        Log.d(TAG, "onPause")
    }

    private fun deleteNoteFromDatabase() {
        val selection = noteInfoEntry._ID + " = ?"
        val selectionArgs = arrayOf(noteID.toString())

        val task = MyAsyncTask(noteUri, contentResolver, selection, selectionArgs)
        task.execute()
    }

    private fun restoreOriginalValues() {
        note.course = DataManager.instance.getCourse(originalNoteCourseId!!)
        note.title = originalNoteTitle
        note.text = originalNoteText
    }

    private fun saveNote() {
        val courseId = getCourseId()
        val title = textNoteTitle!!.text.toString()
        val text = textNoteText!!.text.toString()

        saveNoteToDatabase(courseId, title, text)
    }

    private fun getCourseId(): String {
        val position = spinnerCourses!!.selectedItemPosition
        val cursor = adapterCourses!!.cursor

        cursor.moveToPosition(position)
        val courseIdPos = cursor.getColumnIndex(noteInfoEntry.COLUMN_COURSE_ID)
        return cursor.getString(courseIdPos)
    }

    private fun saveNoteToDatabase(courseId: String, noteTitle: String, noteText: String){
        val selection = noteInfoEntry._ID + " = ?"
        val selectionArgs = arrayOf(noteID.toString())

        val values = ContentValues()
        values.put(noteInfoEntry.COLUMN_COURSE_ID, courseId)
        values.put(noteInfoEntry.COLUMN_NOTE_TITLE, noteTitle)
        values.put(noteInfoEntry.COLUMN_NOTE_TEXT, noteText)

        val task = SaveNoteToDB(dbHelper, values, selection, selectionArgs)
        task.execute()
    }

    private fun displayNote() {
        val courseId = cursor!!.getString(courseIdPos)
        val noteTitle = cursor!!.getString(noteTitlePos)
        val noteText = cursor!!.getString(noteTextPos)

        val courseIndex = getIndexOfCourse(courseId)
        spinnerCourses!!.setSelection(courseIndex)
        textNoteTitle!!.setText(noteTitle)
        textNoteText!!.setText(noteText)

        CourseEventBroadcastHelper.sendEventBroadcast(this, courseId, "Editing Note")
    }

    private fun getIndexOfCourse(courseId: String?): Int {
        val cursor = adapterCourses!!.cursor
        val courseIdPos = cursor.getColumnIndex(noteInfoEntry.COLUMN_COURSE_ID)
        var courseRowIndex = 0

        var more = cursor.moveToFirst()
        while (more){
            val cursorCourseId = cursor.getString(courseIdPos)
            if (courseId.equals(cursorCourseId))
                break

            courseRowIndex++
            more = cursor.moveToNext()
        }
        return courseRowIndex
    }

    private fun readDisplayStateValues() {
        val intent = intent
            noteID = intent.getIntExtra(NOTE_ID, ID_NOT_SET)
            isNewNote = noteID == ID_NOT_SET

        if (isNewNote)
            createNewNote()
    }

    inner class CreateNoteAsync(private val contentRes: ContentResolver, private val tag: String) :
        AsyncTask<ContentValues, Int, Uri>(){
        var progressBar: ProgressBar? = null

        override fun onPreExecute() {
            progressBar = findViewById<ProgressBar>(R.id.progress_bar)
            progressBar!!.visibility = View.VISIBLE
            progressBar!!.progress = 1
        }

        override fun doInBackground(vararg params: ContentValues): Uri?{
            Log.d(tag, "doInBackground - thread: " + Thread.currentThread().id)
            val values = params[0]
            val rowUri = contentResolver.insert(notesContract.CONTENT_URI, values)
            simulateLongBackgroundWork()
            publishProgress(2)
            simulateLongBackgroundWork()
            publishProgress(3)
            return rowUri
        }

        override fun onProgressUpdate(vararg values: Int?) {
            progressBar!!.progress = values[0]!!
        }

        private fun simulateLongBackgroundWork() {
            try {
                Thread.sleep(2000)
            } catch (ex: Exception){
            }
        }

        override fun onPostExecute(result: Uri?) {
            Log.d(tag, "onPostExecute - thread: " + Thread.currentThread().id)
            noteUri = result
            displaySnackBar(noteUri.toString())
            progressBar!!.visibility = View.GONE
        }
    }

    private fun displaySnackBar(msg: String) {
        Toast.makeText(this, msg, Snackbar.LENGTH_LONG).show()
    }

    private fun createNewNote() {
        val values = ContentValues()
        values.put(notesContract.COLUMN_COURSE_ID, "")
        values.put(notesContract.COLUMN_NOTE_TITLE, "")
        values.put(notesContract.COLUMN_NOTE_TEXT, "")

        val task = CreateNoteAsync(contentResolver, TAG)
        Log.d(TAG, "Call to createNewNote Task - thread: " + Thread.currentThread().id)
        task.execute(values)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_send_email -> {
                sendEmail()
                true
            }
            R.id.action_cancel -> {
                isCancelling = true
                finish()
                true
            }
            R.id.action_next -> {
                moveNext()
                true
            }
            R.id.action_set_reminder -> {
                showReminderNotification()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showReminderNotification() {
        val noteText = textNoteText!!.text.toString()
        val noteTitle = textNoteTitle!!.text.toString()
        val noteId = ContentUris.parseId(noteUri).toInt()

        val intent = Intent(this, NoteReminderReceiver::class.java)
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle)
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText)
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, noteId)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val currentTimeInMilliSecs = SystemClock.elapsedRealtime()
        val ONE_HOUR: Long = 60 * 60 * 1000
        val TEN_SEC: Long = 10 * 1000
        val alarmTime = currentTimeInMilliSecs + TEN_SEC
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, alarmTime, pendingIntent)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val item: MenuItem = menu!!.findItem(R.id.action_next)
        val lastIndex = DataManager.instance.notes.count() -1
        item.isEnabled = noteID < lastIndex
        return super.onPrepareOptionsMenu(menu)
    }

    private fun moveNext() {
        saveNote()

        note = DataManager.instance.notes[++noteID]
        saveOriginalValues()

        displayNote()
        invalidateOptionsMenu()
    }

    private fun sendEmail() {
        val course = spinner_courses.selectedItem as CourseInfo
        val subject = textNoteTitle!!.text.toString()
        val body = """Check out what I learned in the pluralsight course "${course.title}"
            | \n${textNoteText!!.text}
        """.trimMargin()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc2822"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(intent)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        var loader: CursorLoader? = null
        if (id == LOADER_NOTE)
            loader = createNoteLoader()
        else if (id == LOADER_COURSES)
            loader = createCourseLoader()

        return loader!!
    }

    private fun createCourseLoader(): CursorLoader? {
        courseQueryFinished = false
        val uri = coursesContract.CONTENT_URI
        val courseColumns = arrayOf(
            coursesContract.COLUMN_COURSE_TITLE,
            coursesContract.COLUMN_COURSE_ID,
            coursesContract._ID)
        return CursorLoader(this, uri, courseColumns, null, null, coursesContract.COLUMN_COURSE_TITLE)
    }

    private fun createNoteLoader(): CursorLoader? {
        notesQueryFinished = false
        val selection = noteInfoEntry._ID + " = ?"
        val selectionArgs = arrayOf(noteID.toString())

        val noteColumns = arrayOf(
            notesContract.COLUMN_COURSE_ID,
            notesContract.COLUMN_NOTE_TITLE,
            notesContract.COLUMN_NOTE_TEXT
        )
        noteUri = ContentUris.withAppendedId(notesContract.CONTENT_URI, noteID.toLong())
        return CursorLoader(this, noteUri!!, noteColumns, selection, selectionArgs, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (loader.id == LOADER_NOTE){
            loadFinishedNotes(data)
        } else if (loader.id == LOADER_COURSES) {
            adapterCourses!!.swapCursor(data)
            courseQueryFinished = true
            displayNoteWhenQueryIsFinished()
        }
    }

    private fun loadFinishedNotes(data: Cursor?) {
        cursor = data
        courseIdPos = cursor!!.getColumnIndex(noteInfoEntry.COLUMN_COURSE_ID)
        noteTitlePos = cursor!!.getColumnIndex(noteInfoEntry.COLUMN_NOTE_TITLE)
        noteTextPos = cursor!!.getColumnIndex(noteInfoEntry.COLUMN_NOTE_TEXT)
        cursor!!.moveToNext()
        notesQueryFinished = true
        displayNoteWhenQueryIsFinished()
    }

    private fun displayNoteWhenQueryIsFinished() {
        if (courseQueryFinished && notesQueryFinished)
            displayNote()
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (loader.id == LOADER_NOTE){
            if (cursor != null)
                cursor!!.close()
        } else if (loader.id == LOADER_COURSES) {
            adapterCourses!!.swapCursor(null)
        }
    }

    companion object {
        private var noteID: Int = 0
        private val ID_NOT_SET = -1
        private var originalNoteText: String? = ""
        private var originalNoteTitle: String? = ""
        private var originalNoteCourseId: String? = ""
        val noteInfoEntry = NoteKeeperDatabaseContract.Companion.NoteInfoEntry()
        val courseInfoEntry = NoteKeeperDatabaseContract.Companion.CourseInfoEntry()

        val coursesContract = NoteKeeperProviderContract.Companion.Courses()
        val notesContract = NoteKeeperProviderContract.Companion.Notes()

        var courseIdPos = 0
        var noteTitlePos = 0
        var noteTextPos = 0
        var courseQueryFinished = false
        var notesQueryFinished = false

        val LOADER_COURSES = 1

        class MyAsyncTask(private val noteUri: Uri?, private val contentRes: ContentResolver, private val selection: String, private val args: Array<String>) :
                AsyncTask<Void, Void, Int>(){
            override fun doInBackground(vararg params: Void?): Int {
                return contentRes.delete(noteUri!!, selection, args)
            }
        }

        class SaveNoteToDB(private val dbHelper: NoteKeeperOpenHelper?,private val values: ContentValues, private val selection: String, private val args: Array<String>) :
            AsyncTask<Void, Void, Int>(){
            override fun doInBackground(vararg params: Void?): Int? {
                val db = dbHelper!!.writableDatabase
                db.update(noteInfoEntry.TABLE_NAME, values, selection, args)
                return null
            }
        }
    }
}
