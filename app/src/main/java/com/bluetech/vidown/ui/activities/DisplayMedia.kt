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
import java.io.File

class DisplayMedia : AppCompatActivity() {

    private val args : DisplayMediaArgs by navArgs()

    private lateinit var media : MediaEntity

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

        val videoView = findViewById<VideoView>(R.id.video_view)

        videoView.visibility = View.VISIBLE

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)

        videoView.setMediaController(mediaController)
        videoView.setVideoURI(Uri.fromFile(file))
        videoView.requestFocus()
        videoView.start()
    }

    private fun setUpAudio(){
        val file = File(filesDir,media.name)

        if(!file.exists())
            throw Exception("File not found")
        val videoView = findViewById<VideoView>(R.id.video_view)

        videoView.visibility = View.VISIBLE

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)

        videoView.setMediaController(mediaController)
        videoView.setVideoURI(Uri.fromFile(file))
        videoView.requestFocus()
        videoView.start()
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
}