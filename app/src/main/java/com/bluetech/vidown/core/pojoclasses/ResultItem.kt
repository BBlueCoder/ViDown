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
        val url: String
    ) : ResultItem()

    class ItemInfo(
        val title: String,
        val thumbnail : String
    ) : ResultItem()
}
