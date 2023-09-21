package com.bluetech.vidown.core.di

import android.content.Context
import androidx.room.Room
import com.bluetech.vidown.data.api.ApplicationApi
import com.bluetech.vidown.data.db.AppLocalDB
import com.bluetech.vidown.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Named("test_db")
    fun provideDatabase(@ApplicationContext context : Context) : AppLocalDB {
        return Room.inMemoryDatabaseBuilder(
            context,
            AppLocalDB::class.java
        ).build()
    }

}