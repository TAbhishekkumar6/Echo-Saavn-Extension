package dev.brahmkshatriya.echo.extension.saavn

import dev.brahmkshatriya.echo.common.models.*
import dev.brahmkshatriya.echo.common.models.ImageHolder.Companion.toImageHolder
import dev.brahmkshatriya.echo.extension.saavn.models.TopArtistItem

class TrendingConverter {
    private fun convertImageUrl(originalUrl: String): String {
        return originalUrl.replace(Regex("\\d+x\\d+"), "500x500")
    }
    fun toArtist(topArtistItem: TopArtistItem): Artist {
        return Artist(
            id = topArtistItem.id,
            name = topArtistItem.name,
            cover = topArtistItem.image.firstOrNull()?.link?.let { convertImageUrl(it).toImageHolder() },
            bio = null,
            background = topArtistItem.image.firstOrNull()?.link?.let { convertImageUrl(it).toImageHolder() },
            banners = emptyList(),
            subtitle = "${formatFollowerCount(topArtistItem.follower_count)} followers",
            extras = mapOf(
                "url" to topArtistItem.url,
                "follower_count" to topArtistItem.follower_count.toString(),
                "is_followed" to topArtistItem.is_followed.toString()
            )
        )
    }
    private fun formatFollowerCount(count: Long): String {
        return when {
            count >= 1_000_000 -> "${count / 1_000_000}M"
            count >= 1_000 -> "${count / 1_000}K"
            else -> count.toString()
        }
    }
}