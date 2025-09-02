package dev.brahmkshatriya.echo.extension.saavn.models

import kotlinx.serialization.Serializable

@Serializable
data class NewReleaseItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val type: String,
    val url: String,
    val image: List<NewReleaseImage>
)

@Serializable
data class NewReleaseImage(
    val quality: String,
    val link: String
)