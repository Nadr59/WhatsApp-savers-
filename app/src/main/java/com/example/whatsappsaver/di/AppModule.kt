package com.example.whatsappsaver.di
import android.content.Context
import androidx.room.Room
import com.example.whatsappsaver.data.local.AppDatabase
import com.example.whatsappsaver.data.local.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun provideDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "whatsapp_saver_db").build()
    @Provides fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()
}
