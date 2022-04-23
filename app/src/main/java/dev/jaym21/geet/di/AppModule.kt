package dev.jaym21.geet.di

import android.app.Application
import android.app.NotificationManager
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.jaym21.geet.db.GeetDatabase
import dev.jaym21.geet.playback.player.MusicPlayer
import dev.jaym21.geet.playback.player.Queue
import dev.jaym21.geet.playback.player.SongPlayer
import dev.jaym21.geet.repository.QueueRepository
import dev.jaym21.geet.repository.SongsRepository
import dev.jaym21.geet.utils.NotificationGenerator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideDatabase(application: Application): GeetDatabase =
        Room.databaseBuilder(application, GeetDatabase::class.java, "geet_database").build()

    @Provides
    @Singleton
    fun provideSongRepository(application: Application): SongsRepository =
        SongsRepository(application.applicationContext)

    @Provides
    @Singleton
    fun provideQueueRepository(geetDatabase: GeetDatabase, songsRepository: SongsRepository) =
        QueueRepository(geetDatabase.queueDao(), songsRepository)

    @Provides
    @Singleton
    fun provideMusicPlayer(application: Application) =
        MusicPlayer(application)

    @Provides
    @Singleton
    fun provideSongPlayer(application: Application, musicPlayer: MusicPlayer, songsRepository: SongsRepository, geetDatabase: GeetDatabase, queue: Queue) =
        SongPlayer(application, musicPlayer, songsRepository, geetDatabase.queueDao(), queue)

    @Provides
    @Singleton
    fun provideNotificationGenerator(application: Application, notificationManager: NotificationManager) =
        NotificationGenerator(application, notificationManager)
}