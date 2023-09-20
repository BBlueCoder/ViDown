package com.bluetech.vidown.core.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class MediaThumbnail(
    @PrimaryKey(autoGenerate = true) val uid : Long,
    val mediaId : Long,
    val thumbnailSavedName : String,
    val blurredThumbnailSavedName : String?
) : Parcelable
