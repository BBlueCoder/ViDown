package com.bluetech.vidown.core.di

import android.content.Context
import androidx.room.Room
import com.bluetech.vidown.core.api.ApplicationApi
import com.bluetech.vidown.core.db.AppLocalDB
import com.bluetech.vidown.utils.Constants.API_BASE_URL_EXAMPLE
import com.bluetech.vidown.utils.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Singleton
    @Provides
    fun provideAppLocalDB(
        @ApplicationContext context : Context
    ) = Room.databaseBuilder(context,AppLocalDB::class.java, DATABASE_NAME).build()

    @Singleton
    @Provides
    fun provideMediaDao(
        appLocalDB: AppLocalDB
    ) = appLocalDB.mediaDao()
}