package com.tinko.unizaexamwatchdog.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("formatDate")
fun formatDate(view: TextView, date: Date) {
    val pattern = "dd.MM. yyyy - HH:mm"
    val formatter = SimpleDateFormat(pattern)
    view.text = formatter.format(date)
}