package com.bluetech.vidown.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bluetech.vidown.core.api.ApplicationApi
import com.bluetech.vidown.core.db.AppLocalDB
import com.bluetech.vidown.utils.Constants.API_BASE_URL_EXAMPLE
import com.bluetech.vidown.utils.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val DB_MIGRATION_2_3 = object : Migration(2,3){
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER table mediaEntity add column thumbnail TEXT")
        }
    }

    private val DB_MIGRATION_3_4 = object : Migration(3,4){
        override fun migrate(database: SupportSQLiteDatabase) {
            database.beginTransaction()
            database.execSQL("alter table mediaEntity add column contentLength INTEGER")
        }
    }

    private val DB_MIGRATION_4_5 = object : Migration(4,5){
        override fun migrate(database: SupportSQLiteDatabase) {
            database.beginTransaction()
            database.execSQL("alter table mediaEntity add column downloadedLength INTEGER")
        }
    }
    private val DB_MIGRATION_5_6 = object : Migration(5,6){
        override fun migrate(database: SupportSQLiteDatabase) {
            database.beginTransaction()
            database.execSQL("alter table mediaEntity add column source TEXT default '' not null")
        }
    }
    private val DB_MIGRATION_6_7 = object : Migration(6,7){
        override fun migrate(database: SupportSQLiteDatabase) {
            database.beginTransaction()
            database.execSQL("alter table mediaEntity add column downloadSource TEXT default '' not null")
        }
    }

    @Singleton
    @Provides
    fun provideOkHTTPClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60,TimeUnit.SECONDS)
        .readTimeout(60,TimeUnit.SECONDS)
        .build()

    @Singleton
    @Provides
    fun provideApplicationApi(okHttpClient: OkHttpClient) : ApplicationApi {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL_EXAMPLE)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApplicationApi::class.java)
    }

    @Singleton
    @Provides
    fun provideAppLocalDB(
        @ApplicationContext context : Context
    ) = Room.databaseBuilder(context,AppLocalDB::class.java, DATABASE_NAME).fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideMediaDao(
        appLocalDB: AppLocalDB
    ) = appLocalDB.mediaDao()
}