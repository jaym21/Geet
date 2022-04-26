package dev.jaym21.geet.di

import android.app.Application
import android.app.NotificationManager
import android.content.ComponentName
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.jaym21.geet.db.GeetDatabase
import dev.jaym21.geet.playback.PlaybackService
import dev.jaym21.geet.playback.player.MusicPlayer
import dev.jaym21.geet.playback.player.PlaybackSessionConnector
import dev.jaym21.geet.playback.player.Queue
import dev.jaym21.geet.playback.player.SongPlayer
import dev.jaym21.geet.repository.*
import dev.jaym21.geet.utils.NotificationGenerator
import kotlinx.coroutines.DelicateCoroutinesApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideDatabase(application: Application): GeetDatabase =
        Room.databaseBuilder(application, GeetDatabase::class.java, "geet_database").build()

    @DelicateCoroutinesApi
    @Provides
    @Singleton
    fun providePlaybackService(): PlaybackService =
        PlaybackService()

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
    fun provideArtistRepository(application: Application) =
        ArtistRepository(application)

    @Provides
    @Singleton
    fun provideGenreRepository(application: Application) =
        GenreRepository(application)

    @Provides
    @Singleton
    fun providePlaylistRepository(application: Application) =
        PlaylistRepository(application)

    @Provides
    @Singleton
    fun provideAlbumRepository(application: Application) =
        AlbumRepository(application)

    @Provides
    @Singleton
    fun provideMusicPlayer(application: Application) =
        MusicPlayer(application)

    @Provides
    @Singleton
    fun provideQueue(application: Application, songsRepository: SongsRepository, geetDatabase: GeetDatabase) =
        Queue(application, songsRepository, geetDatabase.queueDao())

        @Provides
    @Singleton
    fun provideSongPlayer(application: Application, musicPlayer: MusicPlayer, songsRepository: SongsRepository, geetDatabase: GeetDatabase, queue: Queue) =
        SongPlayer(application, musicPlayer, songsRepository, geetDatabase.queueDao(), queue)

    @Provides
    @Singleton
    fun provideNotificationGenerator(application: Application) =
        NotificationGenerator(application)

    @DelicateCoroutinesApi
    @Provides
    @Singleton
    fun provideComponentName(application: Application, playbackService: PlaybackService) =
        ComponentName(application, playbackService::class.java)

    @Provides
    @Singleton
    fun providePlaybackSessionConnector(application: Application, componentName: ComponentName) =
        PlaybackSessionConnector(application, componentName)
}