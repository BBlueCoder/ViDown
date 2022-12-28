package com.bluetech.vidown.core.pojoclasses

data class FetchArgs(
    var orderByNewest : Boolean = true,
    var onlyFavorites : Boolean = false
)
