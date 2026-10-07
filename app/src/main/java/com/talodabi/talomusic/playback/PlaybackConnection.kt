package com.talodabi.talomusic.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.talodabi.talomusic.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackConnection(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playbackState = MutableStateFlow(PlaybackUiState())
    val playbackState: StateFlow<PlaybackUiState> = _playbackState.asStateFlow()

    private var currentSongList: List<Song> = emptyList()
    private var positionUpdateJob: Job? = null

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                setupPlayerListener()
                updateStateFromController()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        val controller = mediaController ?: return
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateStateFromController()
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateStateFromController()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateStateFromController()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playbackState.value = _playbackState.value.copy(isShuffleEnabled = shuffleModeEnabled)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                val mode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
                _playbackState.value = _playbackState.value.copy(repeatMode = mode)
            }
        })
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        val currentPos = controller.currentPosition.coerceAtLeast(0L)
                        val state = _playbackState.value

                        // A-B Loop Section repetition enforcement
                        if (state.isLoopEnabled && state.loopStartMs != null && state.loopEndMs != null) {
                            if (currentPos >= state.loopEndMs || currentPos < state.loopStartMs) {
                                controller.seekTo(state.loopStartMs)
                            }
                        }

                        _playbackState.value = state.copy(
                            currentPositionMs = currentPos,
                            durationMs = controller.duration.coerceAtLeast(0L),
                            bufferedPositionMs = controller.bufferedPosition.coerceAtLeast(0L)
                        )
                    }
                }
                delay(200)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    private fun updateStateFromController() {
        val controller = mediaController ?: return
        val currentIdx = controller.currentMediaItemIndex
        val currentSong = currentSongList.getOrNull(currentIdx)

        _playbackState.value = _playbackState.value.copy(
            currentSong = currentSong,
            isPlaying = controller.isPlaying,
            currentPositionMs = controller.currentPosition.coerceAtLeast(0L),
            durationMs = controller.duration.coerceAtLeast(0L),
            queue = currentSongList,
            queueIndex = currentIdx,
            isShuffleEnabled = controller.shuffleModeEnabled
        )
    }

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        val controller = mediaController ?: return
        currentSongList = queue
        val mediaItems = queue.map { item ->
            val metadata = MediaMetadata.Builder()
                .setTitle(item.title)
                .setArtist(item.artist)
                .setAlbumTitle(item.album)
                .setArtworkUri(item.artworkUri?.let { Uri.parse(it) })
                .build()

            MediaItem.Builder()
                .setMediaId(item.id.toString())
                .setUri(Uri.parse(item.contentUri))
                .setMediaMetadata(metadata)
                .build()
        }

        val targetIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        controller.setMediaItems(mediaItems, targetIndex, 0L)
        controller.prepare()
        controller.play()
        updateStateFromController()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        mediaController?.let { controller ->
            if (controller.currentPosition > 3000) {
                controller.seekTo(0)
            } else {
                controller.seekToPreviousMediaItem()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
    }

    fun toggleShuffle() {
        mediaController?.let { controller ->
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        }
    }

    fun cycleRepeatMode() {
        mediaController?.let { controller ->
            val nextMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            controller.repeatMode = nextMode
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
    }

    fun setLoop(startMs: Long, endMs: Long) {
        _playbackState.value = _playbackState.value.copy(
            loopStartMs = startMs,
            loopEndMs = endMs,
            isLoopEnabled = true
        )
    }

    fun toggleLoop() {
        _playbackState.value = _playbackState.value.copy(
            isLoopEnabled = !_playbackState.value.isLoopEnabled
        )
    }

    fun clearLoop() {
        _playbackState.value = _playbackState.value.copy(
            loopStartMs = null,
            loopEndMs = null,
            isLoopEnabled = false
        )
    }

    fun release() {
        stopPositionUpdates()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}
