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
    val title : String,
    val thumbnail : String? = null,
    var contentLength : Long?,
    var downloadedLength : Long?,
    val source: String,
    val downloadSource : String
) : Parcelable
