package com.tinko.unizaexamwatchdog.ui

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.tinko.unizaexamwatchdog.R
import com.tinko.unizaexamwatchdog.databinding.FragmentLoginBinding
import com.tinko.unizaexamwatchdog.repository.AuthenticationState
import com.tinko.unizaexamwatchdog.viewmodel.LoginViewModel


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

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
        this.binding = binding

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            loginViewModel.loginCancelled()
            requireActivity().finish()
        }

        loginViewModel.authenticated.observe(viewLifecycleOwner, Observer {

            if (it == AuthenticationState.AUTHENTICATED) {
                findNavController().popBackStack()
            } else if (it == AuthenticationState.INVALID_AUTHENTICATION) {
                showSnackbar(R.string.invalid_authentication_message)
            } else if (it == AuthenticationState.NETWORK_ERROR) {
                showSnackbar(R.string.network_error_message)
            } else if (it == AuthenticationState.AUTHENTICATING) {
                hideVirtualKeyboard()
            }
        })
    }

    private fun showSnackbar(stringId: Int) {
        Snackbar.make(binding.root, stringId, Snackbar.LENGTH_SHORT).show()
    }

    private fun hideVirtualKeyboard() {
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
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
