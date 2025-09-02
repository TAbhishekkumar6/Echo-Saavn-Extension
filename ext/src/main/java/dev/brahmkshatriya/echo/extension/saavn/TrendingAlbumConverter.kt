package dev.brahmkshatriya.echo.extension.saavn

import dev.brahmkshatriya.echo.common.models.*
import dev.brahmkshatriya.echo.common.models.ImageHolder.Companion.toImageHolder
import dev.brahmkshatriya.echo.extension.saavn.models.TrendingAlbumItem

class TrendingAlbumConverter {
    private fun convertImageUrl(originalUrl: String): String {
        return originalUrl.replace(Regex("\\d+x\\d+"), "500x500")
    }
    fun toAlbum(trendingAlbumItem: TrendingAlbumItem): Album {
        val artistName = parseArtistFromSubtitle(trendingAlbumItem.subtitle)
        val artist = Artist(
            id = "", 
            name = artistName,
            cover = trendingAlbumItem.image.firstOrNull()?.link?.let { convertImageUrl(it).toImageHolder() },
            bio = null,
            background = trendingAlbumItem.image.firstOrNull()?.link?.let { convertImageUrl(it).toImageHolder() },
            banners = emptyList(),
            subtitle = null,
            extras = emptyMap()
        )
        
        return Album(
            id = trendingAlbumItem.id,
            title = trendingAlbumItem.name,
            type = null, 
            cover = trendingAlbumItem.image.firstOrNull()?.link?.let { convertImageUrl(it).toImageHolder() },
            artists = listOf(artist),
            trackCount = null, 
            duration = null, 
            releaseDate = null, 
            description = null, 
            background = trendingAlbumItem.image.firstOrNull()?.link?.let { convertImageUrl(it).toImageHolder() },
            label = null,
            isExplicit = trendingAlbumItem.explicit,
            subtitle = trendingAlbumItem.subtitle,
            extras = mapOf(
                "url" to trendingAlbumItem.url,
                "type" to trendingAlbumItem.type,
                "language" to (trendingAlbumItem.language ?: ""),
                "year" to (trendingAlbumItem.year?.toString() ?: ""),
                "originalSubtitle" to trendingAlbumItem.subtitle,
                "isTrendingAlbum" to "true", 
                "play_count" to (trendingAlbumItem.play_count?.toString() ?: "0"),
                "duration" to (trendingAlbumItem.duration?.toString() ?: "0"),
                "header_desc" to (trendingAlbumItem.header_desc ?: ""),
                "list_count" to (trendingAlbumItem.list_count?.toString() ?: "0"),
                "list_type" to (trendingAlbumItem.list_type ?: "")
            )
        )
    }
    private fun parseArtistFromSubtitle(subtitle: String): String {
        return try {
            val parts = subtitle.split(",")
            if (parts.isNotEmpty()) {
                parts[0].trim()
            } else {
                subtitle.trim()
            }
        } catch (e: Exception) {
            subtitle.trim()
        }
    }
}