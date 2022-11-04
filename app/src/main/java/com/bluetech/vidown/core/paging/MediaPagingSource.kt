package com.bluetech.vidown.core.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.core.repos.DBRepo
import com.bluetech.vidown.utils.Constants.STARTING_PAGE_INDEX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaPagingSource(private val dbRepo: DBRepo) : PagingSource<Int,MediaEntity>() {

    override fun getRefreshKey(state: PagingState<Int, MediaEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaEntity> {
        println("------------------------------------------------------------load fun called")
        val position = params.key ?: STARTING_PAGE_INDEX
        return try{
            println("------------------------------ before getting data")
            withContext(Dispatchers.IO){
                println("------------------------- load size ${params.loadSize}")
                val data = dbRepo.getMedia(params.loadSize,position * params.loadSize)
                println("_____________________________ data : $data")
                LoadResult.Page(
                    data = data,
                    prevKey = null,
                    nextKey = if(data.isEmpty()) null else position + 1
                )
            }

        }catch (ex : Exception){
            println("error load paging data ${ex.printStackTrace()}")
            LoadResult.Error(ex)
        }
    }
}