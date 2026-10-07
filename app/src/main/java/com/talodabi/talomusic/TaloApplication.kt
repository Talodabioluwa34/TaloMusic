package com.talodabi.talomusic

import android.app.Application
import com.talodabi.talomusic.data.local.TaloDatabase
import com.talodabi.talomusic.data.repository.MediaStoreScanner
import com.talodabi.talomusic.data.repository.MusicRepository
import com.talodabi.talomusic.data.repository.SettingsRepository
import com.talodabi.talomusic.playback.PlaybackConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TaloApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var database: TaloDatabase
        private set
    lateinit var mediaStoreScanner: MediaStoreScanner
        private set
    lateinit var musicRepository: MusicRepository
        private set
    lateinit var playbackConnection: PlaybackConnection
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var analysisRepository: com.talodabi.talomusic.data.repository.AnalysisRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = TaloDatabase.getInstance(this)
        mediaStoreScanner = MediaStoreScanner(this)
        settingsRepository = SettingsRepository(this)
        analysisRepository = com.talodabi.talomusic.data.repository.AnalysisRepository(
            analysisDao = database.analysisDao(),
            analysisEngine = com.talodabi.talomusic.data.analysis.AudioAnalysisEngine()
        )

        musicRepository = MusicRepository(
            scanner = mediaStoreScanner,
            favoriteDao = database.favoriteDao(),
            playlistDao = database.playlistDao(),
            recentPlayDao = database.recentPlayDao(),
            scope = applicationScope
        )

        playbackConnection = PlaybackConnection(this, applicationScope)
    }

    override fun onTerminate() {
        playbackConnection.release()
        super.onTerminate()
    }
}
