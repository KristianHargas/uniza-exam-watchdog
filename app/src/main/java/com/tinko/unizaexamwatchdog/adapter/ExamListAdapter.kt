package com.tinko.unizaexamwatchdog.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tinko.unizaexamwatchdog.databinding.ExamItemBinding
import com.tinko.unizaexamwatchdog.domain.Exam

interface ExamClickListener {
    fun examClicked(exam: Exam)
}

class ExamListAdapter(private val listener: ExamClickListener) : RecyclerView.Adapter<ExamListAdapter.ExamViewHolder>() {

    inner class ExamViewHolder(val binding: ExamItemBinding): RecyclerView.ViewHolder(binding.root)

    var exams: List<Exam> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val binding: ExamItemBinding = ExamItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ExamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        holder.binding.exam = exams[position]
        holder.binding.listener = listener

        // immediately redraw item
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int = exams.size
}