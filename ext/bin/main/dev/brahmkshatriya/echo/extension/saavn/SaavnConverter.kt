package dev.brahmkshatriya.echo.extension.saavn

import dev.brahmkshatriya.echo.common.models.*
import dev.brahmkshatriya.echo.common.models.ImageHolder.Companion.toImageHolder
import dev.brahmkshatriya.echo.common.settings.Settings
import dev.brahmkshatriya.echo.extension.saavn.models.SaavnResponse
import dev.brahmkshatriya.echo.extension.saavn.models.GlobalSearch
import dev.brahmkshatriya.echo.extension.saavn.models.SearchResult
import dev.brahmkshatriya.echo.extension.saavn.models.PagedSearch
import dev.brahmkshatriya.echo.extension.saavn.models.Song as SaavnSong
import dev.brahmkshatriya.echo.extension.saavn.models.Album as SaavnAlbum
import dev.brahmkshatriya.echo.extension.saavn.models.Artist as SaavnArtist
import dev.brahmkshatriya.echo.extension.saavn.models.Playlist as SaavnPlaylist
import dev.brahmkshatriya.echo.extension.saavn.models.ArtistRef as SaavnArtistRef

class SaavnConverter(private val settings: Settings) {    
    private fun convertImageUrl(originalUrl: String): String {
        return originalUrl.replace(Regex("\\d+x\\d+"), "500x500")
    }
    private fun parseDurationToMillis(durationStr: String?): Long? {
        if (durationStr.isNullOrBlank()) return null
        
        return try {
            val seconds = durationStr.toLongOrNull() ?: return null
            seconds * 1000
        } catch (e: Exception) {
            null
        }
    }    
    fun toTrack(saavnSong: SaavnSong): Track {
        val streamables = mutableListOf<Streamable>()
        if (saavnSong.downloadUrl.isNotEmpty()) {
            val downloadUrlsMap = saavnSong.downloadUrl.associate { it.quality to it.url }
            val coverUrl = saavnSong.image.firstOrNull()?.url?.let { convertImageUrl(it) }
            
            streamables.add(
                Streamable.server(
                    id = "server_${saavnSong.id}",
                    quality = 320, 
                    title = "Audio Stream",
                    extras = mutableMapOf<String, String>().apply {
                        put("downloadUrls", downloadUrlsMap.toString())
                        coverUrl?.let { put("coverUrl", it) }
                        put("trackId", saavnSong.id)
                        put("trackTitle", saavnSong.name)
                    }
                )
            )
        }
        val durationMillis = parseDurationToMillis(saavnSong.duration)
        return Track(
            id = saavnSong.id,
            title = saavnSong.name,
            type = Track.Type.Song,
            cover = saavnSong.image.firstOrNull()?.url?.let { convertImageUrl(it).toImageHolder() },
            artists = saavnSong.artists.primary.map { toArtist(it) },
            album = saavnSong.album?.let { albumRef ->
                Album(
                    id = albumRef.id ?: "",
                    title = albumRef.name ?: "",
                    type = null,
                    cover = saavnSong.image.firstOrNull()?.url?.let { convertImageUrl(it).toImageHolder() },
                    artists = saavnSong.artists.primary.map { toArtist(it) },
                    trackCount = null,
                    duration = durationMillis, 
                    releaseDate = null,
                    description = null,
                    background = saavnSong.image.firstOrNull()?.url?.let { convertImageUrl(it).toImageHolder() },
                    label = null,
                    isExplicit = saavnSong.explicitContent,
                    subtitle = null,
                    extras = mapOf(
                        "url" to saavnSong.url,
                        "language" to saavnSong.language,
                        "hasLyrics" to saavnSong.hasLyrics.toString()
                    )
                )
            },
            duration = durationMillis, 
            playedDuration = null,
            plays = null,
            releaseDate = null,
            description = null,
            background = saavnSong.image.firstOrNull()?.url?.let { convertImageUrl(it).toImageHolder() },
            genres = emptyList(),
            isrc = null,
            albumOrderNumber = null,
            albumDiscNumber = null,
            playlistAddedDate = null,
            isExplicit = saavnSong.explicitContent,
            subtitle = null,
            extras = mapOf(
                "url" to saavnSong.url,
                "language" to saavnSong.language,
                "hasLyrics" to saavnSong.hasLyrics.toString(),
                "durationSeconds" to (saavnSong.duration ?: "0") 
            ),
            isPlayable = Track.Playable.Yes,
            streamables = streamables
        )
    }    
    fun toAlbum(saavnAlbum: SaavnAlbum): Album {
        return Album(
            id = saavnAlbum.id,
            title = saavnAlbum.name,
            type = null,
            cover = saavnAlbum.image.firstOrNull()?.url?.let { convertImageUrl(it).toImageHolder() },
            artists = saavnAlbum.artists.primary.map { toArtist(it) },
            trackCount = saavnAlbum.songCount?.toLong(),
            duration = null, 
            releaseDate = null,
            description = saavnAlbum.description,
            background = saavnAlbum.image.firstOrNull()?.url?.let { convertImageUrl(it).toImageHolder() },
            label = null,
            isExplicit = saavnAlbum.explicitContent,
            subtitle = null,
            extras = mapOf(
                "url" to saavnAlbum.url,
                "language" to saavnAlbum.language,
                "type" to saavnAlbum.type
            )
        )
    }  
    fun toArtist(saavnArtist: SaavnArtist): Artist {
        return Artist(
            id = saavnArtist.id,
            name = saavnArtist.name,
            cover = saavnArtist.image.firstOrNull()?.url?.let { convertImageUrl(it).toImageHolder() },
            bio = null,
            background = saavnArtist.image.firstOrNull()?.url?.let { convertImageUrl(it).toImageHolder() },
            banners = emptyList(),
            subtitle = saavnArtist.role,
            extras = mapOf(
                "url" to saavnArtist.url,
                "role" to (saavnArtist.role ?: ""),
                "type" to saavnArtist.type,
                "followerCount" to (saavnArtist.followerCount ?: ""),
                "isVerified" to (saavnArtist.isVerified ?: false).toString(),
                "dominantLanguage" to (saavnArtist.dominantLanguage ?: "")
            )
        )
    }    
    fun toPlaylist(saavnPlaylist: SaavnPlaylist): Playlist {
        return Playlist(
            id = saavnPlaylist.id,
            title = saavnPlaylist.name,
            isEditable = false,
            isPrivate = false,
            cover = saavnPlaylist.image.firstOrNull()?.url?.let { convertImageUrl(it).toImageHolder() },
            authors = saavnPlaylist.artists?.map { toArtist(it) } ?: emptyList(),
            trackCount = saavnPlaylist.songCount?.toLong(),
            duration = null, 
            creationDate = null,
            description = saavnPlaylist.description,
            background = saavnPlaylist.image.firstOrNull()?.url?.let { convertImageUrl(it).toImageHolder() },
            subtitle = null,
            extras = mapOf(
                "url" to saavnPlaylist.url,
                "language" to saavnPlaylist.language,
                "type" to saavnPlaylist.type
            )
        )
    }    
    fun toArtist(artistRef: SaavnArtistRef): Artist {
        return Artist(
            id = artistRef.id,
            name = artistRef.name,
            cover = artistRef.image.firstOrNull()?.url?.let { convertImageUrl(it).toImageHolder() },
            bio = null,
            background = artistRef.image.firstOrNull()?.url?.let { convertImageUrl(it).toImageHolder() },
            banners = emptyList(),
            subtitle = artistRef.role,
            extras = mapOf(
                "url" to artistRef.url,
                "role" to artistRef.role,
                "type" to artistRef.type
            )
        )
    }
}