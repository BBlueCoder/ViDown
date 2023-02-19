package com.bluetech.vidown.core.api

import com.bluetech.vidown.core.pojoclasses.InstaJSONResponse
import com.bluetech.vidown.core.pojoclasses.TTJSONResponse
import com.bluetech.vidown.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url

interface ApplicationApi {

    @GET
    suspend fun getInstaPostJSONData(@Url url : String) : Response<InstaJSONResponse>

    @Headers("User-Agent: ${Constants.USER_AGENT}")
    @GET
    suspend fun getTTMedia(@Url url : String): Response<TTJSONResponse>
}