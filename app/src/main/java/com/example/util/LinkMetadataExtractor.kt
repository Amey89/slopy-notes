package com.example.util

import android.util.Patterns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class LinkMetadata(
    val url: String,
    val title: String,
    val description: String = "",
    val imageUrl: String = "",
    val domain: String = "",
    val isYouTube: Boolean = false
)

object LinkMetadataExtractor {

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(4, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val YOUTUBE_REGEX = Regex(
        "(?:youtu\\.be/|youtube\\.com/(?:embed/|v/|shorts/|watch\\?v=|watch\\?.+&v=))([\\w-]{11})",
        RegexOption.IGNORE_CASE
    )

    /**
     * Extracts the first valid HTTP/HTTPS URL from any shared text.
     */
    fun extractUrl(text: String): String? {
        val matcher = Patterns.WEB_URL.matcher(text)
        while (matcher.find()) {
            var candidate = matcher.group()
            if (!candidate.startsWith("http://") && !candidate.startsWith("https://")) {
                candidate = "https://$candidate"
            }
            return candidate
        }
        return null
    }

    /**
     * Extracts domain host from URL (e.g. "youtube.com").
     */
    fun extractDomain(url: String): String {
        return try {
            val uri = URI(url)
            val host = uri.host ?: ""
            host.removePrefix("www.")
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Extracts YouTube Video ID if present.
     */
    fun extractYouTubeId(url: String): String? {
        val match = YOUTUBE_REGEX.find(url)
        return match?.groupValues?.getOrNull(1)
    }

    /**
     * Resolves metadata for the given URL or shared text completely offline-friendly with zero AI.
     * Uses OpenGraph / HTML meta tags, YouTube oEmbed (no API key needed), and graceful offline fallbacks.
     */
    suspend fun resolve(sharedText: String, hintSubject: String? = null): LinkMetadata = withContext(Dispatchers.IO) {
        val extractedUrl = extractUrl(sharedText) ?: sharedText.trim()
        val domain = extractDomain(extractedUrl).ifBlank { "link" }
        val ytVideoId = extractYouTubeId(extractedUrl)

        // For YouTube, thumbnail URL is deterministic and available completely offline
        val ytThumbnail = if (ytVideoId != null) {
            "https://img.youtube.com/vi/$ytVideoId/hqdefault.jpg"
        } else ""

        val fallbackTitle = when {
            !hintSubject.isNullOrBlank() -> hintSubject.trim()
            else -> generateFallbackTitle(sharedText, extractedUrl, domain)
        }

        var metadata = LinkMetadata(
            url = extractedUrl,
            title = fallbackTitle,
            description = "",
            imageUrl = ytThumbnail,
            domain = domain,
            isYouTube = ytVideoId != null
        )

        // 1. If YouTube link, try YouTube oEmbed first (fast, reliable, returns title + thumbnail)
        if (ytVideoId != null) {
            try {
                val oEmbedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$ytVideoId&format=json"
                val request = Request.Builder()
                    .url(oEmbedUrl)
                    .header("User-Agent", "Mozilla/5.0")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            val json = JSONObject(body)
                            val title = json.optString("title", fallbackTitle)
                            val author = json.optString("author_name", "")
                            val thumb = json.optString("thumbnail_url", ytThumbnail)
                            return@withContext LinkMetadata(
                                url = extractedUrl,
                                title = title,
                                description = if (author.isNotBlank()) "Channel: $author" else "",
                                imageUrl = thumb.ifBlank { ytThumbnail },
                                domain = "youtube.com",
                                isYouTube = true
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                // Offline or timeout; use fallback metadata with offline YouTube thumbnail!
            }
            return@withContext metadata
        }

        // 2. For non-YouTube links, attempt HTML / OpenGraph parsing
        try {
            val request = Request.Builder()
                .url(extractedUrl)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0) Gecko/120.0 Firefox/120.0")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        metadata = parseHtmlMetadata(body, extractedUrl, domain, fallbackTitle)
                    }
                }
            }
        } catch (_: Exception) {
            // Network failure / offline: keep offline fallback
        }

        metadata
    }

    private fun parseHtmlMetadata(html: String, url: String, domain: String, fallbackTitle: String): LinkMetadata {
        // Find title: og:title -> twitter:title -> <title>
        val ogTitle = findMetaContent(html, "property", "og:title")
            ?: findMetaContent(html, "name", "twitter:title")
            ?: findTagContent(html, "title")

        // Find description: og:description -> twitter:description -> meta description
        val ogDesc = findMetaContent(html, "property", "og:description")
            ?: findMetaContent(html, "name", "twitter:description")
            ?: findMetaContent(html, "name", "description")

        // Find image: og:image -> twitter:image -> link rel=image_src
        var ogImage = findMetaContent(html, "property", "og:image")
            ?: findMetaContent(html, "property", "og:image:url")
            ?: findMetaContent(html, "name", "twitter:image")
            ?: findMetaContent(html, "name", "twitter:image:src")

        if (ogImage != null && !ogImage.startsWith("http://") && !ogImage.startsWith("https://")) {
            ogImage = resolveRelativeUrl(url, ogImage)
        }

        val cleanedTitle = cleanHtmlEntities(ogTitle?.trim() ?: fallbackTitle)
        val cleanedDesc = cleanHtmlEntities(ogDesc?.trim() ?: "")

        return LinkMetadata(
            url = url,
            title = cleanedTitle.ifBlank { fallbackTitle },
            description = cleanedDesc,
            imageUrl = ogImage?.trim() ?: "",
            domain = domain,
            isYouTube = false
        )
    }

    private fun findMetaContent(html: String, attrName: String, attrValue: String): String? {
        val pattern = Pattern.compile(
            "<meta\\s+[^>]*$attrName=[\"']${Pattern.quote(attrValue)}[\"'][^>]*content=[\"']([^\"']*)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(html)
        if (matcher.find()) {
            return matcher.group(1)
        }
        val patternRev = Pattern.compile(
            "<meta\\s+[^>]*content=[\"']([^\"']*)[\"'][^>]*$attrName=[\"']${Pattern.quote(attrValue)}[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE
        )
        val matcherRev = patternRev.matcher(html)
        if (matcherRev.find()) {
            return matcherRev.group(1)
        }
        return null
    }

    private fun findTagContent(html: String, tagName: String): String? {
        val pattern = Pattern.compile("<$tagName[^>]*>([^<]*)</$tagName>", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(html)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    private fun resolveRelativeUrl(baseUrl: String, relativePath: String): String {
        return try {
            val base = URI(baseUrl)
            base.resolve(relativePath).toString()
        } catch (_: Exception) {
            relativePath
        }
    }

    private fun cleanHtmlEntities(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&#x27;", "'")
            .replace("&#x2F;", "/")
            .replace("&nbsp;", " ")
            .trim()
    }

    private fun generateFallbackTitle(sharedText: String, url: String, domain: String): String {
        val remaining = sharedText.replace(url, "").trim().trim('-', ':', '|')
        if (remaining.isNotBlank() && remaining.length > 3) {
            return remaining.take(80)
        }
        return try {
            val uri = URI(url)
            val path = uri.path?.trim('/') ?: ""
            if (path.isNotBlank()) {
                val lastSegment = path.substringAfterLast('/').replace('-', ' ').replace('_', ' ')
                if (lastSegment.isNotBlank()) {
                    "$domain - ${lastSegment.replaceFirstChar { it.uppercase() }}"
                } else {
                    domain
                }
            } else {
                domain.replaceFirstChar { it.uppercase() }
            }
        } catch (_: Exception) {
            if (domain.isNotBlank()) domain else "Shared Link"
        }
    }
}
