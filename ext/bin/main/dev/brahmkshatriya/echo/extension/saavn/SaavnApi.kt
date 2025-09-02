package dev.brahmkshatriya.echo.extension.saavn

import dev.brahmkshatriya.echo.common.helpers.ContinuationCallback.Companion.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SaavnApi {
    
    val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true 
    }
    
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()
            builder.addHeader("Accept", "application/json")
            builder.addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Mobile Safari/537.36")
            chain.proceed(builder.build())
        }
        .build()
    
    data class Response<T>(
        val json: T,
        val raw: String
    )
    
    suspend inline fun <reified T> get(
        endpoint: String,
        params: Map<String, String> = emptyMap()
    ): Response<T> {
        val url = buildString {
            append("https://jiosaavn-api-app.desikathabox.workers.dev/api")
            append(endpoint)
            if (params.isNotEmpty()) {
                append("?")
                append(params.entries.joinToString("&") { "${it.key}=${it.value}" })
            }
        }     
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
            
        val response = client.newCall(request).await()
        val raw = response.body.string()
        
        return Response(json.decodeFromString(raw), raw)
    }
    suspend inline fun <reified T> post(
        endpoint: String,
        body: String
    ): Response<T> {
        val url = "https://jiosaavn-api-app.desikathabox.workers.dev/api$endpoint"
        
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
            
        val response = client.newCall(request).await()
        val raw = response.body.string()
        
        return Response(json.decodeFromString(raw), raw)
    }
}
