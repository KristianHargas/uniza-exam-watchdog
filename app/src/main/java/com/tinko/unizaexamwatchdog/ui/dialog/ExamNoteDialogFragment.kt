package com.tinko.unizaexamwatchdog.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.tinko.unizaexamwatchdog.R
import java.lang.IllegalStateException

class ExamNoteDialogFragment() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.note))
            builder.setMessage(arguments?.getString(KEY_NOTE))
            builder.create()
        } ?: throw IllegalStateException("Activity is null!")
    }

    companion object {
        const val KEY_NOTE = "note"
        const val TAG = "exam_note_dialog"
    }
}