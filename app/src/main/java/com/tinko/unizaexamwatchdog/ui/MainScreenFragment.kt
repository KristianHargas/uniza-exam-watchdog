package com.tinko.unizaexamwatchdog.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.tinko.unizaexamwatchdog.R

class MainScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.i("MainScreenFragment", "onCreateView");

        val root = inflater.inflate(R.layout.fragment_main_screen, container, false)

        root.findViewById<Button>(R.id.login).setOnClickListener {
            findNavController().navigate(R.id.action_mainScreenFragment_to_loginFragment)
        }

        return root
    }

    override fun onStart() {
        super.onStart()
        Log.i("MainScreenFragment", "onStart");
    }

    override fun onStop() {
        super.onStop()
        Log.i("MainScreenFragment", "onStop");
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.i("MainScreenFragment", "onDestroy");
    }
}
