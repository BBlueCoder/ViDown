package com.bluetech.vidown.core.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.core.pojoclasses.FetchArgs
import com.bluetech.vidown.core.repos.DBRepo
import com.bluetech.vidown.utils.Constants.STARTING_PAGE_INDEX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MediaPagingSource(private val dbRepo: DBRepo,private val fetchArgs: FetchArgs) : PagingSource<Int,MediaEntity>() {

    override fun getRefreshKey(state: PagingState<Int, MediaEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaEntity> {

        val position = params.key ?: STARTING_PAGE_INDEX
        return try{

            withContext(Dispatchers.IO){
                val data = dbRepo.getMedia(params.loadSize,position * params.loadSize,fetchArgs.orderByNewest,fetchArgs.onlyFavorites)
                LoadResult.Page(
                    data = data,
                    prevKey = if(position == STARTING_PAGE_INDEX) null else position-1,
                    nextKey = if(data.isEmpty()) null else position + 1
                )
            }

        }catch (ex : Exception){
            LoadResult.Error(ex)
        }
    }
}