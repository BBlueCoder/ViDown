package com.bluetech.vidown.core.pojoclasses

import com.bluetech.vidown.core.db.entities.MediaEntity
import com.bluetech.vidown.core.db.entities.MediaWithThumbnail

data class SelectItem(
    val mediaWithThumbnail: MediaWithThumbnail,
    val position : Int
)
