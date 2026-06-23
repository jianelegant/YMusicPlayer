package com.cosmic.ymusicplayer.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cosmic.ymusicplayer.YMusicPlayerApp
import com.cosmic.ymusicplayer.data.model.Song
import com.cosmic.ymusicplayer.ui.components.MiniPlayer
import com.cosmic.ymusicplayer.ui.screens.favorites.FavoritesScreen
import com.cosmic.ymusicplayer.ui.screens.favorites.FavoritesViewModel
import com.cosmic.ymusicplayer.ui.screens.library.LibraryScreen
import com.cosmic.ymusicplayer.ui.screens.library.LibraryViewModel
import com.cosmic.ymusicplayer.ui.screens.nowplaying.NowPlayingScreen
import com.cosmic.ymusicplayer.ui.screens.nowplaying.NowPlayingViewModel
import com.cosmic.ymusicplayer.ui.screens.playlists.PlaylistViewModel
import com.cosmic.ymusicplayer.ui.screens.playlists.PlaylistsScreen
import com.cosmic.ymusicplayer.ui.screens.settings.SettingsScreen
import com.cosmic.ymusicplayer.ui.screens.settings.SettingsViewModel

data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val navItems = listOf(
    NavItem("library", "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    NavItem("playlists", "Playlists", Icons.Filled.PlaylistPlay, Icons.Outlined.PlaylistPlay),
    NavItem("favorites", "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    NavItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    app: YMusicPlayerApp
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val nowPlayingViewModel: NowPlayingViewModel = viewModel(
        factory = NowPlayingViewModel.Factory(app.musicPlayerFlow, app.musicRepository)
    )
    val libraryViewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModel.Factory(app.musicRepository, app.playlistRepository)
    )
    val playlistViewModel: PlaylistViewModel = viewModel(
        factory = PlaylistViewModel.Factory(app.playlistRepository)
    )
    val favoritesViewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModel.Factory(app.musicRepository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(app.themeManager, app.musicRepository)
    )

    val playerState by nowPlayingViewModel.playerState.collectAsState()
    val showAddToPlaylistSong by playlistViewModel.showAddToPlaylistDialog.collectAsState()

    // Shared add-to-playlist handler
    val onAddToPlaylist: (Song) -> Unit = { song ->
        playlistViewModel.showAddToPlaylistDialog(song)
    }

    // Check if NowPlaying screen is showing
    val isNowPlayingScreen = currentRoute == "now_playing"

    Scaffold(
        bottomBar = {
            if (!isNowPlayingScreen) {
                Column {
                    // Mini player above nav bar
                    MiniPlayer(
                        playerState = playerState,
                        onPlayPause = { nowPlayingViewModel.togglePlayPause() },
                        onSkipNext = { nowPlayingViewModel.skipToNext() },
                        onOpenNowPlaying = {
                            navController.navigate("now_playing") {
                                launchSingleTop = true
                            }
                        }
                    )

                    NavigationBar {
                        navItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "library",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("library") {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onPlaySong = { songs, index ->
                        nowPlayingViewModel.playSong(songs, index)
                    },
                    onAddToPlaylist = onAddToPlaylist
                )
            }

            composable("playlists") {
                PlaylistsScreen(
                    viewModel = playlistViewModel,
                    onPlaySong = { songs, index ->
                        nowPlayingViewModel.playSong(songs, index)
                    },
                    onAddToPlaylist = onAddToPlaylist
                )
            }

            composable("favorites") {
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onPlaySong = { songs, index ->
                        nowPlayingViewModel.playSong(songs, index)
                    },
                    onAddToPlaylist = onAddToPlaylist
                )
            }

            composable("settings") {
                SettingsScreen(viewModel = settingsViewModel)
            }

            composable("now_playing") {
                NowPlayingScreen(
                    viewModel = nowPlayingViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    // Add to playlist dialog
    showAddToPlaylistSong?.let { song ->
        AddToPlaylistDialog(
            playlists = playlistViewModel.playlists.collectAsState().value,
            onDismiss = { playlistViewModel.hideAddToPlaylistDialog() },
            onSelect = { playlist ->
                playlistViewModel.addToPlaylistAndDismiss(playlist.id, song)
            }
        )
    }
}

@Composable
private fun AddToPlaylistDialog(
    playlists: List<com.cosmic.ymusicplayer.data.model.Playlist>,
    onDismiss: () -> Unit,
    onSelect: (com.cosmic.ymusicplayer.data.model.Playlist) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to playlist") },
        text = {
            if (playlists.isEmpty()) {
                Text("No playlists yet. Create one first!")
            } else {
                Column {
                    playlists.forEach { playlist ->
                        TextButton(
                            onClick = { onSelect(playlist) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(playlist.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
