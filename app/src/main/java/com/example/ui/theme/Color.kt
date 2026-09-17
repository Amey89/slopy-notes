package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Scheme Colors - Passive, grounded, zero neon/glow aesthetic
val PrimaryLight = Color(0xFF2E3A48)       // Deep Charcoal Slate (calm, non-neon)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE2E8F0) // Soft muted slate
val OnPrimaryContainerLight = Color(0xFF1E293B)

val SecondaryLight = Color(0xFF4A5568)     // Subtle muted slate blue
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFEDF2F7)
val OnSecondaryContainerLight = Color(0xFF2D3748)

val TertiaryLight = Color(0xFF6B5B4D)      // Earthy warm stone
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFF5EFEB)
val OnTertiaryContainerLight = Color(0xFF382E25)

val BackgroundLight = Color(0xFFF9FAFB)    // Clean flat off-white
val OnBackgroundLight = Color(0xFF111827)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF111827)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnSurfaceVariantLight = Color(0xFF64748B)
val OutlineLight = Color(0xFFE2E8F0)

// Dark Scheme Colors - Deep graphite/slate voids, zero neon/purple glow
val PrimaryDark = Color(0xFF94A3B8)        // Passive soft pewter/slate
val OnPrimaryDark = Color(0xFF0F172A)
val PrimaryContainerDark = Color(0xFF273546)
val OnPrimaryContainerDark = Color(0xFFE2E8F0)

val SecondaryDark = Color(0xFF8896A6)      // Muted steel
val OnSecondaryDark = Color(0xFF0F172A)
val SecondaryContainerDark = Color(0xFF1E293B)
val OnSecondaryContainerDark = Color(0xFFCBD5E1)

val TertiaryDark = Color(0xFFB5A492)       // Muted warm stone
val OnTertiaryDark = Color(0xFF2E261E)
val TertiaryContainerDark = Color(0xFF382F26)
val OnTertiaryContainerDark = Color(0xFFF5EFEB)

val BackgroundDark = Color(0xFF12161C)     // Calm flat dark graphite
val OnBackgroundDark = Color(0xFFF1F5F9)
val SurfaceDark = Color(0xFF181F28)        // Slate charcoal
val OnSurfaceDark = Color(0xFFF1F5F9)
val SurfaceVariantDark = Color(0xFF222B36)
val OnSurfaceVariantDark = Color(0xFF94A3B8)
val OutlineDark = Color(0xFF2E3846)

// Note Card Accent Colors - Soft, muted, organic paper/earth tones (no neon)
val NoteAccentColorsLight = listOf(
    Color(0xFFFFFFFF), // 0: Pure clean white
    Color(0xFFF6F3ED), // 1: Soft warm oat
    Color(0xFFEFF4F0), // 2: Soft pale sage
    Color(0xFFEDF2F7), // 3: Soft muted slate
    Color(0xFFF7EFEF), // 4: Soft pale clay
    Color(0xFFEEF3F5)  // 5: Soft misty mineral
)

val NoteAccentColorsDark = listOf(
    Color(0xFF181F28), // 0: Default flat slate
    Color(0xFF26221D), // 1: Deep warm oat
    Color(0xFF1B2621), // 2: Deep pale sage
    Color(0xFF1E252F), // 3: Deep muted slate
    Color(0xFF271F22), // 4: Deep clay
    Color(0xFF1C2528)  // 5: Deep mineral
)
