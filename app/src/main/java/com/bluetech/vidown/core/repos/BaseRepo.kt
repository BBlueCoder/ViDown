package com.bluetech.vidown.core.repos

import com.bluetech.vidown.core.pojoclasses.ResultItem
import kotlinx.coroutines.flow.Flow

abstract class BaseRepo {

    abstract suspend fun getResultsAsFlow(url : String) : Flow<Result<List<ResultItem>>>

}