package com.example.notekeeper

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.*
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
                    LoaderManager.LoaderCallbacks<Cursor>{

    private var dbHelper: NoteKeeperOpenHelper? = null
    private var NOTE_UPLOADER_JOB_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        enableStrictMode()

        dbHelper = NoteKeeperOpenHelper(this)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
        initializeDisplayContent()
    }

    private fun enableStrictMode() {
        if (BuildConfig.DEBUG){
            val policy = StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
            StrictMode.setThreadPolicy(policy)
        }
    }

    override fun onResume() {
        super.onResume()
        LoaderManager.enableDebugLogging(true)
        LoaderManager.getInstance(this).destroyLoader(LOADER_NOTE)
        LoaderManager.getInstance(this).restartLoader(LOADER_NOTE, null, this)
        retrievePreferences()
        toggleDrawer()
    }

    private fun toggleDrawer() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.openDrawer(GravityCompat.START)
        }, 1000)
    }


    override fun onDestroy() {
        dbHelper!!.close()
        super.onDestroy()
    }

    private fun retrievePreferences() {
        val header = nav_view.getHeaderView(0)
        val txtUsername = header.findViewById<TextView>(R.id.text_user_name)
        val txtUserEmail = header.findViewById<TextView>(R.id.text_user_email)
        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        txtUsername.text = pref.getString("user_display_name", "")
        txtUserEmail.text = pref.getString("user_email_address", "")
    }

    private fun initializeDisplayContent() {
        DataManager.loadFromDatabase(dbHelper!!)

        notesLayoutManager = LinearLayoutManager(this)
        courseLayoutManager = GridLayoutManager(this, resources.getInteger(R.integer.course_grid_span))
        val courses = DataManager.instance.courses

        noteRecyclerAdapter = NoteRecyclerAdapter(NoteKeeperApp.context!!, null)
        courseRecyclerAdapter = CourseRecyclerAdapter(this, courses)

        displayNotes()
        (nav_view.menu).findItem(R.id.nav_notes).isChecked = true
    }

    private fun displayNotes() {
        list_items.layoutManager = notesLayoutManager
        list_items.adapter = noteRecyclerAdapter
    }

    private fun displayCourses() {
        list_items.layoutManager = courseLayoutManager
        list_items.adapter = courseRecyclerAdapter
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_backup -> {
                backupNotes()
                true
            }
            R.id.action_upload -> {
                scheduleNoteUpload()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scheduleNoteUpload() {
        val extras = PersistableBundle()
        extras.putString(EXTRA_DATA_URI, notesContract.CONTENT_URI.toString())

        val componentName = ComponentName(this, NoteUploaderJobService::class.java)
        val jobInfo = JobInfo.Builder(NOTE_UPLOADER_JOB_ID, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setExtras(extras)
            .build()

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(jobInfo)
    }

    private fun backupNotes() {
        val intent = Intent(this, NoteBackupService::class.java)
        intent.putExtra(EXTRA_COURSE_ID, NoteBackup.ALL_COURSES)
        startService(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_notes -> {
                displayNotes()
            }
            R.id.nav_courses -> {
                displayCourses()
            }
            R.id.nav_share -> {
                handleShare()
            }
            R.id.nav_send -> {
                handleSelection(R.string.nav_send_message)
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleShare() {
        Snackbar.make(list_items, PreferenceManager.getDefaultSharedPreferences(this).getString("social_media", ""),
                            Snackbar.LENGTH_LONG).show()
    }

    private fun handleSelection(messageId: Int) {
        Snackbar.make(list_items, messageId, Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val loader: CursorLoader? = null
        return when (id) {
            LOADER_NOTE -> {
                createLoaderNotes()
            }
            else -> {
                loader as Loader<Cursor>
            }
        }
    }

    private fun createLoaderNotes(): CursorLoader {
        val uri = notesContract.EXPANDED_CONTENT_URI
        val noteColumns = arrayOf(
            notesContract._ID,
            notesContract.COLUMN_NOTE_TITLE,
            notesContract.COLUMN_COURSE_TITLE
        )

        val orderBy = notesContract.COLUMN_COURSE_TITLE + ", " + notesContract.COLUMN_NOTE_TITLE

        return CursorLoader(this, uri, noteColumns, null, null, orderBy)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (loader.id == LOADER_NOTE){
            noteRecyclerAdapter.changeCursor(data!!)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (loader.id == LOADER_NOTE) {
            noteRecyclerAdapter.changeCursor(null)
        }
    }

    companion object {
        private lateinit var courseRecyclerAdapter: CourseRecyclerAdapter
        private lateinit var noteRecyclerAdapter: NoteRecyclerAdapter
        lateinit var notesLayoutManager: LinearLayoutManager
        lateinit var courseLayoutManager: GridLayoutManager
        val noteInfoEntry = NoteKeeperDatabaseContract.Companion.NoteInfoEntry()
        val courseInfoEntry = NoteKeeperDatabaseContract.Companion.CourseInfoEntry()

        val notesContract = NoteKeeperProviderContract.Companion.Notes()
    }
}
