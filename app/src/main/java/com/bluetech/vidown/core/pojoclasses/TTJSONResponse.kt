package com.bluetech.vidown.core.pojoclasses

import com.google.gson.annotations.SerializedName

data class TTJSONResponse(
    @SerializedName("url_type") val type : String,
    @SerializedName("album_title") val albumTitle : String?,
    @SerializedName("album_music_title") val albumMusicTitle : String?,
    @SerializedName("album_music_url") val albumMusicUrl : String?,
    @SerializedName("album_list") val albumUrls : List<String>?,
    @SerializedName("nwm_video_url") val videoUrlWithoutWatermark : String?,
    @SerializedName("wm_video_url") val videoUrl : String?,
    @SerializedName("video_title") val videoTitle : String?,
    @SerializedName("video_cover") val videoThumbnail : String?,
    @SerializedName("video_music_url") val videoMusicUrl : String?,
    @SerializedName("video_music_title") val videoMusicTitle : String?
)
