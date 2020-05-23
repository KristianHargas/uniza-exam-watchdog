package com.tinko.unizaexamwatchdog.util

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Binding adapter which sets parsed date as content of [TextView].
 *
 * @param view target view.
 * @param date content.
 */
@SuppressLint("SimpleDateFormat")
@BindingAdapter("formatDate")
fun formatDate(view: TextView, date: Date?) {
    if (date == null) {
        view.text = "-"
    } else {
        val pattern = "dd.MM. yyyy - HH:mm"
        val formatter = SimpleDateFormat(pattern)
        view.text = formatter.format(date)
    }
}

/**
 * Binding adapter which hides view if provided argument is null.
 *
 * @param view target view.
 * @param obj object to check for null.
 */
@BindingAdapter("goneIfNull")
fun goneIfNull(view: View, obj: Any?) {
    view.visibility = if (obj == null) View.GONE else View.VISIBLE
}