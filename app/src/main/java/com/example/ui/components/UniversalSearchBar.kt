package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.LocalOffer
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.model.TagEntity
import com.example.data.model.TaskWithSubtasks
import com.example.ui.components.FolderColors

enum class SearchFilter(val label: String) {
    ALL("All"),
    NOTES("Notes"),
    TASKS("Tasks"),
    FOLDERS("Folders"),
    STARRED("Starred"),
    REMINDERS("Reminders")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UniversalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    activeFilter: SearchFilter,
    onFilterSelect: (SearchFilter) -> Unit,
    allFolders: List<FolderEntity> = emptyList(),
    allNotes: List<NoteEntity> = emptyList(),
    allTasks: List<TaskWithSubtasks> = emptyList(),
    allTags: List<TagEntity> = emptyList(),
    selectedTagFilter: String? = null,
    onSelectTagFilter: (String?) -> Unit = {},
    onOpenNote: (NoteEntity) -> Unit = {},
    onOpenTask: (TaskWithSubtasks) -> Unit = {},
    onSelectFolder: (FolderEntity) -> Unit = {},
    modifier: Modifier = Modifier,
    performanceMode: Boolean = false
) {
    var isPopupOpen by rememberSaveable { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()

    BackHandler(enabled = isPopupOpen) {
        isPopupOpen = false
    }

    val trimmedQuery = query.trim()

    val matchedFolders = remember(allFolders, trimmedQuery) {
        if (trimmedQuery.isBlank()) emptyList()
        else allFolders.filter { it.name.contains(trimmedQuery, ignoreCase = true) }
    }

    val matchedTasks = remember(allTasks, trimmedQuery) {
        if (trimmedQuery.isBlank()) emptyList()
        else allTasks.filter { tws ->
            tws.task.title.contains(trimmedQuery, ignoreCase = true) ||
            tws.task.description.contains(trimmedQuery, ignoreCase = true) ||
            tws.task.tags.contains(trimmedQuery, ignoreCase = true) ||
            tws.subtasks.any { it.title.contains(trimmedQuery, ignoreCase = true) }
        }
    }

    val matchedNotes = remember(allNotes, trimmedQuery) {
        if (trimmedQuery.isBlank()) emptyList()
        else allNotes.filter { note ->
            note.title.contains(trimmedQuery, ignoreCase = true) ||
            note.content.contains(trimmedQuery, ignoreCase = true) ||
            note.tags.contains(trimmedQuery, ignoreCase = true)
        }
    }

    val matchedTags = remember(allTags, trimmedQuery) {
        if (trimmedQuery.isBlank()) emptyList()
        else allTags.filter { it.name.contains(trimmedQuery.removePrefix("#"), ignoreCase = true) }
    }

    val totalMatches = matchedFolders.size + matchedTasks.size + matchedNotes.size + matchedTags.size

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                if (it.isNotBlank()) {
                    isPopupOpen = true
                }
            },
            placeholder = { Text("Search notes, tasks, folders...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onQueryChange("")
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (isPopupOpen) {
                        IconButton(
                            onClick = { isPopupOpen = false },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search popup",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (it.isFocused) {
                        isPopupOpen = true
                    }
                }
        )

        // Smooth-transitioned Dynamic-Height Search Popup Window
        AnimatedVisibility(
            visible = isPopupOpen,
            enter = if (performanceMode) androidx.compose.animation.EnterTransition.None else expandVertically(animationSpec = tween(180)) + fadeIn(animationSpec = tween(150)),
            exit = if (performanceMode) androidx.compose.animation.ExitTransition.None else shrinkVertically(animationSpec = tween(150)) + fadeOut(animationSpec = tween(120))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header inside popup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (trimmedQuery.isBlank()) "Quick Search"
                            else "Results ($totalMatches)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        TextButton(
                            onClick = { isPopupOpen = false },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Done", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Prompt when empty
                    if (trimmedQuery.isBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Type above to search folders, notes, and tasks",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    } else if (totalMatches == 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "No results found for \"$trimmedQuery\"",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Check your spelling or try another keyword",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // 1. Matching Tags
                        if (matchedTags.isNotEmpty()) {
                            Text(
                                text = "MATCHING TAGS (${matchedTags.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                matchedTags.forEach { tagEntity ->
                                    val tagColor = TagPaletteColors.getOrElse(tagEntity.colorIndex) { MaterialTheme.colorScheme.primary }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = tagColor.copy(alpha = 0.16f),
                                        border = BorderStroke(1.dp, tagColor.copy(alpha = 0.4f)),
                                        modifier = Modifier.clickable {
                                            onSelectTagFilter(tagEntity.name)
                                            onQueryChange("")
                                            isPopupOpen = false
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalOffer,
                                                contentDescription = null,
                                                tint = tagColor,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "#${tagEntity.name}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tagColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Matched Folders
                        if (matchedFolders.isNotEmpty()) {
                            Text(
                                text = "FOLDERS (${matchedFolders.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )

                            val defaultFolderColor = MaterialTheme.colorScheme.primary
                            matchedFolders.forEach { folder ->
                                val folderColor = FolderColors.getOrElse(folder.colorIndex) {
                                    defaultFolderColor
                                }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isPopupOpen = false
                                            onSelectFolder(folder)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(folderColor.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = folderColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = folder.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Folder • ${folder.folderType.lowercase()}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Matched Tasks
                        if (matchedTasks.isNotEmpty()) {
                            Text(
                                text = "TASKS (${matchedTasks.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )

                            matchedTasks.forEach { tws ->
                                val task = tws.task
                                val isDone = task.isCompleted
                                val taskBg = if (isDone) {
                                    if (isDark) Color(0xFF132B1A) else Color(0xFFEBF7EE)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                }
                                val taskBorder = if (isDone) {
                                    BorderStroke(1.dp, if (isDark) Color(0xFF234E2E) else Color(0xFFB7E4C7))
                                } else null

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isPopupOpen = false
                                            onOpenTask(tws)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = taskBg),
                                    border = taskBorder
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isDone) Color(0xFF16A34A) else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = task.title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                                color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                                else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (task.description.isNotBlank()) {
                                                Text(
                                                    text = task.description,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Matched Notes
                        if (matchedNotes.isNotEmpty()) {
                            Text(
                                text = "NOTES (${matchedNotes.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )

                            matchedNotes.forEach { note ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isPopupOpen = false
                                            onOpenNote(note)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = note.title.ifBlank { "Untitled Note" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (note.content.isNotBlank()) {
                                                Text(
                                                    text = note.content.take(70).replace(Regex("[#*`_~>\\[\\]]"), "").trim(),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Chips Row (LazyRow for fluid recycling and 60 FPS scrolling)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(SearchFilter.entries, key = { it.name }) { filter ->
                val isSelected = activeFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterSelect(filter) },
                    modifier = Modifier.height(38.dp),
                    label = {
                        Text(
                            filter.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        when (filter) {
                            SearchFilter.ALL -> null
                            SearchFilter.NOTES -> Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            SearchFilter.TASKS -> Icon(Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(16.dp))
                            SearchFilter.FOLDERS -> Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                            SearchFilter.REMINDERS -> Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                            SearchFilter.STARRED -> Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // Active Tag filter chip if any
            if (selectedTagFilter != null) {
                item(key = "active_tag_filter") {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .height(38.dp)
                            .clickable { onSelectTagFilter(null) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "#$selectedTagFilter",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear tag filter",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Quick Tag Filter Chips
            items(allTags, key = { "tag_${it.name}" }) { tagEntity ->
                val isTagSelected = selectedTagFilter.equals(tagEntity.name, ignoreCase = true)
                val tagColor = TagPaletteColors.getOrElse(tagEntity.colorIndex) { MaterialTheme.colorScheme.primary }
                FilterChip(
                    selected = isTagSelected,
                    onClick = {
                        onSelectTagFilter(if (isTagSelected) null else tagEntity.name)
                    },
                    modifier = Modifier.height(38.dp),
                    label = {
                        Text(
                            text = "#${tagEntity.name}",
                            fontSize = 13.sp,
                            fontWeight = if (isTagSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(tagColor, CircleShape)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = tagColor.copy(alpha = 0.2f),
                        selectedLabelColor = tagColor
                    )
                )
            }
        }
    }
}
