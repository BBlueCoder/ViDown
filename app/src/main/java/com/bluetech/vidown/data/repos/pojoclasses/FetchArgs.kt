package com.bluetech.vidown.data.repos.pojoclasses

data class FetchArgs(
    var orderByNewest : Boolean = true,
    var onlyFavorites : Boolean = false
)
