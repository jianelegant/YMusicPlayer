package com.cosmic.ymusicplayer.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.cosmic.ymusicplayer.ui.component.MiniPlayer
import com.cosmic.ymusicplayer.ui.navigation.Screen
import com.cosmic.ymusicplayer.ui.viewmodel.MainViewModel
import com.cosmic.ymusicplayer.ui.viewmodel.MusicViewModel
import com.cosmic.ymusicplayer.ui.viewmodel.SettingsViewModel

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    musicViewModel: MusicViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentSong by mainViewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by mainViewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by mainViewModel.currentPosition.collectAsStateWithLifecycle()
    val duration by mainViewModel.duration.collectAsStateWithLifecycle()
    val shuffleModeEnabled by mainViewModel.shuffleModeEnabled.collectAsStateWithLifecycle()
    val repeatMode by mainViewModel.repeatMode.collectAsStateWithLifecycle()

    val filteredSongs by musicViewModel.filteredSongs.collectAsStateWithLifecycle()
    val searchQuery by musicViewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            Column {
                // Mini Player
                AnimatedVisibility(
                    visible = currentSong != null && currentDestination?.hasRoute<Screen.Player>() != true,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    MiniPlayer(
                        title = currentSong?.mediaMetadata?.title?.toString() ?: "Unknown",
                        artist = currentSong?.mediaMetadata?.artist?.toString() ?: "Unknown",
                        albumArtUri = currentSong?.mediaMetadata?.artworkUri,
                        isPlaying = isPlaying,
                        onPlayPause = { mainViewModel.pauseResume() },
                        onClick = { navController.navigate(Screen.Player) }
                    )
                }
                
                // Bottom Navigation
                NavigationBar {
                    val items = listOf(
                        Triple(Screen.Home, "Home", Icons.Default.Home),
                        Triple(Screen.Playlists, "Playlists", Icons.Default.PlaylistPlay),
                        Triple(Screen.Library, "Library", Icons.Default.LibraryMusic),
                        Triple(Screen.Settings, "Settings", Icons.Default.Settings)
                    )
                    
                    items.forEach { (screen, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination?.hierarchy?.any { it.hasRoute(screen::class) } == true,
                            onClick = {
                                navController.navigate(screen) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Home> {
                SongListScreen(
                    songs = filteredSongs,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { musicViewModel.setSearchQuery(it) },
                    onSongClick = { index -> 
                        mainViewModel.playSongs(filteredSongs, index)
                    }
                )
            }
            composable<Screen.Playlists> {
                Text("Playlists Screen - Coming Soon", modifier = Modifier.padding(16.dp))
            }
            composable<Screen.Library> {
                Text("Library Screen (Favorites/History) - Coming Soon", modifier = Modifier.padding(16.dp))
            }
            composable<Screen.Settings> {
                val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
                SettingsScreen(
                    currentThemeMode = themeMode,
                    onThemeModeChange = { settingsViewModel.setThemeMode(it) }
                )
            }
            composable<Screen.Player> {
                PlayerScreen(
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    shuffleModeEnabled = shuffleModeEnabled,
                    repeatMode = repeatMode,
                    onPlayPause = { mainViewModel.pauseResume() },
                    onPrevious = { mainViewModel.skipToPrevious() },
                    onNext = { mainViewModel.skipToNext() },
                    onSeek = { mainViewModel.seekTo(it) },
                    onToggleShuffle = { mainViewModel.toggleShuffle() },
                    onToggleRepeat = { 
                        val nextMode = when(repeatMode) {
                            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                            else -> Player.REPEAT_MODE_OFF
                        }
                        mainViewModel.setRepeatMode(nextMode)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
