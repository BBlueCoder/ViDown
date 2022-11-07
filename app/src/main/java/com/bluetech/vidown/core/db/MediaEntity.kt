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
    val name : String,
    val mediaType : MediaType,
    val title : String,
    val thumbnail : String? = null
) : Parcelable
