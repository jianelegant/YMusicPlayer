package com.cosmic.ymusicplayer.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Home : Screen
    
    @Serializable
    data object Search : Screen
    
    @Serializable
    data object Playlists : Screen
    
    @Serializable
    data object Library : Screen
    
    @Serializable
    data object Player : Screen
}
