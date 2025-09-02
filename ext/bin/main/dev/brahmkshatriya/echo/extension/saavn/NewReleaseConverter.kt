package dev.brahmkshatriya.echo.extension.saavn

import dev.brahmkshatriya.echo.common.models.*
import dev.brahmkshatriya.echo.common.models.ImageHolder.Companion.toImageHolder
import dev.brahmkshatriya.echo.extension.saavn.models.NewReleaseItem

class NewReleaseConverter {
    private fun convertImageUrl(originalUrl: String): String {
        return originalUrl.replace(Regex("\\d+x\\d+"), "500x500")
    }
    fun toTrack(newReleaseItem: NewReleaseItem): Track {
        val artistName = parseArtistFromSubtitle(newReleaseItem.subtitle)
        val artist = Artist(
            id = "", 
            name = artistName,
            cover = newReleaseItem.image.firstOrNull()?.link?.let { convertImageUrl(it).toImageHolder() },
            bio = null,
            background = newReleaseItem.image.firstOrNull()?.link?.let { convertImageUrl(it).toImageHolder() },
            banners = emptyList(),
            subtitle = null,
            extras = emptyMap()
        )    
        return Track(
            id = newReleaseItem.id,
            title = newReleaseItem.name,
            type = Track.Type.Song,
            cover = newReleaseItem.image.firstOrNull()?.link?.let { convertImageUrl(it).toImageHolder() },
            artists = listOf(artist),
            album = null, 
            duration = null, 
            playedDuration = null,
            plays = null,
            releaseDate = null,
            description = null,
            background = newReleaseItem.image.firstOrNull()?.link?.let { convertImageUrl(it).toImageHolder() },
            genres = emptyList(),
            isrc = null,
            albumOrderNumber = null,
            albumDiscNumber = null,
            playlistAddedDate = null,
            isExplicit = false, 
            subtitle = newReleaseItem.subtitle,
            extras = mapOf(
                "url" to newReleaseItem.url,
                "type" to newReleaseItem.type,
                "originalSubtitle" to newReleaseItem.subtitle,
                "isNewRelease" to "true" 
            ),
            isPlayable = Track.Playable.Yes,
            streamables = emptyList() 
        )
    }
    private fun parseArtistFromSubtitle(subtitle: String): String {
        return try {
            val parts = subtitle.split(" - ")
            if (parts.size >= 2) {
                parts[0].trim()
            } else {
                subtitle.trim()
            }
        } catch (e: Exception) {
            subtitle.trim()
        }
    }
}