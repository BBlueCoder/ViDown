package com.bluetech.vidown.data.repos

import com.bluetech.vidown.data.repos.pojoclasses.ResultItem
import kotlinx.coroutines.flow.Flow

abstract class BaseRepo {

    abstract suspend fun getResultsAsFlow(url : String) : Flow<Result<List<ResultItem>>>

}