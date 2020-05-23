package com.tinko.unizaexamwatchdog.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tinko.unizaexamwatchdog.databinding.ExamItemBinding
import com.tinko.unizaexamwatchdog.domain.Exam

/**
 * This listener interface is invoked when the exam item is clicked in the list.
 */
interface ExamClickListener {
    /**
     * Called when users clicks the exam item.
     *
     * @param exam exam object associated with clicked item.
     */
    fun examClicked(exam: Exam)
}

/**
 * Adapter used by the list displaying exam items in [ExamListFragment].
 *
 * @property listener listener object which handles clicks on exam items.
 */
class ExamListAdapter(private val listener: ExamClickListener) :
    RecyclerView.Adapter<ExamListAdapter.ExamViewHolder>() {

    /**
     * Holder class holding reference to binding of exam item.
     *
     * @property binding binding of the exam item.
     */
    inner class ExamViewHolder(val binding: ExamItemBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * List of exams to display in the list.
     *
     * Update this when data set changes.
     */
    var exams: List<Exam> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    /**
     * Called by the list when new item needs to be created.
     *
     * @param parent parent container.
     * @param viewType type of the created view.
     * @return holder for this item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val binding: ExamItemBinding = ExamItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ExamViewHolder(binding)
    }

    /**
     * Called when the list needs to update the data of passed item.
     *
     * @param holder holder object to update
     * @param position position in data set
     */
    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        holder.binding.exam = exams[position]
        holder.binding.listener = listener

        // immediately redraw item
        holder.binding.executePendingBindings()
    }

    /**
     * Called by the list.
     *
     * @return size of data set.
     */
    override fun getItemCount(): Int = exams.size
}