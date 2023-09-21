package com.bluetech.vidown.ui.displayscreen

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.WindowCompat
import androidx.navigation.navArgs
import com.bluetech.vidown.R
import com.bluetech.vidown.data.db.entities.MediaType
import com.bluetech.vidown.data.db.entities.MediaWithThumbnail
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import java.io.File

class DisplayMedia : AppCompatActivity() {

    private val args: DisplayMediaArgs by navArgs()

    private lateinit var media: MediaWithThumbnail

    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_media)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        media = args.mediaWithThumbnail

        try {
            if (media.mediaEntity.mediaType == MediaType.Image)
                setUpImage(media.mediaEntity.savedName)
            else
                playMedia()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun playMedia() {
        val file = File(filesDir, media.mediaEntity.savedName)

        if (!file.exists())
            throw Exception("File not found")

        media.mediaThumbnail.blurredThumbnailSavedName?.let {
            setUpImage(it)
        }

        if (media.mediaEntity.mediaType == MediaType.Audio)
            setUpAudioCover(media.mediaThumbnail.thumbnailSavedName, media.mediaEntity.title)

        val playerView = findViewById<StyledPlayerView>(R.id.player_view)

        playerView.visibility = View.VISIBLE

        initializeExoPlayer()

        playerView.player = exoPlayer
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun setUpImage(imageFileName: String) {
        val file = File(filesDir, imageFileName)
        val imageView = findViewById<ImageView>(R.id.image_view)

        imageView.visibility = View.VISIBLE

        if (file.exists()) {
            Glide.with(this)
                .load(Uri.fromFile(file))
                .into(imageView)

        }
    }

    private fun setUpAudioCover(audioCover: String, audioTitle: String) {
        val audioCoverView = findViewById<AppCompatImageView>(R.id.audio_cover)
        val audioTitleView = findViewById<TextView>(R.id.audio_title)
        val imageView = findViewById<ImageView>(R.id.image_view)

        audioTitleView.visibility = View.VISIBLE
        audioCoverView.visibility = View.VISIBLE
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        audioTitleView.text = audioTitle

        val file = File(filesDir, audioCover)
        Glide.with(this)
            .load(Uri.fromFile(file))
            .error(R.drawable.music_cover)
            .into(audioCoverView)

    }

    private fun initializeExoPlayer() {
        val trackSelector = DefaultTrackSelector(this)
        exoPlayer = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build().apply {
                trackSelectionParameters =
                    DefaultTrackSelector.Parameters.Builder(this@DisplayMedia).build()
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        error.printStackTrace()

                    }
                })
                playWhenReady = false
            }
    }

    override fun onPause() {
        super.onPause()
        pauseExoPlayer()
    }

    override fun onStop() {
        super.onStop()
        pauseExoPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        pauseExoPlayer(true)
    }

    private fun pauseExoPlayer(stopPlayer: Boolean = false) {
        if (media.mediaEntity.mediaType == MediaType.Audio || media.mediaEntity.mediaType == MediaType.Video) {
            try {
                exoPlayer.pause()
                if (stopPlayer)
                    exoPlayer.stop()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}