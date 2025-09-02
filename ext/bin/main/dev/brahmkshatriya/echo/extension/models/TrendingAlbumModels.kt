package dev.brahmkshatriya.echo.extension.saavn.models

import kotlinx.serialization.Serializable

@Serializable
data class TrendingAlbumItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val type: String,
    val language: String? = null,
    val play_count: Int? = null,
    val duration: Int? = null,
    val explicit: Boolean,
    val year: Int? = null,
    val url: String,
    val header_desc: String? = null,
    val list_count: Int? = null,
    val list_type: String? = null,
    val image: List<TrendingAlbumImage>
)

@Serializable
data class TrendingAlbumImage(
    val quality: String,
    val link: String
)