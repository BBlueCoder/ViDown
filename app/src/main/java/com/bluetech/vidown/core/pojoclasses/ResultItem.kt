package com.bluetech.vidown.core.pojoclasses

import com.bluetech.vidown.core.MediaType


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
