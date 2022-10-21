package com.bluetech.vidown.core.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bluetech.vidown.core.MediaType

@Entity
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val uid : Int,
    val name : String,
    val mediaType : MediaType
)
