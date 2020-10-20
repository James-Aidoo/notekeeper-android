package com.example.notekeeper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NoteReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE)
        val noteText = intent.getStringExtra(EXTRA_NOTE_TEXT)
        val noteId = intent.getIntExtra(EXTRA_NOTE_ID, -1)

        NoteReminderNotification.notify(context, noteTitle, noteText, noteId)
    }

    companion object {
        const val EXTRA_NOTE_TITLE = "com.example.notekeeper.extra.NOTE_TITLE"
        const val EXTRA_NOTE_TEXT = "com.example.notekeeper.extra.NOTE_TEXT"
        const val EXTRA_NOTE_ID = "com.example.notekeeper.extra.NOTE_ID"
    }
}
