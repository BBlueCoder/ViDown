package com.bluetech.vidown.di

import com.bluetech.vidown.api.ApplicationApi
import com.bluetech.vidown.utils.Constants.API_BASE_URL_EXAMPLE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplicationApi() : ApplicationApi {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL_EXAMPLE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApplicationApi::class.java)
    }

}