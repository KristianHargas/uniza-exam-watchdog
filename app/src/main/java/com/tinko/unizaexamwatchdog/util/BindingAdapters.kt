package com.tinko.unizaexamwatchdog.util

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*

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

@BindingAdapter("goneIfNull")
fun goneIfNull(view: View, obj: Any?) {
    view.visibility = if (obj == null) View.GONE else View.VISIBLE
}