package com.bluetech.vidown.api

import com.bluetech.vidown.pojoclasses.InstaJSONResponse
import com.bluetech.vidown.pojoclasses.TTJSONResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface ApplicationApi {

    @GET
    suspend fun getInstaPostJSONData(@Url url : String) : Response<InstaJSONResponse>

    @GET
    suspend fun getTTMedia(@Url url : String): Response<TTJSONResponse>
}