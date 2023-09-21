package com.bluetech.vidown.data.repos.pojoclasses

import com.bluetech.vidown.data.db.entities.MediaType


sealed class ResultItem {
    class CategoryTitle(
        val title: String
    ) : ResultItem()

    class ItemData(
        val id: Int,
        val format: MediaType,
        val quality : String,
        val url: String,
        val hasAudio : Boolean? = null
    ) : ResultItem()

    class ItemInfo(
        val link : String,
        val title: String,
        val thumbnail : String,
        val platform : String
    ) : ResultItem()
}
