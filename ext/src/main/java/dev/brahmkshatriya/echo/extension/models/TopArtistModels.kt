package dev.brahmkshatriya.echo.extension.saavn.models

import kotlinx.serialization.Serializable

@Serializable
data class TopArtistResponse(
    val status: String,
    val message: String,
    val data: List<TopArtistItem>
)

@Serializable
data class TopArtistItem(
    val id: String,
    val name: String,
    val image: List<TopArtistImage>,
    val url: String,
    val is_followed: Boolean,
    val follower_count: Long
)

@Serializable
data class TopArtistImage(
    val quality: String,
    val link: String
)