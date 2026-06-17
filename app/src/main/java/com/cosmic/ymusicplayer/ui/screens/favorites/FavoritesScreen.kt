package com.cosmic.ymusicplayer.ui.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cosmic.ymusicplayer.data.model.Song
import com.cosmic.ymusicplayer.ui.components.SongListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onPlaySong: (List<Song>, Int) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    val history by viewModel.history.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Favorites") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Favorites") },
                icon = { Icon(Icons.Default.Favorite, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("History") },
                icon = { Icon(Icons.Default.History, contentDescription = null) }
            )
        }

        when (selectedTab) {
            0 -> SongListContent(
                songs = favorites,
                emptyMessage = "No favorites yet",
                onPlaySong = onPlaySong,
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onAddToPlaylist = onAddToPlaylist
            )
            1 -> SongListContent(
                songs = history,
                emptyMessage = "No play history",
                onPlaySong = onPlaySong,
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onAddToPlaylist = onAddToPlaylist
            )
        }
    }
}

@Composable
private fun SongListContent(
    songs: List<Song>,
    emptyMessage: String,
    onPlaySong: (List<Song>, Int) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit
) {
    if (songs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = songs,
                key = { it.id }
            ) { song ->
                SongListItem(
                    song = song,
                    onPlay = { onPlaySong(songs, songs.indexOf(song)) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onAddToPlaylist = { onAddToPlaylist(song) }
                )
            }
        }
    }
}
