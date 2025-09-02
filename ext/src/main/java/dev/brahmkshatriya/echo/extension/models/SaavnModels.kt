package dev.brahmkshatriya.echo.extension.saavn.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class SaavnResponse<T>(
    val success: Boolean,
    val data: T
)

@Serializable
data class GlobalSearch(
    val albums: SearchResult<Album>,
    val songs: SearchResult<Song>,
    val artists: SearchResult<Artist>,
    val playlists: SearchResult<Playlist>,
    val topQuery: SearchResult<Song>
)

@Serializable
data class SearchResult<T>(
    val results: List<T>,
    val position: Int
)

@Serializable
data class PagedSearch<T>(
    val total: Int,
    val start: Int,
    val results: List<T>
)

@Serializable
data class Song(
    val id: String,
    val name: String,
    val type: String,
    val year: String? = null,
    val releaseDate: String? = null,
    val duration: String? = null,
    val label: String? = null,
    val explicitContent: Boolean,
    val playCount: String? = null,
    val language: String,
    val hasLyrics: Boolean,
    val lyricsId: String? = null,
    val url: String,
    val copyright: String? = null,
    val album: AlbumRef? = null,
    val artists: Artists,
    val image: List<Image>,
    val downloadUrl: List<DownloadUrl>
)

@Serializable
data class Album(
    val id: String,
    val name: String,
    val description: String,
    val year: String? = null,
    val type: String,
    val playCount: String? = null,
    val language: String,
    val explicitContent: Boolean,
    val artists: Artists,
    val songCount: Int? = null,
    val url: String,
    val image: List<Image>,
    val songs: List<Song>? = null
)

@Serializable
data class Artist(
    val id: String,
    val name: String,
    val role: String? = null,
    val type: String,
    val image: List<Image>,
    val url: String,
    val followerCount: String? = null,
    val fanCount: String? = null,
    val isVerified: Boolean? = null,
    val dominantLanguage: String? = null,
    val dominantType: String? = null,
    val bio: List<Bio>? = null,
    val dob: String? = null,
    val fb: String? = null,
    val twitter: String? = null,
    val wiki: String? = null,
    val availableLanguages: List<String>? = null,
    val isRadioPresent: Boolean? = null,
    val topSongs: List<Song>? = null,
    val topAlbums: List<Album>? = null,
    val singles: List<Song>? = null,
    val similarArtists: List<SimilarArtist>? = null
)

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val description: String? = null,
    val year: String? = null,
    val type: String,
    val playCount: String? = null,
    val language: String,
    val explicitContent: Boolean,
    val songCount: Int? = null,
    val url: String,
    val image: List<Image>,
    val songs: List<Song>? = null,
    val artists: List<ArtistRef>? = null
)

@Serializable
data class AlbumRef(
    val id: String? = null,
    val name: String? = null,
    val url: String? = null
)

@Serializable
data class ArtistRef(
    val id: String,
    val name: String,
    val role: String,
    val type: String,
    val image: List<Image>,
    val url: String
)

@Serializable
data class Artists(
    val primary: List<ArtistRef>,
    val featured: List<ArtistRef>,
    val all: List<ArtistRef>
)

@Serializable
data class Image(
    val quality: String,
    val url: String
)

@Serializable
data class DownloadUrl(
    val quality: String,
    val url: String
)

@Serializable
data class Bio(
    val text: String? = null,
    val title: String? = null,
    val sequence: Int? = null
)

@Serializable
data class SimilarArtist(
    val id: String,
    val name: String,
    val url: String,
    val image: List<Image>,
    val languages: JsonObject? = null, 
    val wiki: String? = null, 
    val dob: String? = null, 
    val fb: String? = null, 
    val twitter: String? = null, 
    val isRadioPresent: Boolean? = null,
    val type: String,
    val dominantType: String? = null, 
    val aka: String? = null, 
    val bio: String? = null,
    val similarArtists: List<SimilarArtistRef>? = null
)

@Serializable
data class SimilarArtistRef(
    val id: String,
    val name: String
)

@Serializable
data class ArtistSongs(
    val total: Int,
    val songs: List<Song>
)

@Serializable
data class ArtistAlbums(
    val total: Int,
    val albums: List<Album>
)