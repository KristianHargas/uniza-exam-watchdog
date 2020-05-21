package com.tinko.unizaexamwatchdog.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tinko.unizaexamwatchdog.databinding.SubjectItemBinding
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.hasSameSubjectsAs

class SubjectListAdapter(private val listener: SubjectListener) : RecyclerView.Adapter<SubjectListAdapter.SubjectViewHolder>() {

    var subjects: List<Subject> = emptyList()
        set(value) {
            // useless recyclerview redraw prevention
            if (value.hasSameSubjectsAs(field)) {
                field = value
            } else {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding: SubjectItemBinding = SubjectItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]
        holder.binding.subject = subject
        holder.binding.listener = listener

        holder.binding.watchdogSwitch.setOnCheckedChangeListener(null)
        holder.binding.watchdogSwitch.isChecked = subject.watched
        holder.binding.watchdogSwitch.setOnCheckedChangeListener { view, isChecked ->
            listener.subjectWatcherStateChanged(isChecked, subject)
        }
    }

    override fun getItemCount(): Int = subjects.size

    inner class SubjectViewHolder(val binding: SubjectItemBinding) : RecyclerView.ViewHolder(binding.root)
}

interface SubjectListener {
    fun subjectClicked(subject: Subject)
    fun subjectWatcherStateChanged(state: Boolean, subject: Subject)
}