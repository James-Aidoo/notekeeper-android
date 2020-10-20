package com.example.notekeeper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.google.android.material.snackbar.Snackbar

class CourseRecyclerAdapter(context: Context, var courses: ArrayList<CourseInfo>) : Adapter<CourseRecyclerAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.course_list_item, parent,false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return courses.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textCourse.text = courses[position].title
        holder.currentPosition = position
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textCourse: TextView = itemView.findViewById(R.id.text_course)
        var currentPosition = 0

        init {
            itemView.setOnClickListener {
                Snackbar.make(it, textCourse.text, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}