package com.example.notekeeper

import android.app.IntentService
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

const val EXTRA_COURSE_ID = "com.example.notekeeper.extra.COURSE_ID"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * TODO: Customize class - update intent actions and extra parameters.
 */
@RequiresApi(Build.VERSION_CODES.CUPCAKE)
class NoteBackupService : IntentService("NoteBackupService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val backUpCourseId = intent.getStringExtra(EXTRA_COURSE_ID)
            NoteBackup.doBackup(this, backUpCourseId)
        }
    }
}
