package com.bluetech.vidown.data.repos.pojoclasses

import com.google.gson.annotations.SerializedName

data class TTJSONResponse(
    val status : String,
    val type : String,
    val desc : String,
    val music : TTMusic,
    @SerializedName("cover_data") val thumbnails : Thumbnails,
    @SerializedName("image_data") val imageData: ImageData?,
    @SerializedName("video_data") val videoData: VideoData?
)

data class TTMusic(
    val title : String,
    @SerializedName("cover_hd") val hd : Cover,
    @SerializedName("cover_large") val large : Cover,
    @SerializedName("cover_medium") val medium : Cover,
    @SerializedName("cover_thumb") val thumb : Cover,
    @SerializedName("play_url") val source : Cover
)

data class Thumbnails(
    @SerializedName("cover") val source : Cover,
    @SerializedName("dynamic_cover") val dynamicSource : Cover
)

data class Cover(
    val uri : String,
    @SerializedName("url_list") val urlList : List<String>,
    val width : Int,
    val height : Int,
)

data class ImageData(
    @SerializedName("no_watermark_image_list") val imagesWithoutWatermark : List<String>,
    @SerializedName("watermark_image_list") val images : List<String>
)

data class VideoData(
    @SerializedName("wm_video_url") val video : String,
    @SerializedName("wm_video_url_HQ") val videoHQ : String,
    @SerializedName("nwm_video_url") val videoWithoutWatermark : String,
    @SerializedName("nwm_video_url_HQ") val videoWithoutWatermarkHQ : String
)

