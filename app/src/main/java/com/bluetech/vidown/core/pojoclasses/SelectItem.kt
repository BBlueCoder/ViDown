package com.bluetech.vidown.core.pojoclasses

import com.bluetech.vidown.core.db.MediaEntity

data class SelectItem(
    val mediaEntity: MediaEntity,
    val position : Int
)
