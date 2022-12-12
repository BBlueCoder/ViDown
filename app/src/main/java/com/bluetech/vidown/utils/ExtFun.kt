package com.bluetech.vidown.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

fun View.snackBar(message: String) {
    Snackbar.make(
        this,
        message,
        Snackbar.LENGTH_SHORT
    ).also { snackBar ->
        snackBar.setAction("OK") {
            snackBar.dismiss()
        }
    }.show()
}

fun Long.formatSizeToReadableFormat(): String{
    val size = this
    if (size <= 1) return "0"
    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
}