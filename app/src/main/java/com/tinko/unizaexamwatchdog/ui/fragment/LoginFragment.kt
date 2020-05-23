package com.tinko.unizaexamwatchdog.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.tinko.unizaexamwatchdog.R
import com.tinko.unizaexamwatchdog.databinding.FragmentLoginBinding
import com.tinko.unizaexamwatchdog.repository.AuthenticationState
import com.tinko.unizaexamwatchdog.ui.MainActivity
import com.tinko.unizaexamwatchdog.viewmodel.LoginViewModel

/**
 * This fragment is used to authenticate user.
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private val loginViewModel: LoginViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "No activity present before onActivityCreated()!"
        }

        ViewModelProvider(this, LoginViewModel.Factory(activity.application))
            .get(LoginViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set the toolbar title
        (activity as MainActivity).supportActionBar?.title = getString(R.string.login_screen_title)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate the layout for this fragment using binding
        val binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.loginViewModel = loginViewModel
        this.binding = binding

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // when the user presses back button in login fragment, close the app, he refused to authenticate
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            loginViewModel.loginCancelled()
            requireActivity().finish()
        }

        loginViewModel.authState.observe(viewLifecycleOwner, Observer {
            it?.let { state ->
                when (state) {
                    AuthenticationState.AUTHENTICATED -> {
                        // user is authenticated, return back to the main screen fragment
                        findNavController().popBackStack()
                    }
                    AuthenticationState.INVALID_AUTHENTICATION -> {
                        showSnackBar(R.string.invalid_authentication_message)
                    }
                    AuthenticationState.NETWORK_ERROR -> {
                        showSnackBar(R.string.network_error_message)
                    }
                    AuthenticationState.AUTHENTICATING -> {
                        // when the user presses the login button, hide virtual keyboard
                        hideVirtualKeyboard()
                    }
                    AuthenticationState.UNAUTHENTICATED -> {
                    }
                }
            }
        })
    }

    private fun showSnackBar(stringId: Int) {
        Snackbar.make(binding.root, stringId, Snackbar.LENGTH_LONG).show()
    }

    private fun hideVirtualKeyboard() {
        val inputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
