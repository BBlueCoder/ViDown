package com.bluetech.vidown.data.db.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.bluetech.vidown.data.db.entities.MediaEntity
import com.bluetech.vidown.data.db.entities.MediaThumbnail
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaWithThumbnail(
    @Embedded val mediaEntity: MediaEntity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "mediaId"
    )
    val mediaThumbnail: MediaThumbnail
) : Parcelable
