package com.bluetech.vidown.core.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.bluetech.vidown.core.db.entities.DownloadHistoryItemExtras
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import jp.wasabeef.glide.transformations.BlurTransformation
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BlurImage(
    private val context: Context,
    private val imageSavedName : String
) {

    suspend operator fun invoke(): String? {
        val file = File(context.filesDir, imageSavedName)
        return suspendCoroutine {
            Glide.with(context)
                .load(file)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(60)))
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        try {
                            val savedName = "blurred_${imageSavedName}"
                            val blurredImageFile = File(context.filesDir, savedName)
                            val imageBitmap = resource.toBitmap(500, 600)
                            blurredImageFile.outputStream().use { os ->
                                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, os)
                            }
                            it.resume(savedName)
                        } catch (ex: Exception) {
                            it.resume(null)
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        it.resume(null)
                    }
                })
        }
    }
}