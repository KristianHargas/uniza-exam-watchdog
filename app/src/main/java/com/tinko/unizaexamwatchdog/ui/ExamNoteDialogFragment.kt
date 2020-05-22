package com.tinko.unizaexamwatchdog.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.Exception
import java.lang.IllegalStateException

class ExamNoteDialogFragment() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Poznámka")
            builder.setMessage(arguments?.getString("KEY_NOTE"))
            builder.create()
        } ?: throw IllegalStateException("Activity is null!")
    }
}