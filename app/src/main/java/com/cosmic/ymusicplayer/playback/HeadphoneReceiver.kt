package com.cosmic.ymusicplayer.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HeadphoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Handled by MusicPlaybackService's internal receiver
        // This BroadcastReceiver is registered in the manifest for system broadcasts
    }
}
