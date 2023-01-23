package com.bluetech.vidown.ui.activities

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.navigation.navArgs
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.MediaEntity
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import java.io.File

class DisplayMedia : AppCompatActivity() {

    private val args : DisplayMediaArgs by navArgs()

    private lateinit var media : MediaEntity

    private lateinit var exoPlayer : ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_media)

        media = args.mediaEntity

        try{
            when(media.mediaType){
                MediaType.Video-> setUpVideo()
                MediaType.Image->setUpImage()
                MediaType.Audio->setUpAudio()
            }
        }catch (ex :Exception){
            ex.printStackTrace()
        }


    }

    private fun setUpVideo(){
        val file = File(filesDir,media.name)

        if(!file.exists())
            throw Exception("File not found")

        val videoView = findViewById<StyledPlayerView>(R.id.video_view)

        videoView.visibility = View.VISIBLE

        val trackSelector = DefaultTrackSelector(this)
        exoPlayer = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build().apply {
                trackSelectionParameters = DefaultTrackSelector.Parameters.Builder(this@DisplayMedia).build()
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        error.printStackTrace()

                    }
                })
                playWhenReady = false
            }

        videoView.player = exoPlayer
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun setUpAudio(){
        val file = File(filesDir,media.name)

        if(!file.exists())
            throw Exception("File not found")
        val videoView = findViewById<StyledPlayerView>(R.id.video_view)

        videoView.visibility = View.VISIBLE

        val trackSelector = DefaultTrackSelector(this)
        exoPlayer = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build().apply {
                trackSelectionParameters = DefaultTrackSelector.Parameters.Builder(this@DisplayMedia).build()
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        error.printStackTrace()

                    }
                })
                playWhenReady = false
            }

        videoView.player = exoPlayer
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun setUpImage(){
        val file = File(filesDir,media.name)
        val imageView = findViewById<ImageView>(R.id.image_view)

        imageView.visibility = View.VISIBLE

        if (file.exists()) {
            Glide.with(this)
                .load(Uri.fromFile(file))
                .error(R.drawable.ic_video_corrupted)
                .into(imageView)
        }
    }

    override fun onPause() {
        super.onPause()
        println("------------------------------- onpause")
        exoPlayer.pause()
    }

    override fun onStop() {
        super.onStop()
        println("------------------------------- onstop")
        exoPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        println("------------------------------- ondestroy")
        exoPlayer.stop()
        exoPlayer.release()
    }
}