package dev.brahmkshatriya.echo.extension.saavn

import dev.brahmkshatriya.echo.extension.saavn.models.*

class SaavnQueries(
    private val api: SaavnApi
) {
    suspend fun globalSearch(query: String) = api.get<SaavnResponse<GlobalSearch>>(
        "/search",
        mapOf("query" to query)
    )  
    suspend fun searchSongs(query: String, page: Int = 0, limit: Int = 10) = api.get<SaavnResponse<PagedSearch<Song>>>(
        "/search/songs",
        mapOf("query" to query, "page" to page.toString(), "limit" to limit.toString())
    )    
    suspend fun searchAlbums(query: String, page: Int = 0, limit: Int = 10) = api.get<SaavnResponse<PagedSearch<Album>>>(
        "/search/albums",
        mapOf("query" to query, "page" to page.toString(), "limit" to limit.toString())
    )    
    suspend fun searchArtists(query: String, page: Int = 0, limit: Int = 10) = api.get<SaavnResponse<PagedSearch<Artist>>>(
        "/search/artists",
        mapOf("query" to query, "page" to page.toString(), "limit" to limit.toString())
    )    
    suspend fun searchPlaylists(query: String, page: Int = 0, limit: Int = 10) = api.get<SaavnResponse<PagedSearch<Playlist>>>(
        "/search/playlists",
        mapOf("query" to query, "page" to page.toString(), "limit" to limit.toString())
    )
    suspend fun getSong(id: String) = api.get<SaavnResponse<List<Song>>>(
        "/songs/$id"
    )  
    suspend fun getSongs(ids: List<String>) = api.get<SaavnResponse<List<Song>>>(
        "/songs",
        mapOf("ids" to ids.joinToString(","))
    )    
    suspend fun getSongSuggestions(id: String, limit: Int = 10) = api.get<SaavnResponse<List<Song>>>(
        "/songs/$id/suggestions",
        mapOf("limit" to limit.toString())
    )
    suspend fun getAlbum(id: String) = api.get<SaavnResponse<Album>>(
        "/albums",
        mapOf("id" to id)
    )    
    suspend fun getAlbumByLink(link: String) = api.get<SaavnResponse<Album>>(
        "/albums",
        mapOf("link" to link)
    )
    suspend fun getArtist(id: String) = api.get<SaavnResponse<Artist>>(
        "/artists",
        mapOf("id" to id)
    )  
    suspend fun getArtistByLink(link: String) = api.get<SaavnResponse<Artist>>(
        "/artists",
        mapOf("link" to link)
    )    
    suspend fun getArtistSongs(
        id: String, 
        page: Int = 0, 
        sortBy: String = "popularity", 
        sortOrder: String = "desc"
    ) = api.get<SaavnResponse<ArtistSongs>>(
        "/artists/$id/songs",
        mapOf(
            "page" to page.toString(),
            "sortBy" to sortBy,
            "sortOrder" to sortOrder
        )
    )    
    suspend fun getArtistAlbums(
        id: String, 
        page: Int = 0, 
        sortBy: String = "popularity", 
        sortOrder: String = "desc"
    ) = api.get<SaavnResponse<ArtistAlbums>>(
        "/artists/$id/albums",
        mapOf(
            "page" to page.toString(),
            "sortBy" to sortBy,
            "sortOrder" to sortOrder
        )
    )
    suspend fun getPlaylist(id: String) = api.get<SaavnResponse<Playlist>>(
        "/playlists",
        mapOf("id" to id)
    )
    
    suspend fun getPlaylistByLink(link: String) = api.get<SaavnResponse<Playlist>>(
        "/playlists",
        mapOf("link" to link)
    )  
    suspend fun getPlaylistWithSongs(
        id: String, 
        page: Int = 0, 
        limit: Int = 10
    ) = api.get<SaavnResponse<Playlist>>(
        "/playlists",
        mapOf(
            "id" to id,
            "page" to page.toString(),
            "limit" to limit.toString()
        )
    )
}