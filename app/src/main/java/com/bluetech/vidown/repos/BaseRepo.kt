package com.bluetech.vidown.repos

import com.bluetech.vidown.pojoclasses.ResultItem
import kotlinx.coroutines.flow.Flow

abstract class BaseRepo {

    abstract fun getResultsAsFlow(url : String) : Flow<Result<List<ResultItem>>>

}