package com.bluetech.vidown.core.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bluetech.vidown.core.MediaType
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val uid : Long,
    var savedName : String,
    val mediaType : MediaType,
    var title : String,
    var duration : Long,
    var favorite : Boolean = false
) : Parcelable
