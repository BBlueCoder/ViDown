package com.bluetech.vidown.data.repos.pojoclasses

import com.bluetech.vidown.data.db.entities.MediaEntity
import com.bluetech.vidown.data.db.entities.MediaWithThumbnail

data class SelectItem(
    val mediaWithThumbnail: MediaWithThumbnail,
    val position : Int
)
