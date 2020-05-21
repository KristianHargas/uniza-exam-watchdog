package com.tinko.unizaexamwatchdog.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.tinko.unizaexamwatchdog.R
import com.tinko.unizaexamwatchdog.adapter.SubjectListAdapter
import com.tinko.unizaexamwatchdog.adapter.SubjectListener
import com.tinko.unizaexamwatchdog.databinding.FragmentMainScreenBinding
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.Term
import com.tinko.unizaexamwatchdog.domain.filter
import com.tinko.unizaexamwatchdog.repository.AuthenticationState
import com.tinko.unizaexamwatchdog.viewmodel.MainViewModel
import java.io.IOException

class MainScreenFragment : Fragment() {

    private lateinit var binding: FragmentMainScreenBinding
    private lateinit var adapter: SubjectListAdapter

    private val mainViewModel: MainViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "No activity present before onActivityCreated()!"
        }
        ViewModelProvider(this, MainViewModel.Factory(activity.application))
            .get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.i(TAG, "onCreateView");

        // Inflate the layout for this fragment
        val binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.mainViewModel = mainViewModel
        this.binding = binding

        val subjectListener: SubjectListener = object : SubjectListener {
            override fun subjectClicked(subject: Subject) {
                mainViewModel.loadExams()
            }

            override fun subjectWatcherStateChanged(state: Boolean, subject: Subject) {
                mainViewModel.updateSubject(subject, state)
            }
        }

        adapter = SubjectListAdapter(subjectListener)
        binding.subjectsRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // conditional login navigation flow or data load
        mainViewModel.authenticated.observe(viewLifecycleOwner, Observer {
            if (it != AuthenticationState.AUTHENTICATED) {
                // if the user is not authenticated after startup -> always redirect him to login fragment
                findNavController().navigate(R.id.action_mainScreenFragment_to_loginFragment)
            } else {
                // we are authenticated -> lets load data
                mainViewModel.loadSubjects()
            }
        })

        // term switched
        mainViewModel.term.observe(viewLifecycleOwner, Observer {
            it?.apply {
                // update bottom nav selection after startup
                val itemId = if(it == Term.WINTER) R.id.winter_term else R.id.summer_term
                if (binding.mainBottomMenu.selectedItemId != itemId)
                    binding.mainBottomMenu.selectedItemId = itemId

                // update toolbar title
                (activity as MainActivity).supportActionBar?.title = getString(
                    when(it) {
                        Term.WINTER -> R.string.winter_term
                        else -> R.string.summer_term
                    }
                )

                // update subject selection
                mainViewModel.allSubjects.value?.apply {
                    adapter.subjects = this.filter(it)
                }
            }
        })

        // any change in loaded data is sent to adapter to get reflected in the list
        mainViewModel.allSubjects.observe(viewLifecycleOwner, Observer {
            it?.apply {
                adapter.subjects = it.filter(mainViewModel.term.value ?: Term.WINTER)
            }
        })

        // term selection
        binding.mainBottomMenu.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.summer_term -> {
                    mainViewModel.termChanged(Term.SUMMER)
                    true
                }
                R.id.winter_term -> {
                    mainViewModel.termChanged(Term.WINTER)
                    true
                }
                else -> false
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i(TAG, "onAttach");
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate");
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart");
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop");
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.i(TAG, "onDestroy");
    }

    companion object {
        const val TAG = "MainScreenFragment"
    }
}
