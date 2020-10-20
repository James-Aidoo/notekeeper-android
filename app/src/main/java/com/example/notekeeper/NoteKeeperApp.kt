package com.example.notekeeper

import android.app.Application
import android.content.Context

class NoteKeeperApp : Application() {
    companion object {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = this.applicationContext
    }
}