with open("app/src/main/java/com/example/ui/components/UniversalSearchBar.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    new_lines.append(line)

insert_idx = 222
content = """        // Smooth-transitioned Dynamic-Height Search Popup Window
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
"""

lines.insert(insert_idx, content)
with open("app/src/main/java/com/example/ui/components/UniversalSearchBar.kt", "w") as f:
    f.writelines(lines)
