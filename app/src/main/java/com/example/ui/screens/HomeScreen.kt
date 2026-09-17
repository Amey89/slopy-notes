package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.model.TaskWithSubtasks
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.CreateTagDialog
import com.example.ui.components.FolderBreadcrumbBar
import com.example.ui.components.FolderCard
import com.example.ui.components.NoteCard
import com.example.ui.components.RenameFolderDialog
import com.example.ui.components.SearchFilter
import com.example.ui.components.TaskCard
import com.example.ui.components.UniversalSearchBar
import com.example.ui.theme.appBackground
import com.example.ui.viewmodel.NotesTasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NotesTasksViewModel,
    onOpenNote: (NoteEntity?) -> Unit,
    onOpenTask: (TaskWithSubtasks?) -> Unit,
    onOpenSettings: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()

    val notesList by viewModel.notesList.collectAsState()
    val tasksList by viewModel.tasksList.collectAsState()

    val allNotes by viewModel.allNotes.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allFolders by viewModel.allFolders.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val selectedTagFilter by viewModel.selectedTagFilter.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()

    val themePreset by viewModel.themePreset.collectAsState()
    val backgroundPattern by viewModel.backgroundPattern.collectAsState()

    val driveConnectionState by viewModel.driveConnectionState.collectAsState()
    val driveSyncState by viewModel.driveSyncState.collectAsState()
    
    val performanceMode by viewModel.performanceMode.collectAsState()

    // Memoized maps for high-performance fluid rendering without stutter (collected from ViewModel)
    val folderNotesCountMap by viewModel.folderNotesCountMap.collectAsState()
    val folderTasksCountMap by viewModel.folderTasksCountMap.collectAsState()
    val subfoldersMap by viewModel.subfoldersMap.collectAsState()
    val visibleFolders by viewModel.visibleFolders.collectAsState()

    var showActionSheet by remember { mutableStateOf(false) }
    var showCreateTagDialog by remember { mutableStateOf(false) }

    // Folder Dialog States
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var createFolderParent by remember { mutableStateOf<FolderEntity?>(null) }
    var folderToRename by remember { mutableStateOf<FolderEntity?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderEntity?>(null) }

    // Android back gesture/button support for folders and search
    BackHandler(enabled = selectedFolder != null || searchQuery.isNotBlank()) {
        if (searchQuery.isNotBlank()) {
            viewModel.setSearchQuery("")
        } else if (selectedFolder != null) {
            viewModel.navigateUpFolder()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = if (backgroundPattern > 0) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = if (selectedFolder != null) {
                    {
                        IconButton(
                            onClick = { viewModel.navigateUpFolder() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to parent folder",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    {}
                },
                title = {
                    if (selectedFolder != null) {
                        Text(
                            text = selectedFolder!!.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    if (driveConnectionState is com.example.data.sync.DriveConnectionState.Connected) {
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.size(48.dp)
                        ) {
                            val icon = when (driveSyncState) {
                                is com.example.data.sync.DriveSyncState.Syncing -> Icons.Default.CloudSync
                                else -> Icons.Default.CloudDone
                            }
                            val tint = when (driveSyncState) {
                                is com.example.data.sync.DriveSyncState.Error -> MaterialTheme.colorScheme.error
                                is com.example.data.sync.DriveSyncState.Syncing -> MaterialTheme.colorScheme.primary
                                else -> Color(0xFF16A34A)
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = "Google Drive Sync",
                                tint = tint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            // Large comfortable FAB for thick fingers
            FloatingActionButton(
                onClick = { showActionSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Item",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pinned Universal Search Bar with Filter Chips
            // Isolating the search bar from the LazyColumn eliminates layout thrashing,
            // avoids item recycling churn, and stops horizontal/vertical scroll gesture fighting.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 4.dp)
            ) {
                UniversalSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    activeFilter = activeFilter,
                    onFilterSelect = { viewModel.setActiveFilter(it) },
                    allFolders = allFolders,
                    allNotes = allNotes,
                    allTasks = allTasks,
                    allTags = allTags,
                    selectedTagFilter = selectedTagFilter,
                    onSelectTagFilter = { viewModel.setSelectedTagFilter(it) },
                    onOpenNote = { note -> onOpenNote(note) },
                    onOpenTask = { task -> onOpenTask(task) },
                    onSelectFolder = { folder -> viewModel.selectFolder(folder) },
                    performanceMode = performanceMode
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Folder Breadcrumb Navigation (shown when navigating inside a folder)
                if (selectedFolder != null) {
                    item(key = "breadcrumb_${selectedFolder?.id}", contentType = "breadcrumb") {
                        FolderBreadcrumbBar(
                            currentFolder = selectedFolder!!,
                            allFolders = allFolders,
                            onExitFolder = { viewModel.selectFolder(null) }
                        )
                    }
                }

                // 2. Folders Section
                val showFolders = (activeFilter in listOf(SearchFilter.ALL, SearchFilter.FOLDERS, SearchFilter.NOTES, SearchFilter.TASKS)) &&
                        visibleFolders.isNotEmpty()

                if (showFolders) {
                    item(key = "folders_header", contentType = "header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedFolder == null) "FOLDERS (${visibleFolders.size})"
                                else "SUBFOLDERS (${visibleFolders.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )

                            val isCurrentPermanent = selectedFolder?.id == FolderEntity.COMPLETED_TASKS_FOLDER_ID || selectedFolder?.id == FolderEntity.SHARED_LINKS_FOLDER_ID
                            if (!isCurrentPermanent) {
                                TextButton(
                                    onClick = {
                                        createFolderParent = selectedFolder
                                        showCreateFolderDialog = true
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (selectedFolder == null) "New Folder" else "New Subfolder", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    items(
                        items = visibleFolders, 
                        key = { "folder_${it.id}" },
                        contentType = { "folder" }
                    ) { folder ->
                        val subfolders = subfoldersMap[folder.id] ?: emptyList()
                        val folderNotesCount = folderNotesCountMap[folder.id] ?: 0
                        val folderTasksCount = folderTasksCountMap[folder.id] ?: 0
                        val isPermanent = folder.id == FolderEntity.COMPLETED_TASKS_FOLDER_ID || folder.id == FolderEntity.SHARED_LINKS_FOLDER_ID

                        FolderCard(
                            folder = folder,
                            subfolders = subfolders,
                            notesCount = folderNotesCount,
                            tasksCount = folderTasksCount,
                            onClick = { viewModel.selectFolder(folder) },
                            onAddSubfolder = {
                                if (!isPermanent) {
                                    createFolderParent = folder
                                    showCreateFolderDialog = true
                                }
                            },
                            onRename = {
                                if (!isPermanent) {
                                    folderToRename = folder
                                }
                            },
                            onDelete = {
                                if (!isPermanent) {
                                    folderToDelete = folder
                                }
                            },
                            performanceMode = performanceMode
                        )
                    }
                }

                // 3. Tasks Section
                val showTasks = (activeFilter in listOf(SearchFilter.ALL, SearchFilter.TASKS, SearchFilter.REMINDERS, SearchFilter.STARRED))

                if (showTasks && tasksList.isNotEmpty()) {
                    item(key = "tasks_header", contentType = "header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedFolder?.id == FolderEntity.COMPLETED_TASKS_FOLDER_ID)
                                    "PERMANENT COMPLETED TASKS (${tasksList.size})"
                                else
                                    "TASKS (${tasksList.count { !it.task.isCompleted }} pending)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedFolder?.id == FolderEntity.COMPLETED_TASKS_FOLDER_ID) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    items(
                        items = tasksList, 
                        key = { "task_${it.task.id}" },
                        contentType = { "task" }
                    ) { taskWithSubtasks ->
                        TaskCard(
                            taskWithSubtasks = taskWithSubtasks,
                            onClick = { onOpenTask(taskWithSubtasks) },
                            onToggleCompleted = { isDone ->
                                viewModel.toggleTaskCompleted(taskWithSubtasks.task.id, isDone)
                            },
                            onToggleStar = {
                                viewModel.toggleTaskStar(taskWithSubtasks)
                            },
                            onToggleSubtask = { subtask, isDone ->
                                viewModel.toggleSubtaskCompleted(subtask.id, isDone)
                            },
                            onDelete = {
                                viewModel.deleteTask(taskWithSubtasks.task.id)
                            },
                            performanceMode = performanceMode
                        )
                    }
                }

                // 4. Notes Section
                val showNotes = (activeFilter in listOf(SearchFilter.ALL, SearchFilter.NOTES, SearchFilter.STARRED))

                if (showNotes && notesList.isNotEmpty()) {
                    item(key = "notes_header", contentType = "header") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedFolder?.id == FolderEntity.SHARED_LINKS_FOLDER_ID)
                                "PERMANENT SHARED LINKS (${notesList.size})"
                            else
                                "NOTES (${notesList.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedFolder?.id == FolderEntity.SHARED_LINKS_FOLDER_ID) Color(0xFF2563EB) else MaterialTheme.colorScheme.secondary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(
                        items = notesList, 
                        key = { "note_${it.id}" },
                        contentType = { "note" }
                    ) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onOpenNote(note) },
                            onToggleStar = {
                                viewModel.toggleNoteStar(note)
                            },
                            onDelete = {
                                viewModel.deleteNote(note.id)
                            },
                            performanceMode = performanceMode
                        )
                    }
                }

                // Empty State
                val isEmpty = (notesList.isEmpty() && tasksList.isEmpty() && visibleFolders.isEmpty())
                if (isEmpty) {
                    item(key = "empty_state", contentType = "empty_state") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when {
                                                selectedFolder?.id == FolderEntity.SHARED_LINKS_FOLDER_ID -> Icons.Default.Link
                                                selectedFolder?.id == FolderEntity.COMPLETED_TASKS_FOLDER_ID -> Icons.Default.CheckCircle
                                                selectedFolder != null -> Icons.Default.Folder
                                                else -> Icons.Default.Description
                                            },
                                            contentDescription = null,
                                            tint = if (selectedFolder?.id == FolderEntity.SHARED_LINKS_FOLDER_ID) Color(0xFF2563EB) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No results found"
                                    else if (selectedFolder?.id == FolderEntity.COMPLETED_TASKS_FOLDER_ID) "No completed tasks yet"
                                    else if (selectedFolder?.id == FolderEntity.SHARED_LINKS_FOLDER_ID) "No shared links yet"
                                    else if (selectedFolder != null) "Folder is empty"
                                    else "No notes or tasks yet",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Try changing search terms or switching filter."
                                    else if (selectedFolder?.id == FolderEntity.COMPLETED_TASKS_FOLDER_ID) "Completed tasks and reminders automatically archive here."
                                    else if (selectedFolder?.id == FolderEntity.SHARED_LINKS_FOLDER_ID) "Share links from your browser, YouTube, or other apps to save rich previews here."
                                    else "Tap the + button below to create your first note, task, or folder.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                )
                            }
                        }
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }

    // Modal Action Sheet for Big Comfortable Touch Targets
    if (showActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (selectedFolder != null) "Create in '${selectedFolder?.name}'" else "Create New",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )

                // 1. New Rich Note
                ActionSheetItem(
                    icon = Icons.Default.Description,
                    iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "New Note",
                    subtitle = "Markdown, rich formatting & attachments",
                    onClick = {
                        showActionSheet = false
                        onOpenNote(null)
                    }
                )

                // 2. New Task + Reminder
                ActionSheetItem(
                    icon = Icons.Default.FormatListBulleted,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "New Task + Reminder",
                    subtitle = "Subtasks, priorities & full-screen alerts",
                    onClick = {
                        showActionSheet = false
                        onOpenTask(null)
                    }
                )

                // 3. New Folder / Subfolder
                ActionSheetItem(
                    icon = Icons.Default.CreateNewFolder,
                    iconBgColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = if (selectedFolder != null) "New Subfolder" else "New Folder",
                    subtitle = "Organize notes and tasks hierarchically",
                    onClick = {
                        showActionSheet = false
                        createFolderParent = selectedFolder
                        showCreateFolderDialog = true
                    }
                )

                // 4. New Permanent Tag
                ActionSheetItem(
                    icon = Icons.Default.LocalOffer,
                    iconBgColor = Color(0xFFFDE68A).copy(alpha = 0.5f),
                    iconTint = Color(0xFFD97706),
                    title = "New Tag",
                    subtitle = "Create permanent tag with custom color",
                    onClick = {
                        showActionSheet = false
                        showCreateTagDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Dialog: Create Tag
    if (showCreateTagDialog) {
        CreateTagDialog(
            onDismiss = { showCreateTagDialog = false },
            onConfirm = { name, colorIdx ->
                viewModel.createTag(name, colorIdx)
                showCreateTagDialog = false
            }
        )
    }

    // Dialog: Create Folder
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            parentFolder = createFolderParent,
            defaultFolderType = if (activeFilter == SearchFilter.TASKS || activeFilter == SearchFilter.REMINDERS) "TASK" else "NOTE",
            onDismiss = {
                showCreateFolderDialog = false
                createFolderParent = null
            },
            onConfirm = { name, folderType, colorIdx ->
                viewModel.createFolder(
                    name = name,
                    folderType = folderType,
                    parentFolderId = createFolderParent?.id,
                    colorIndex = colorIdx
                )
                showCreateFolderDialog = false
                createFolderParent = null
            }
        )
    }

    // Dialog: Rename Folder
    if (folderToRename != null) {
        RenameFolderDialog(
            folder = folderToRename!!,
            onDismiss = { folderToRename = null },
            onConfirm = { newName ->
                viewModel.updateFolder(folderToRename!!.copy(name = newName))
                folderToRename = null
            }
        )
    }

    // Dialog: Delete Folder
    if (folderToDelete != null) {
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("Delete Folder?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete '${folderToDelete?.name}'? Notes and tasks inside will remain safe and be moved to root."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFolder(folderToDelete!!.id)
                        folderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { folderToDelete = null },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ActionSheetItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}
