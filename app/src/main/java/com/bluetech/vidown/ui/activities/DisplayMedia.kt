package com.bluetech.vidown.ui.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.navigation.navArgs
import com.bluetech.vidown.R
import java.io.File

class DisplayMedia : AppCompatActivity() {

    private val args : DisplayMediaArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_media)

        val media = args.mediaEntity
        val file = File(filesDir,media.name)
        val videoView = findViewById<VideoView>(R.id.video_view)

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)

        videoView.setMediaController(mediaController)
        videoView.setVideoURI(Uri.fromFile(file))
        videoView.requestFocus()
        videoView.start()
    }
}