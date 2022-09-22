package com.bluetech.vidown.pojoclasses


sealed class ResultItem {
    class CategoryTitle(
        val title: String
    ) : ResultItem()

    class ItemData(
        val id: Int,
        val title: String,
        val format: String,
        val url: String
    ) : ResultItem()
}
