package com.bluetech.vidown.core.pojoclasses

import com.google.gson.annotations.SerializedName

data class InstaJSONResponse(
    val graphql : InstaGraphql
)

data class InstaGraphql(
    @SerializedName("shortcode_media") val InstaMedia : InstaMedia
)

data class InstaMedia(
    @SerializedName("__typename") val typeName : String,
    @SerializedName("display_url") val thumbnail : String,
    @SerializedName("video_url") val videoUrl : String?,
    @SerializedName("edge_media_to_caption") val mediaCaption : InstaMediaCaption,
    @SerializedName("edge_sidecar_to_children") val mediaSideCar : InstaMediaSideCar
)

data class InstaMediaSideCar(
    val edges : List<InstaEdgeNodeSide>
)

data class InstaEdgeNodeSide(
    val node : InstaNodeSide
)

data class InstaNodeSide(
    @SerializedName("__typename") val typeName: String,
    @SerializedName("display_url") val thumbnail : String,
    @SerializedName("video_url") val videoUrl : String?,
)

data class InstaMediaCaption(
    @SerializedName("edges") val edges : List<InstaEdgeNode>
)

data class InstaEdgeNode(
    val node : InstaNode
)

data class InstaNode(
    val text : String
)