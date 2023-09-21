package com.bluetech.vidown.workers

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GetDurationOfMedia(private val context: Context, private val savedName: String) {

    suspend operator fun invoke(): Long {
        val file = File(context.filesDir, savedName)

        val duration = suspendCoroutine<Long> { coroutine ->
            ExoPlayer.Builder(context).build().also {
                it.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                it.prepare()
                it.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            it.removeListener(this)
                            it.release()
                            coroutine.resume(it.duration)
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        coroutine.resume(0)
                    }
                })
            }

        }
        return duration
    }
}
