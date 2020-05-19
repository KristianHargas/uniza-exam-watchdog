package com.tinko.unizaexamwatchdog.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.tinko.unizaexamwatchdog.R
import com.tinko.unizaexamwatchdog.databinding.FragmentMainScreenBinding
import com.tinko.unizaexamwatchdog.repository.AuthenticationState
import com.tinko.unizaexamwatchdog.viewmodel.MainViewModel

class MainScreenFragment : Fragment() {

    private lateinit var binding: FragmentMainScreenBinding

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.authenticated.observe(viewLifecycleOwner, Observer {
            if (it != AuthenticationState.AUTHENTICATED) {
                // if the user is not authenticated after startup -> always redirect him to login fragment
                findNavController().navigate(R.id.action_mainScreenFragment_to_loginFragment)
            } else {
                // we are authenticated -> lets load data
                mainViewModel.loadSubjects()
            }
        })

        mainViewModel.allSubjects.observe(viewLifecycleOwner, Observer {
            var subjects: String = ""

            it.forEach {
                subjects += "${it.id} ${it.name} ${it.term}\n"
            }

            binding.mainContent.text = subjects
        })
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
