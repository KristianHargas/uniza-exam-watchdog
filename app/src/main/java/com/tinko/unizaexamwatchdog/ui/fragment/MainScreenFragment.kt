package com.tinko.unizaexamwatchdog.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.tinko.unizaexamwatchdog.R
import com.tinko.unizaexamwatchdog.adapter.SubjectListAdapter
import com.tinko.unizaexamwatchdog.adapter.SubjectListener
import com.tinko.unizaexamwatchdog.databinding.FragmentMainScreenBinding
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.Term
import com.tinko.unizaexamwatchdog.domain.filter
import com.tinko.unizaexamwatchdog.ui.MainActivity
import com.tinko.unizaexamwatchdog.viewmodel.MainViewModel

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val binding = FragmentMainScreenBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.mainViewModel = mainViewModel
        this.binding = binding

        val subjectListener: SubjectListener = object : SubjectListener {
            override fun subjectClicked(subject: Subject) {
                // start exam list fragment
                val action = MainScreenFragmentDirections.actionMainScreenFragmentToExamListFragment(subject.id, subject.name)
                findNavController().navigate(action)
            }

            override fun subjectWatcherStateChanged(state: Boolean, subject: Subject) {
                mainViewModel.updateSubject(subject, state)
                val message = when(state) {
                    true -> getString(R.string.subject_watch_state_on_message, subject.name)
                    else -> getString(R.string.subject_watch_state_off_message, subject.name)
                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            }
        }

        adapter = SubjectListAdapter(subjectListener)
        binding.subjectsRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // conditional login navigation flow or data load
        mainViewModel.userAuthenticated.observe(viewLifecycleOwner, Observer {
            it?.let { authenticated ->
                if (authenticated) {
                    // we are authenticated -> lets load data
                    mainViewModel.loadSubjects()
                } else {
                    // if the user is not authenticated after startup -> always redirect him to login fragment
                    findNavController().navigate(R.id.action_mainScreenFragment_to_loginFragment)
                }
            }
        })

        // term switched
        mainViewModel.term.observe(viewLifecycleOwner, Observer {
            it?.let { term ->
                // update bottom nav selection after startup
                val itemId = if(term == Term.WINTER) R.id.winter_term else R.id.summer_term
                if (binding.mainBottomMenu.selectedItemId != itemId)
                    binding.mainBottomMenu.selectedItemId = itemId

                // update toolbar title
                (activity as MainActivity).supportActionBar?.title = getString(
                    when(term) {
                        Term.WINTER -> R.string.winter_term
                        else -> R.string.summer_term
                    }
                )

                // update subject selection
                mainViewModel.allSubjects.value?.apply {
                    adapter.subjects = this.filter(term)
                }
            }
        })

        // any change in loaded data is sent to adapter to get reflected in the list
        mainViewModel.allSubjects.observe(viewLifecycleOwner, Observer {
            it?.let { subjects ->
                adapter.subjects = subjects.filter(mainViewModel.term.value ?: Term.WINTER)
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

        mainViewModel.workerRunning.observe(viewLifecycleOwner, Observer {
            it?.let {
                activity?.invalidateOptionsMenu()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_options_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        when (mainViewModel.workerRunning.value) {
            true -> {
                menu.findItem(R.id.menu_item_watchdog_on).isVisible = false
                menu.findItem(R.id.menu_item_watchdog_off).isVisible = true
            }
            false -> {
                menu.findItem(R.id.menu_item_watchdog_on).isVisible = true
                menu.findItem(R.id.menu_item_watchdog_off).isVisible = false
            }
            else -> {
                menu.findItem(R.id.menu_item_watchdog_on).isVisible = false
                menu.findItem(R.id.menu_item_watchdog_off).isVisible = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_watchdog_on -> {
                mainViewModel.startWorker()
                Snackbar.make(binding.root, getString(R.string.global_watchdog_on_message), Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.menu_item_watchdog_off -> {
                mainViewModel.stopWorker()
                Snackbar.make(binding.root, getString(R.string.global_watchdog_off_message), Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.menu_item_logout -> {
                mainViewModel.logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
