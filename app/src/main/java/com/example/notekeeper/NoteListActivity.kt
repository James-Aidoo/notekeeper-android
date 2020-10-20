package com.example.notekeeper

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_note_list.*
import kotlinx.android.synthetic.main.content_note_list.*

class NoteListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_note_list)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }

        initializeDisplayContent()
    }

    override fun onResume() {
        super.onResume()
        //notesAdapter.notifyDataSetChanged()
        noteRecyclerAdapter.notifyDataSetChanged()
    }

    private fun initializeDisplayContent() {
        val linearLayoutManager = LinearLayoutManager(this)
        list_notes.layoutManager = linearLayoutManager

        list_notes.adapter = noteRecyclerAdapter
    }

    companion object {
        //private val notes = DataManager.instance.notes
        val noteRecyclerAdapter = NoteRecyclerAdapter(NoteKeeperApp.context!!, null)
    }

}
