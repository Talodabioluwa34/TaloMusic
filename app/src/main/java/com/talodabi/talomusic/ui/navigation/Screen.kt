package com.talodabi.talomusic.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object NowPlaying : Screen("now_playing")
    object PlaylistDetail : Screen("playlist_detail/{playlistId}/{playlistName}") {
        fun createRoute(playlistId: Long, playlistName: String): String =
            "playlist_detail/$playlistId/${android.net.Uri.encode(playlistName)}"
    }
    object MusicLab : Screen("music_lab/{songId}") {
        fun createRoute(songId: Long): String = "music_lab/$songId"
    }
}
