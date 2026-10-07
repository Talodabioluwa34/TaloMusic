package com.talodabi.talomusic.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.talodabi.talomusic.ui.components.MiniPlayer
import com.talodabi.talomusic.ui.components.TaloBottomNav
import com.talodabi.talomusic.ui.screens.home.HomeScreen
import com.talodabi.talomusic.ui.screens.library.LibraryScreen
import com.talodabi.talomusic.ui.screens.nowplaying.NowPlayingScreen
import com.talodabi.talomusic.ui.screens.playlist.PlaylistDetailScreen
import com.talodabi.talomusic.ui.screens.search.SearchScreen
import com.talodabi.talomusic.ui.screens.settings.SettingsScreen
import com.talodabi.talomusic.ui.theme.TaloTheme
import com.talodabi.talomusic.ui.viewmodel.MusicViewModel

@Composable
fun TaloAppScaffold(
    viewModel: MusicViewModel,
    musicLabViewModel: com.talodabi.talomusic.ui.screens.musiclab.MusicLabViewModel? = null,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val playbackState by viewModel.playbackState.collectAsState()

    var isNowPlayingExpanded by remember { mutableStateOf(false) }

    val isTopLevelRoute = currentRoute in listOf(
        Screen.Home.route,
        Screen.Library.route,
        Screen.Search.route,
        Screen.Settings.route
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TaloTheme.colors.background)
    ) {
        Scaffold(
            bottomBar = {
                if (isTopLevelRoute && !isNowPlayingExpanded) {
                    Column {
                        // Persistent Mini Player above Bottom Nav
                        if (playbackState.currentSong != null) {
                            MiniPlayer(
                                playbackState = playbackState,
                                onExpand = { isNowPlayingExpanded = true },
                                onPlayPause = { viewModel.togglePlayPause() },
                                onSkipNext = { viewModel.skipToNext() }
                            )
                        }

                        TaloBottomNav(
                            currentRoute = currentRoute,
                            onNavigate = { screen ->
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            },
            containerColor = TaloTheme.colors.background
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onPlaylistClick = { id, name ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(id, name))
                        }
                    )
                }

                composable(Screen.Library.route) {
                    LibraryScreen(viewModel = viewModel)
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        viewModel = viewModel,
                        onPlaylistClick = { id, name ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(id, name))
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }

                composable(
                    route = Screen.PlaylistDetail.route,
                    arguments = listOf(
                        navArgument("playlistId") { type = NavType.LongType },
                        navArgument("playlistName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
                    val playlistName = backStackEntry.arguments?.getString("playlistName") ?: "Playlist"
                    PlaylistDetailScreen(
                        playlistId = playlistId,
                        playlistName = playlistName,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.MusicLab.route,
                    arguments = listOf(
                        navArgument("songId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    val songId = backStackEntry.arguments?.getLong("songId") ?: 0L
                    if (musicLabViewModel != null) {
                        com.talodabi.talomusic.ui.screens.musiclab.MusicLabScreen(
                            songId = songId,
                            viewModel = musicLabViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }

        // Animated full-screen Now Playing modal overlay
        AnimatedVisibility(
            visible = isNowPlayingExpanded && playbackState.currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            NowPlayingScreen(
                viewModel = viewModel,
                onDismiss = { isNowPlayingExpanded = false },
                onOpenMusicLab = { songId ->
                    isNowPlayingExpanded = false
                    navController.navigate(Screen.MusicLab.createRoute(songId))
                }
            )
        }
    }
}
