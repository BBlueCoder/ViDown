package com.bluetech.vidown.core.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bluetech.vidown.core.MediaType
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val uid : Int,
    var name : String,
    val mediaType : MediaType,
    var title : String,
    var thumbnail : String? = null,
    val source: String,
    val downloadSource : String,
    var duration : Long,
    var isMediaCorrupted : Boolean = false,
    var favorite : Boolean = false
) : Parcelable
