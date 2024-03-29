package com.bluetech.vidown.utils

import android.os.Build
import java.time.Instant
import java.time.format.DateTimeFormatter

object Constants {

    const val DATABASE_NAME = "vidown_db"

    const val TWITTER_SSS_BASE_URL = "https://ssstwitter.com/"
    const val TT_BASE_URL_API = "https://api.douyin.wtf/api?url="
    const val API_BASE_URL_EXAMPLE = "https://vidown.com/"

    const val NOTIFICATION_CHANNEL_ID = "ViDown notification channel"
    const val FILE_PREFIX_NAME = "vidown_"

    const val DOWNLOAD_SERVICE_ACTION = "DOWNLOAD_SERVICE"
    const val DOWNLOAD_FILE_PROGRESS_ACTION = "download_progress_action"

    const val STARTING_PAGE_INDEX = 0

    const val YOUTUBE = "youtube"
    const val INSTAGRAM = "instagram"
    const val TIKTOK = "tiktok"

    const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36"

    fun generateFileName(): String {
        return "${Constants.FILE_PREFIX_NAME}${getTimeInMillis()}"
    }

    fun getTimeInMillis() : Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ISO_INSTANT.format(Instant.now()).toLong()
        } else {
            System.currentTimeMillis()
        }
    }
}