package com.tinko.unizaexamwatchdog.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tinko.unizaexamwatchdog.databinding.SubjectItemBinding
import com.tinko.unizaexamwatchdog.domain.Subject

/**
 * Listener interface for handling events related to subject item interaction.
 */
interface SubjectListener {
    /**
     * Called when the subject item is clicked in the list.
     *
     * @param subject subject data associated with clicked item.
     */
    fun subjectClicked(subject: Subject)

    /**
     * Called when the state of item's switch changes
     *
     * @param state new state after switching.
     * @param subject subject data associated with the item.
     */
    fun subjectWatcherStateChanged(state: Boolean, subject: Subject)
}

/**
 * Adapter used by the list displaying subjects items in [MainScreenFragment].
 *
 * @property listener listener object which handles interactions with subject items.
 */
class SubjectListAdapter(private val listener: SubjectListener) :
    RecyclerView.Adapter<SubjectListAdapter.SubjectViewHolder>() {

    /**
     * Holder class holding reference to binding of subject item.
     *
     * @property binding binding of the subject item.
     */
    inner class SubjectViewHolder(val binding: SubjectItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * List of subjects to display in the list.
     *
     * Update this when data set changes.
     */
    var subjects: List<Subject> = emptyList()
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding: SubjectItemBinding = SubjectItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return SubjectViewHolder(binding)
    }

    /**
     * Called when the list needs to update the data of passed item.
     *
     * @param holder holder object to update
     * @param position position in data set
     */
    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]
        holder.binding.subject = subject
        holder.binding.listener = listener

        // avoid triggering switch state change event by setting default state
        holder.binding.watchdogSwitch.setOnCheckedChangeListener(null)
        // set the default state
        holder.binding.watchdogSwitch.isChecked = subject.watched
        holder.binding.watchdogSwitch.setOnCheckedChangeListener { _, isChecked ->
            listener.subjectWatcherStateChanged(isChecked, subject)
        }

        // immediately redraw item
        holder.binding.executePendingBindings()
    }

    /**
     * Called by the list.
     *
     * @return size of data set.
     */
    override fun getItemCount(): Int = subjects.size
}
