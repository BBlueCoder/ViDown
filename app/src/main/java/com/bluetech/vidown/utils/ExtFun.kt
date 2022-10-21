package com.bluetech.vidown.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

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