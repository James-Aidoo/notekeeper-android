package com.example.notekeeper

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter

class NoteRecyclerAdapter(context: Context, var cursor: Cursor?) : Adapter<NoteRecyclerAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    init {
        populateColumnsPositions()
    }

    private fun populateColumnsPositions() {
        if (cursor == null)
            return

        coursePos = cursor!!.getColumnIndex(courseInfoEntry.COLUMN_COURSE_TITLE)
        noteTitlePos = cursor!!.getColumnIndex(noteInfoEntry.COLUMN_NOTE_TITLE)
        idPos = cursor!!.getColumnIndex(noteInfoEntry._ID)
    }

    fun changeCursor(cursor: Cursor?){
        if (this.cursor != null)
            this.cursor!!.close()

        this.cursor = cursor
        populateColumnsPositions()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.note_list_item, parent,false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if (cursor == null) 0; else cursor!!.count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        cursor?.count
        cursor!!.moveToPosition(position)
        holder.textCourse.text = cursor!!.getString(coursePos)
        holder.textTitle.text = cursor!!.getString(noteTitlePos)
        holder.id = cursor!!.getInt(idPos)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textCourse: TextView = itemView.findViewById(R.id.note_title)
        val textTitle: TextView = itemView.findViewById(R.id.text_title)
        var id = 0

        init {
            itemView.setOnClickListener {
                val intent = Intent(NoteKeeperApp.context, NoteActivity::class.java)
                intent.putExtra(NOTE_ID, id)

                NoteKeeperApp.context!!.startActivity(intent)
            }
        }
    }

    companion object {
        val noteInfoEntry = NoteKeeperDatabaseContract.Companion.NoteInfoEntry()
        val courseInfoEntry = NoteKeeperDatabaseContract.Companion.CourseInfoEntry()
        var coursePos = 0
        var noteTitlePos = 0
        var idPos = 0
    }
}