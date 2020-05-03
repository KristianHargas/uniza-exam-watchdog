package com.tinko.unizaexamwatchdog.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.tinko.unizaexamwatchdog.databinding.FragmentLoginBinding

import com.tinko.unizaexamwatchdog.R
import com.tinko.unizaexamwatchdog.repository.AuthenticationState
import com.tinko.unizaexamwatchdog.repository.UserRepository
import com.tinko.unizaexamwatchdog.viewmodel.LoginViewModel

class LoginFragment : Fragment() {

    private val loginViewModel: LoginViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "No activity present before onActivityCreated()!"
        }
        ViewModelProvider(this, LoginViewModel.Factory(activity.application))
            .get(LoginViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.i("LoginFragment", "onCreateView");

        // Inflate the layout for this fragment
        val binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.loginViewModel = loginViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.i("LoginFragment", "back button clicked")
            requireActivity().finish()
        }

        loginViewModel.authenticated.observe(viewLifecycleOwner, Observer {
            Log.i("LoginFragment", it.toString())

            if (it == AuthenticationState.AUTHENTICATED) {
                Log.i("LoginFragment", "loggedIn")

                findNavController().popBackStack()
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i("LoginFragment", "onAttach");
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("LoginFragment", "onCreate");
    }

    override fun onStart() {
        super.onStart()
        Log.i("LoginFragment", "onStart");
    }

    override fun onStop() {
        super.onStop()
        Log.i("LoginFragment", "onStop");
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.i("LoginFragment", "onDestroy");
    }
}
