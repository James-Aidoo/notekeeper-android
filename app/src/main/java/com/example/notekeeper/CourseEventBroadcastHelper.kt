package com.example.notekeeper

import android.content.Context
import android.content.Intent

val ACTION_COURSE_EVENT = "com.example.notekeeper.action.COURSE_EVENT"
val EXTRA_COURSE_MESSAGE = "com.example.notekeeper.extra.COURSE_MESSAGE"

class CourseEventBroadcastHelper {

    companion object {
        fun sendEventBroadcast(context: Context, courseId: String, message: String) {
            val intent = Intent(ACTION_COURSE_EVENT)
            intent.putExtra(EXTRA_COURSE_ID, courseId)
            intent.putExtra(EXTRA_COURSE_MESSAGE, message)

            context.sendBroadcast(intent)
        }
    }
}