package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.ui.theme.NoteAccentColorsDark
import com.example.ui.theme.NoteAccentColorsLight
import android.content.Intent
import android.net.Uri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CLEAN_MARKDOWN_REGEX = Regex("[#*`_~>\\[\\]]")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    performanceMode: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val palette = if (isDark) NoteAccentColorsDark else NoteAccentColorsLight
    val cardBg = palette.getOrElse(note.colorIndex) { MaterialTheme.colorScheme.surface }
    val isSharedLinkNote = note.folderId == FolderEntity.SHARED_LINKS_FOLDER_ID
    val context = LocalContext.current

    val imageList = remember(note.imageUris) {
        if (note.imageUris.isBlank()) emptyList() 
        else note.imageUris.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
    val tagsList = remember(note.tags) {
        if (note.tags.isBlank()) emptyList()
        else note.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    val extractedUrl = remember(note.content) {
        if (isSharedLinkNote || tagsList.contains("link")) {
            note.content.lines().firstOrNull { it.trim().startsWith("http://") || it.trim().startsWith("https://") }?.trim()
        } else null
    }

    val cardBorder = if (isSharedLinkNote) {
        androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2563EB).copy(alpha = 0.55f))
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Title (with Link badge if shared) + Star button + Delete icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSharedLinkNote) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF2563EB).copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = note.title.ifBlank { if (isSharedLinkNote) "Shared Link" else "Untitled Note" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleStar,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (note.isStarred) "Unstar" else "Star",
                            tint = if (note.isStarred) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Rich Shared Link Hero Thumbnail (e.g. YouTube thumbnail or OpenGraph preview image)
            if (isSharedLinkNote && imageList.isNotEmpty()) {
                val heroUrl = imageList.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(if (heroUrl.startsWith("http")) heroUrl else File(heroUrl))
                            .crossfade(!performanceMode)
                            .build(),
                        contentDescription = "Link thumbnail preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (tagsList.contains("video")) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "Video",
                                tint = Color.White.copy(alpha = 0.95f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }

            // Note Content Snippet
            if (note.content.isNotBlank()) {
                val cleanPreview = remember(note.content, isSharedLinkNote, performanceMode) {
                    var text = note.content
                    if (isSharedLinkNote) {
                        val firstHttp = text.indexOf("http")
                        if (firstHttp != -1) {
                            val endOfLine = text.indexOf('\n', firstHttp)
                            text = if (endOfLine != -1) {
                                text.substring(0, firstHttp) + text.substring(endOfLine + 1)
                            } else {
                                text.substring(0, firstHttp)
                            }
                        }
                    }
                    val snippet = if (text.length > 280) text.substring(0, 280) else text
                    if (performanceMode) snippet.trim() else CLEAN_MARKDOWN_REGEX.replace(snippet, "").trim()
                }
                if (cleanPreview.isNotBlank()) {
                    Text(
                        text = cleanPreview,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        ),
                        maxLines = if (isSharedLinkNote && imageList.isNotEmpty()) 2 else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Standard Note Image Thumbnails (for non-shared-link notes)
            if (!isSharedLinkNote && imageList.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayedImages = remember(imageList) { imageList.take(3) }
                    displayedImages.forEach { imgPath ->
                        val imageRequest = remember(imgPath) {
                            val model = if (imgPath.startsWith("http")) imgPath else File(imgPath)
                            ImageRequest.Builder(context)
                                .data(model)
                                .size(168, 168)
                                .crossfade(false)
                                .build()
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            AsyncImage(
                                model = imageRequest,
                                contentDescription = "Attached thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    if (imageList.size > 3) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "+${imageList.size - 3}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Clickable URL Pill for Shared Links
            if (extractedUrl != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2563EB).copy(alpha = 0.12f),
                    modifier = Modifier.clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(extractedUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = extractedUrl.removePrefix("https://").removePrefix("http://").take(42),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1D4ED8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in browser",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Tags (Single-pass row layout to keep scrolling locked at 60 FPS)
            if (tagsList.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayedTags = remember(tagsList) { tagsList.take(3) }
                    displayedTags.forEach { tag ->
                        TagChip(tag = tag)
                    }
                    if (tagsList.size > 3) {
                        Text(
                            text = "+${tagsList.size - 3}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            // Footer: Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppDateFormatter.format(note.updatedAt),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (imageList.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${imageList.size}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
