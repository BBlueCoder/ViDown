package com.bluetech.vidown.core.di

import com.bluetech.vidown.core.api.ApplicationApi
import com.bluetech.vidown.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Named("test_api")
    fun provideApplicationApi() : ApplicationApi {
        return Retrofit.Builder()
            .baseUrl(Constants.API_BASE_URL_EXAMPLE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApplicationApi::class.java)
    }

}