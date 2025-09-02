package dev.brahmkshatriya.echo.extension.saavn

import dev.brahmkshatriya.echo.common.helpers.ContinuationCallback.Companion.await
import dev.brahmkshatriya.echo.extension.saavn.models.TopArtistResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import okhttp3.OkHttpClient
import okhttp3.Request

class TrendingApi {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true 
    }  
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()
            builder.addHeader("Accept", "application/json")
            builder.addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Mobile Safari/537.36")
            chain.proceed(builder.build())
        }
        .build()    
    suspend fun getTopArtists(): TopArtistResponse {
        val request = Request.Builder()
            .url("https://jioapi.sryialsingh.workers.dev/get/top-artists")
            .get()
            .build()         
        val response = client.newCall(request).await()
        val raw = response.body.string()
        
        return json.decodeFromString(raw)
    }
}