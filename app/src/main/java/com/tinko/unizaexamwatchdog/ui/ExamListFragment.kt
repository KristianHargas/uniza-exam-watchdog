package com.tinko.unizaexamwatchdog.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs

import com.tinko.unizaexamwatchdog.R
import com.tinko.unizaexamwatchdog.adapter.ExamClickListener
import com.tinko.unizaexamwatchdog.adapter.ExamListAdapter
import com.tinko.unizaexamwatchdog.databinding.FragmentExamListBinding
import com.tinko.unizaexamwatchdog.domain.Exam
import com.tinko.unizaexamwatchdog.viewmodel.ExamListViewModel
import kotlinx.coroutines.internal.artificialFrame

class ExamListFragment : Fragment() {

    private val args: ExamListFragmentArgs by navArgs()

    private val examListViewModel: ExamListViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "No activity present before onActivityCreated()!"
        }
        ViewModelProvider(this, ExamListViewModel.Factory(activity.application))
            .get(ExamListViewModel::class.java)
    }
    private lateinit var binding: FragmentExamListBinding
    private lateinit var adapter: ExamListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // set the toolbar title
        (activity as MainActivity).supportActionBar?.title = args.subjectName

        val binding = FragmentExamListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.examListViewModel = examListViewModel

        val examClickListener: ExamClickListener = object : ExamClickListener {
            override fun examClicked(exam: Exam) {
                val dialog = ExamNoteDialogFragment()
                val bundle = Bundle()
                bundle.putString("KEY_NOTE", exam.note)
                dialog.arguments = bundle
                dialog.show(parentFragmentManager, "exam_note")
            }
        }

        this.binding = binding
        this.adapter = ExamListAdapter(examClickListener)
        binding.examsRecyclerView.adapter = adapter

        // launch loading of subjects
        examListViewModel.loadExams(args.subjectId)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        examListViewModel.exams.observe(viewLifecycleOwner, Observer {
            it?.apply {
                adapter.exams = it
            }
        })
    }
}
