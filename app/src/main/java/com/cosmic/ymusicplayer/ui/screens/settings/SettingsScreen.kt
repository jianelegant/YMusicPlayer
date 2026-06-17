package com.cosmic.ymusicplayer.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosmic.ymusicplayer.util.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Theme
            item {
                ListItem(
                    headlineContent = { Text("Theme mode") },
                    supportingContent = {
                        Text(
                            when (themeMode) {
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                                ThemeMode.SYSTEM -> "Follow system"
                            }
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Default.Palette, contentDescription = null)
                    },
                    modifier = Modifier.clickable { showThemeDialog = true }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            // Rescan
            item {
                ListItem(
                    headlineContent = { Text("Rescan library") },
                    supportingContent = { Text("Re-scan device for music files") },
                    leadingContent = {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    },
                    modifier = Modifier.clickable { viewModel.rescanLibrary() }
                )
            }

            if (isScanning) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Scanning…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    // Theme dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select theme") },
            text = {
                Column {
                    ThemeOption("Light", ThemeMode.LIGHT, themeMode) {
                        viewModel.setThemeMode(it)
                        showThemeDialog = false
                    }
                    ThemeOption("Dark", ThemeMode.DARK, themeMode) {
                        viewModel.setThemeMode(it)
                        showThemeDialog = false
                    }
                    ThemeOption("Follow system", ThemeMode.SYSTEM, themeMode) {
                        viewModel.setThemeMode(it)
                        showThemeDialog = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    mode: ThemeMode,
    currentMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(mode) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = currentMode == mode,
            onClick = { onSelect(mode) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
