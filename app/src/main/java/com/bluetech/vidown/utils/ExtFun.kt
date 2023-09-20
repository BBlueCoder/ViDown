package com.bluetech.vidown.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import com.bluetech.vidown.core.db.entities.DownloadHistoryWithExtras
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
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

fun Long.formatDurationToReadableFormat(): String{
    val durationDate = Date((this))
    return when {
        this / 1000 < 3600 -> {
            val sdf = SimpleDateFormat("m:ss", Locale.getDefault())
            sdf.format(durationDate)
        }
        else -> {
            val sdf = SimpleDateFormat("h:mm:ss", Locale.getDefault())
            sdf.format(durationDate)
        }
    }
}

fun Context.showPermissionRequestExplanation(permission : String, message : String, retry : (()-> Unit)? = null){
    AlertDialog.Builder(this)
        .setTitle("$permission required")
        .setMessage(message)
        .setPositiveButton("OK"){_,_->
            retry?.invoke()
        }.show()
}

fun Activity.hideKeyboard(view : View){
    val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun View.toggleVisibility(visibility : Int = View.GONE){
    if(this.isVisible){
        this.visibility = visibility
        return
    }
    this.visibility = View.VISIBLE
}

fun DownloadHistoryWithExtras.calculateSize() : Long {
    var size : Long = 0
    size += downloadHistoryEntity.downloadData.sizeInBytes
    size += downloadHistoryItemExtras
        .map {
            it.downloadData.sizeInBytes
        }.reduce { a, b -> a+b }
    return size
}

fun DownloadHistoryWithExtras.calculateDownloadedSize() : Long {
    var downloadedSize : Long = 0
    downloadedSize += downloadHistoryEntity.downloadData.downloadSizeInBytes
    downloadedSize += downloadHistoryItemExtras.
    map {
        it.downloadData.downloadSizeInBytes
    }.reduce {a,b -> a + b}
    return downloadedSize
}