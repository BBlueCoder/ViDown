package com.bluetech.vidown.core.pojoclasses


sealed class ResultItem {
    class CategoryTitle(
        val title: String
    ) : ResultItem()

    class ItemData(
        val id: Int,
        val format: String,
        val quality : String,
        val url: String
    ) : ResultItem()

    class ItemInfo(
        val title: String,
        val thumbnail : String
    ) : ResultItem()
}
