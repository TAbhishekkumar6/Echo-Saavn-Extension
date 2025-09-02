package dev.brahmkshatriya.echo.extension

import dev.brahmkshatriya.echo.common.clients.*
import dev.brahmkshatriya.echo.common.helpers.Page
import dev.brahmkshatriya.echo.common.helpers.PagedData
import dev.brahmkshatriya.echo.common.models.*
import dev.brahmkshatriya.echo.common.models.Feed.Companion.toFeed
import dev.brahmkshatriya.echo.common.models.Feed.Companion.toFeedData
import dev.brahmkshatriya.echo.common.models.ImageHolder.Companion.toImageHolder
import dev.brahmkshatriya.echo.common.models.NetworkRequest.Companion.toGetRequest
import dev.brahmkshatriya.echo.common.settings.Setting
import dev.brahmkshatriya.echo.common.settings.SettingSwitch
import dev.brahmkshatriya.echo.common.settings.Settings
import dev.brahmkshatriya.echo.extension.saavn.SaavnApi
import dev.brahmkshatriya.echo.extension.saavn.SaavnConverter
import dev.brahmkshatriya.echo.extension.saavn.SaavnQueries
import dev.brahmkshatriya.echo.extension.saavn.TrendingApi
import dev.brahmkshatriya.echo.extension.saavn.TrendingConverter
import dev.brahmkshatriya.echo.extension.saavn.NewReleaseApi
import dev.brahmkshatriya.echo.extension.saavn.NewReleaseConverter
import dev.brahmkshatriya.echo.extension.saavn.TrendingAlbumApi
import dev.brahmkshatriya.echo.extension.saavn.TrendingAlbumConverter
import dev.brahmkshatriya.echo.extension.saavn.models.SaavnResponse
import dev.brahmkshatriya.echo.extension.saavn.models.GlobalSearch
import dev.brahmkshatriya.echo.extension.saavn.models.PagedSearch
import dev.brahmkshatriya.echo.extension.saavn.models.Song as SaavnSong
import dev.brahmkshatriya.echo.extension.saavn.models.Album as SaavnAlbum
import dev.brahmkshatriya.echo.extension.saavn.models.Artist as SaavnArtist
import dev.brahmkshatriya.echo.extension.saavn.models.Playlist as SaavnPlaylist
import dev.brahmkshatriya.echo.extension.saavn.models.ArtistSongs
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class SaavnExtension : ExtensionClient, 
    QuickSearchClient, HomeFeedClient, LibraryFeedClient, SearchFeedClient,
    TrackClient, AlbumClient, ArtistClient, PlaylistClient {

    override suspend fun getSettingItems(): List<Setting> {
        return listOf(
            SettingSwitch(
                "show_cover_art_background",
                "Show Cover Art Background",
                "Enable cover art as background by default during playback",
                true
            )
        )
    }
    private lateinit var setting: Settings
    override fun setSettings(settings: Settings) {
        setting = settings
    }
    private val api by lazy { SaavnApi() }
    private val queries by lazy { SaavnQueries(api) }
    private val converter by lazy { SaavnConverter(setting) }
    private val trendingApi by lazy { TrendingApi() }
    private val trendingConverter by lazy { TrendingConverter() }
    private val newReleaseApi by lazy { NewReleaseApi() }
    private val newReleaseConverter by lazy { NewReleaseConverter() }
    private val trendingAlbumApi by lazy { TrendingAlbumApi() }
    private val trendingAlbumConverter by lazy { TrendingAlbumConverter() }

    override suspend fun quickSearch(query: String): List<QuickSearchItem> {
        if (query.isBlank()) return emptyList()   
        return try {
            val response = queries.globalSearch(query)
            if (!response.json.success) return emptyList()
            
            val searchResults = response.json.data as GlobalSearch
            val items = mutableListOf<QuickSearchItem>() 
            items.addAll(searchResults.songs.results.map { 
                QuickSearchItem.Media(converter.toTrack(it), false) 
            })
            items.addAll(searchResults.albums.results.map { 
                QuickSearchItem.Media(converter.toAlbum(it), false) 
            }) 
            items.addAll(searchResults.artists.results.map { 
                QuickSearchItem.Media(converter.toArtist(it), false) 
            })
            items.addAll(searchResults.playlists.results.map { 
                QuickSearchItem.Media(converter.toPlaylist(it), false) 
            })
            items.take(10)
        } catch (e: Exception) {
            println("DEBUG: Quick search failed: ${e.message}")
            emptyList()
        }
    }
    override suspend fun deleteQuickSearch(item: QuickSearchItem) {
    }
        override suspend fun loadSearchFeed(query: String): Feed<Shelf> {
        if (query.isBlank()) {
            return emptyList<Shelf>().toFeed()
        }
        return coroutineScope {
            val songsDeferred = async { 
                try { queries.searchSongs(query, 0, 10) } 
                catch (e: Exception) { 
                    println("DEBUG: Songs search failed: ${e.message}")
                    null 
                }
            }
            val albumsDeferred = async { 
                try { queries.searchAlbums(query, 0, 10) } 
                catch (e: Exception) { 
                    println("DEBUG: Albums search failed: ${e.message}")
                    null 
                }
            }
            val artistsDeferred = async { 
                try { queries.searchArtists(query, 0, 10) } 
                catch (e: Exception) { 
                    println("DEBUG: Artists search failed: ${e.message}")
                    null 
                }
            }
            val playlistsDeferred = async { 
                try { queries.searchPlaylists(query, 0, 10) } 
                catch (e: Exception) { 
                    println("DEBUG: Playlists search failed: ${e.message}")
                    null 
                }
            }

            val (songsResponse, albumsResponse, artistsResponse, playlistsResponse) = awaitAll(
                songsDeferred, albumsDeferred, artistsDeferred, playlistsDeferred
            )
            val shelves = mutableListOf<Shelf>()
            songsResponse?.json?.data?.let { songsData ->
                val pagedSearch = songsData as? PagedSearch<SaavnSong>
                pagedSearch?.let {
                    shelves.add(Shelf.Lists.Tracks(
                        id = "search_songs",
                        title = "Songs",
                        list = it.results.map { song -> converter.toTrack(song) }
                    ))
                }
            }
            albumsResponse?.json?.data?.let { albumsData ->
                val pagedSearch = albumsData as? PagedSearch<SaavnAlbum>
                pagedSearch?.let {
                    shelves.add(Shelf.Lists.Items(
                        id = "search_albums",
                        title = "Albums",
                        list = it.results.map { album -> converter.toAlbum(album) }
                    ))
                }
            }
            artistsResponse?.json?.data?.let { artistsData ->
                val pagedSearch = artistsData as? PagedSearch<SaavnArtist>
                pagedSearch?.let {
                    shelves.add(Shelf.Lists.Items(
                        id = "search_artists",
                        title = "Artists",
                        list = it.results.map { artist -> converter.toArtist(artist) }
                    ))
                }
            }
            playlistsResponse?.json?.data?.let { playlistsData ->
                val pagedSearch = playlistsData as? PagedSearch<SaavnPlaylist>
                pagedSearch?.let {
                    shelves.add(Shelf.Lists.Items(
                        id = "search_playlists",
                        title = "Playlists",
                        list = it.results.map { playlist -> converter.toPlaylist(playlist) }
                    ))
                }
            }
            val tabs = listOf(
                Tab("all", "All"),
                Tab("songs", "Songs"),
                Tab("albums", "Albums"),
                Tab("artists", "Artists"),
                Tab("playlists", "Playlists")
            )
            Feed(tabs) { tab ->
                when (tab?.id) {
                    "songs" -> PagedData.Continuous { continuation ->
                        val page = (continuation as? String)?.toIntOrNull() ?: 0
                        try {
                            val response = queries.searchSongs(query, page, 20)
                            val data = response.json.data as? PagedSearch<SaavnSong> 
                                ?: return@Continuous Page(emptyList<Shelf>(), null)
                            Page(
                                listOf(Shelf.Lists.Tracks(
                                    id = "search_songs_tab",
                                    title = "Songs",
                                    list = data.results.map { converter.toTrack(it) }
                                )),
                                if (data.total > data.start + data.results.size) (page + 1).toString() else null
                            )
                        } catch (e: Exception) {
                            println("DEBUG: Songs tab pagination failed: ${e.message}")
                            Page(emptyList<Shelf>(), null)
                        }
                    }.toFeedData() 
                    "albums" -> PagedData.Continuous { continuation ->
                        val page = (continuation as? String)?.toIntOrNull() ?: 0
                        try {
                            val response = queries.searchAlbums(query, page, 20)
                            val data = response.json.data as? PagedSearch<SaavnAlbum> 
                                ?: return@Continuous Page(emptyList<Shelf>(), null)
                            Page(
                                listOf(Shelf.Lists.Items(
                                    id = "search_albums_tab",
                                    title = "Albums",
                                    list = data.results.map { converter.toAlbum(it) }
                                )),
                                if (data.total > data.start + data.results.size) (page + 1).toString() else null
                            )
                        } catch (e: Exception) {
                            println("DEBUG: Albums tab pagination failed: ${e.message}")
                            Page(emptyList<Shelf>(), null)
                        }
                    }.toFeedData()            
                    "artists" -> PagedData.Continuous { continuation ->
                        val page = (continuation as? String)?.toIntOrNull() ?: 0
                        try {
                            val response = queries.searchArtists(query, page, 20)
                            val data = response.json.data as? PagedSearch<SaavnArtist> 
                                ?: return@Continuous Page(emptyList<Shelf>(), null)
                            Page(
                                listOf(Shelf.Lists.Items(
                                    id = "search_artists_tab",
                                    title = "Artists",
                                    list = data.results.map { converter.toArtist(it) }
                                )),
                                if (data.total > data.start + data.results.size) (page + 1).toString() else null
                            )
                        } catch (e: Exception) {
                            println("DEBUG: Artists tab pagination failed: ${e.message}")
                            Page(emptyList<Shelf>(), null)
                        }
                    }.toFeedData() 
                    "playlists" -> PagedData.Continuous { continuation ->
                        val page = (continuation as? String)?.toIntOrNull() ?: 0
                        try {
                            val response = queries.searchPlaylists(query, page, 20)
                            val data = response.json.data as? PagedSearch<SaavnPlaylist> 
                                ?: return@Continuous Page(emptyList<Shelf>(), null)
                            Page(
                                listOf(Shelf.Lists.Items(
                                    id = "search_playlists_tab",
                                    title = "Playlists",
                                    list = data.results.map { playlist -> converter.toPlaylist(playlist) }
                                )),
                                if (data.total > data.start + data.results.size) (page + 1).toString() else null
                            )
                        } catch (e: Exception) {
                            println("DEBUG: Playlists tab pagination failed: ${e.message}")
                            Page(emptyList<Shelf>(), null)
                        }
                    }.toFeedData()  
                    else -> PagedData.Single { shelves }.toFeedData()
                }
            }
        }
    }
    override suspend fun loadHomeFeed(): Feed<Shelf> {
        println("DEBUG: loadHomeFeed() called - Loading new releases, top artists, and trending albums")
        
        return try {
            val shelves = mutableListOf<Shelf>()
            try {
                val newReleases = newReleaseApi.getNewReleases()
                println("DEBUG: Received ${newReleases.size} new releases")
                
                if (newReleases.isNotEmpty()) {
                    val releaseList = newReleases.take(15).mapNotNull { release ->
                        try {
                            println("DEBUG: Converting release: ${release.name}")
                            newReleaseConverter.toTrack(release)
                        } catch (e: Exception) {
                            println("DEBUG: Failed to convert release ${release.name}: ${e.message}")
                            null
                        }
                    }             
                    if (releaseList.isNotEmpty()) {
                        shelves.add(Shelf.Lists.Tracks(
                            id = "trending_releases",
                            title = "Trending Release",
                            list = releaseList
                        ))
                        println("DEBUG: Added trending releases shelf with ${releaseList.size} tracks")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Error loading new releases: ${e.message}")
            }
            try {
                val topArtistsResponse = trendingApi.getTopArtists()
                
                if (topArtistsResponse.status != "Success") {
                    println("DEBUG: Top Artists API error: ${topArtistsResponse.message}")
                } else {
                    val topArtists = topArtistsResponse.data
                    println("DEBUG: Received ${topArtists.size} top artists")
                    
                    if (topArtists.isNotEmpty()) {
                        val artistList = topArtists.take(20).mapNotNull { artist ->
                            try {
                                println("DEBUG: Converting artist: ${artist.name}")
                                trendingConverter.toArtist(artist)
                            } catch (e: Exception) {
                                println("DEBUG: Failed to convert artist ${artist.name}: ${e.message}")
                                null
                            }
                        }          
                        if (artistList.isNotEmpty()) {
                            shelves.add(Shelf.Lists.Items(
                                id = "top_artists",
                                title = "Top Artists",
                                list = artistList
                            ))
                            println("DEBUG: Added top artists shelf with ${artistList.size} artists")
                        }
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Error loading top artists: ${e.message}")
            }
            try {
                val trendingAlbums = trendingAlbumApi.getTrendingAlbums()
                println("DEBUG: Received ${trendingAlbums.size} trending albums")
                
                if (trendingAlbums.isNotEmpty()) {
                    val albumList = trendingAlbums.take(20).mapNotNull { album ->
                        try {
                            println("DEBUG: Converting album: ${album.name}")
                            trendingAlbumConverter.toAlbum(album)
                        } catch (e: Exception) {
                            println("DEBUG: Failed to convert album ${album.name}: ${e.message}")
                            null
                        }
                    }                   
                    if (albumList.isNotEmpty()) {
                        shelves.add(Shelf.Lists.Items(
                            id = "trending_albums",
                            title = "Trending Albums",
                            list = albumList
                        ))
                        println("DEBUG: Added trending albums shelf with ${albumList.size} albums")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Error loading trending albums: ${e.message}")
            }
            try {
                val topArtistsResponse = trendingApi.getTopArtists()  
                if (topArtistsResponse.status == "Success") {
                    val topArtists = topArtistsResponse.data     
                    val mostFollowed = topArtists
                        .sortedByDescending { it.follower_count }
                        .take(10)
                        .mapNotNull { artist ->
                            try {
                                trendingConverter.toArtist(artist)
                            } catch (e: Exception) {
                                println("DEBUG: Failed to convert most followed artist ${artist.name}: ${e.message}")
                                null
                            }
                        }      
                    if (mostFollowed.isNotEmpty()) {
                        shelves.add(Shelf.Lists.Items(
                            id = "most_followed_artists",
                            title = "Most Followed Artists",
                            list = mostFollowed
                        ))
                        println("DEBUG: Added most followed artists shelf with ${mostFollowed.size} artists")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Error loading most followed artists: ${e.message}")
            }

            println("DEBUG: Created ${shelves.size} shelves total")
            
            if (shelves.isNotEmpty()) {
                println("DEBUG: Returning feed with ${shelves.size} shelves")
                shelves.toFeed()
            } else {
                println("DEBUG: No shelves created, returning empty shelf")
                val emptyShelf = Shelf.Lists.Items(
                    id = "no_content",
                    title = "No Content Available",
                    list = emptyList<EchoMediaItem>()
                )
                listOf(emptyShelf).toFeed()
            }   
        } catch (e: Exception) {
            println("DEBUG: Unexpected error in loadHomeFeed: ${e.message}")
            e.printStackTrace()
            val debugShelf = Shelf.Lists.Items(
                id = "debug_error",
                title = "Error: ${e.javaClass.simpleName}",
                list = emptyList<EchoMediaItem>()
            )
            listOf(debugShelf).toFeed()
        }
    }
    override suspend fun loadLibraryFeed(): Feed<Shelf> {
        println("DEBUG: loadLibraryFeed() called - Creating library feed")
        
        return try {
            val shelves = mutableListOf<Shelf>()  
            val favoritesCategory = Shelf.Category(
                id = "favorites",
                title = "Favorites",
                subtitle = "Your favorite music",
                feed = null,
                extras = mapOf("type" to "favorites")
            )
            shelves.add(favoritesCategory)
            val recentlyPlayedCategory = Shelf.Category(
                id = "recently_played",
                title = "Recently Played",
                subtitle = "Your recently played tracks",
                feed = null,
                extras = mapOf("type" to "recently_played")
            )
            shelves.add(recentlyPlayedCategory)
            val playlistsCategory = Shelf.Category(
                id = "playlists",
                title = "My Playlists",
                subtitle = "Your created playlists",
                feed = null,
                extras = mapOf("type" to "playlists")
            )
            shelves.add(playlistsCategory) 
            try {
                val newReleases = newReleaseApi.getNewReleases()
                if (newReleases.isNotEmpty()) {
                    val recommendedTracks = newReleases.take(10).mapNotNull { release ->
                        try {
                            newReleaseConverter.toTrack(release)
                        } catch (e: Exception) {
                            println("DEBUG: Failed to convert recommended track ${release.name}: ${e.message}")
                            null
                        }
                    }    
                    if (recommendedTracks.isNotEmpty()) {
                        shelves.add(Shelf.Lists.Tracks(
                            id = "library_recommended",
                            title = "Recommended for You",
                            subtitle = "Based on trending music",
                            list = recommendedTracks
                        ))
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Error loading recommended tracks for library: ${e.message}")
            }  
            println("DEBUG: Created library feed with ${shelves.size} shelves")
            if (shelves.isNotEmpty()) {
                shelves.toFeed()
            } else {
                val emptyShelf = Shelf.Category(
                    id = "empty_library",
                    title = "Your Library",
                    subtitle = "No items in your library yet",
                    feed = null
                )
                listOf(emptyShelf).toFeed()
            }
        } catch (e: Exception) {
            println("DEBUG: Error in loadLibraryFeed: ${e.message}")
            e.printStackTrace()
            
            val errorShelf = Shelf.Category(
                id = "library_error",
                title = "Library",
                subtitle = "Unable to load library content",
                feed = null
            )
            listOf(errorShelf).toFeed()
        }
    }
    override suspend fun loadTrack(track: Track, isDownload: Boolean): Track {
        return try {
            println("DEBUG: Loading track with ID: ${track.id}")
            val response = queries.getSong(track.id)
            val song = response.json.data.firstOrNull() as? SaavnSong
                ?: throw Exception("Track not found")
            
            val fullTrack = converter.toTrack(song)
            
            if (track.extras["isNewRelease"] == "true") {
                fullTrack.copy(
                    title = track.title,
                    cover = track.cover ?: fullTrack.cover,
                    background = track.background ?: fullTrack.background,
                    isExplicit = track.isExplicit || fullTrack.isExplicit,
                    extras = track.extras + fullTrack.extras
                )
            } else {
                fullTrack.copy(
                    isExplicit = track.isExplicit,
                    extras = track.extras + fullTrack.extras
                )
            }
        } catch (e: Exception) {
            println("DEBUG: Failed to load track ${track.id}: ${e.message}")
            throw Exception("Failed to load track: ${e.message}")
        }
    }
    override suspend fun loadStreamableMedia(
        streamable: Streamable, 
        isDownload: Boolean
    ): Streamable.Media {
        return when (streamable.type) {
            Streamable.MediaType.Server -> {
                println("DEBUG: Loading streamable media with integrated cover art")
                
                val downloadUrls = extractDownloadUrls(streamable.extras)
                    ?: throw Exception("No download URLs found in streamable extras")
                val qualityUrl = downloadUrls["320kbps"] 
                    ?: downloadUrls["160kbps"] 
                    ?: downloadUrls["96kbps"]
                    ?: downloadUrls["48kbps"]
                    ?: downloadUrls["12kbps"]
                    ?: throw Exception("No suitable stream quality found")
                if (qualityUrl.isBlank()) {
                    throw Exception("Selected URL is blank")
                }  
                val quality = when {
                    qualityUrl.contains("320") -> 320
                    qualityUrl.contains("160") -> 160
                    qualityUrl.contains("96") -> 96
                    qualityUrl.contains("48") -> 48
                    qualityUrl.contains("12") -> 12
                    else -> 128
                } 
                val httpSource = Streamable.Source.Http(
                    request = qualityUrl.toGetRequest(),
                    type = Streamable.SourceType.Progressive,
                    quality = quality,
                    title = "${quality}kbps"
                )  
                Streamable.Media.Server(listOf(httpSource), false)
            }
            Streamable.MediaType.Background -> {
                throw Exception("Background streamables not supported for audio content. Cover art is handled by server streamable.")
            }
            Streamable.MediaType.Subtitle -> {
                throw Exception("Subtitles not supported")
            }
        }
    }
    private fun extractDownloadUrls(extras: Map<String, String>): Map<String, String>? {
        val downloadUrlsValue = extras["downloadUrls"] ?: return null
        val downloadUrlsString = downloadUrlsValue.toString().trim()
        
        if (!downloadUrlsString.startsWith("{") || !downloadUrlsString.endsWith("}")) {
            return null
        }     
        val content = downloadUrlsString.substring(1, downloadUrlsString.length - 1).trim()
        if (content.isEmpty()) return null   
        val result = mutableMapOf<String, String>()
        val pairs = content.split(",")
        for (pair in pairs) {
            val trimmedPair = pair.trim()
            val equalIndex = trimmedPair.indexOf('=')
            if (equalIndex != -1) {
                val key = trimmedPair.substring(0, equalIndex).trim()
                val value = trimmedPair.substring(equalIndex + 1).trim()
                if (key.isNotBlank() && value.isNotBlank()) {
                    result[key] = value
                }
            }
        }   
        return if (result.isNotEmpty()) result else null
    }
    override suspend fun loadFeed(track: Track): Feed<Shelf> {
        return emptyList<Shelf>().toFeed()
    }
    override suspend fun loadAlbum(album: Album): Album {
        return try {
            println("DEBUG: Loading album with ID: ${album.id}")
            val response = queries.getAlbum(album.id)
            val saavnAlbum = response.json.data as SaavnAlbum
            val fullAlbum = converter.toAlbum(saavnAlbum)   
            if (album.extras["isTrendingAlbum"] == "true") {
                fullAlbum.copy(
                    title = album.title,
                    cover = album.cover ?: fullAlbum.cover,
                    background = album.background ?: fullAlbum.background,
                    isExplicit = album.isExplicit || fullAlbum.isExplicit,
                    subtitle = album.subtitle ?: fullAlbum.subtitle,
                    extras = album.extras + fullAlbum.extras
                )
            } else {
                fullAlbum
            }
        } catch (e: Exception) {
            println("DEBUG: Failed to load album ${album.id}: ${e.message}")
            throw Exception("Failed to load album: ${e.message}")
        }
    }
    override suspend fun loadTracks(album: Album): Feed<Track>? {
        return try {
            println("DEBUG: Loading tracks for album: ${album.id}")
            val response = queries.getAlbum(album.id)
            val saavnAlbum = response.json.data as SaavnAlbum
            val tracks = saavnAlbum.songs?.map { converter.toTrack(it as SaavnSong) } ?: emptyList()
            
            println("DEBUG: Loaded ${tracks.size} tracks for album ${album.id}")
            tracks.toFeed() as Feed<Track>
        } catch (e: Exception) {
            println("DEBUG: Failed to load tracks for album ${album.id}: ${e.message}")
            null
        }
    }
    override suspend fun loadFeed(album: Album): Feed<Shelf>? {
        return null
    }
    override suspend fun loadArtist(artist: Artist): Artist {
        return try {
            println("DEBUG: Loading artist with ID: ${artist.id}")
            val response = queries.getArtist(artist.id)
            val saavnArtist = response.json.data as SaavnArtist
            converter.toArtist(saavnArtist)
        } catch (e: Exception) {
            println("DEBUG: Failed to load artist ${artist.id}: ${e.message}")
            throw Exception("Failed to load artist: ${e.message}")
        }
    }
    override suspend fun loadFeed(artist: Artist): Feed<Shelf> {
        return try {
            val artistResponse = queries.getArtist(artist.id)
            val saavnArtist = artistResponse.json.data as SaavnArtist
            
            val shelves = mutableListOf<Shelf>()
            
            saavnArtist.topSongs?.let { topSongs ->
                if (topSongs.isNotEmpty()) {
                    shelves.add(Shelf.Lists.Tracks(
                        id = "artist_top_songs",
                        title = "Popular Songs",
                        list = topSongs.map { converter.toTrack(it) }
                    ))
                }
            }            
            saavnArtist.topAlbums?.let { topAlbums ->
                if (topAlbums.isNotEmpty()) {
                    shelves.add(Shelf.Lists.Items(
                        id = "artist_albums",
                        title = "Albums",
                        list = topAlbums.map { converter.toAlbum(it) }
                    ))
                }
            }
            saavnArtist.singles?.let { singles ->
                if (singles.isNotEmpty()) {
                    shelves.add(Shelf.Lists.Tracks(
                        id = "artist_singles",
                        title = "Singles",
                        list = singles.map { converter.toTrack(it) }
                    ))
                }
            }
            if (shelves.isEmpty()) {
                try {
                    val songsResponse = queries.getArtistSongs(artist.id, 0, "popularity", "desc")
                    val artistSongs = songsResponse.json.data as ArtistSongs
                    if (artistSongs.songs.isNotEmpty()) {
                        shelves.add(Shelf.Lists.Tracks(
                            id = "artist_all_songs",
                            title = "Songs",
                            list = artistSongs.songs.take(20).map { converter.toTrack(it) }
                        ))
                    }
                } catch (e: Exception) {
                    println("DEBUG: Failed to load artist songs: ${e.message}")
                }
            }    
            shelves.toFeed()
        } catch (e: Exception) {
            println("DEBUG: Failed to load artist feed: ${e.message}")
            emptyList<Shelf>().toFeed()
        }
    }
    override suspend fun loadPlaylist(playlist: Playlist): Playlist {
        return try {
            println("DEBUG: Loading playlist with ID: ${playlist.id}")
            val response = queries.getPlaylist(playlist.id)
            val saavnPlaylist = response.json.data as SaavnPlaylist
            converter.toPlaylist(saavnPlaylist)
        } catch (e: Exception) {
            println("DEBUG: Failed to load playlist ${playlist.id}: ${e.message}")
            throw Exception("Failed to load playlist: ${e.message}")
        }
    }
    override suspend fun loadTracks(playlist: Playlist): Feed<Track> {
        return PagedData.Continuous { continuation ->
            val page = (continuation as? String)?.toIntOrNull() ?: 0
            try {
                val response = queries.getPlaylistWithSongs(playlist.id, page, 50)
                val data = response.json.data as SaavnPlaylist
                val tracks = data.songs?.map { converter.toTrack(it as SaavnSong) } ?: emptyList()
                
                val hasMore = tracks.size >= 50
                Page(tracks, if (hasMore) (page + 1).toString() else null)
            } catch (e: Exception) {
                println("DEBUG: Failed to load playlist tracks: ${e.message}")
                Page(emptyList<Track>(), null)
            }
        }.toFeed() as Feed<Track>
    }
    override suspend fun loadFeed(playlist: Playlist): Feed<Shelf>? {
        return try {
            val response = queries.getPlaylistWithSongs(playlist.id, 0, 20)
            val saavnPlaylist = response.json.data as SaavnPlaylist
            
            val shelves = mutableListOf<Shelf>()
            
            saavnPlaylist.songs?.let { songs ->
                if (songs.isNotEmpty()) {
                    shelves.add(Shelf.Lists.Tracks(
                        id = "playlist_songs",
                        title = "Songs",
                        list = songs.map { converter.toTrack(it as SaavnSong) }
                    ))
                }
            }    
            if (shelves.isNotEmpty()) {
                shelves.toFeed()
            } else {
                null
            }
        } catch (e: Exception) {
            println("DEBUG: Failed to load playlist feed: ${e.message}")
            null
        }
    }
}