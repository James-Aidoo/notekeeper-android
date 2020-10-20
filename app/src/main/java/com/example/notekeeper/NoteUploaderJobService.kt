package com.example.notekeeper

import android.app.job.JobParameters
import android.app.job.JobService
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi

val EXTRA_DATA_URI = "com.example.notekeeper.extras.DATA_URI"

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NoteUploaderJobService : JobService() {

    var noteUploader: NoteUploader? = null

    inner class UploadTask : AsyncTask<JobParameters, Void, Void>(){
        override fun doInBackground(vararg backgroundParams: JobParameters?): Void? {
            val jobParams = backgroundParams[0]!!

            val stringDataUri = jobParams.extras.getString(EXTRA_DATA_URI)
            val dataUri = Uri.parse(stringDataUri)

            noteUploader!!.doUpload(dataUri)

            if (!noteUploader!!.isCanceled)
                jobFinished(jobParams, false)

            return null
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        noteUploader = NoteUploader(this)

        val task = UploadTask()
        task.execute(params)

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        noteUploader!!.cancel()
        return true
    }
}
