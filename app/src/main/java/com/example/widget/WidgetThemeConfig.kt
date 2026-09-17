package com.example.widget

import android.content.Context
import android.graphics.Color
import com.example.R

enum class WidgetTheme(
    val key: String,
    val displayName: String,
    val isLight: Boolean,
    val bgWidgetRes: Int,
    val bgItemRes: Int,
    val titleTextColor: Int,
    val contentTextColor: Int,
    val metaTextColor: Int,
    val countBadgeTextColor: Int,
    val previewColorHex: String
) {
    LIGHT_CLEAN(
        key = "LIGHT_CLEAN",
        displayName = "Clean White",
        isLight = true,
        bgWidgetRes = R.drawable.bg_widget_light,
        bgItemRes = R.drawable.bg_widget_item_light,
        titleTextColor = Color.BLACK,
        contentTextColor = Color.BLACK,
        metaTextColor = Color.parseColor("#222222"),
        countBadgeTextColor = Color.BLACK,
        previewColorHex = "#FFFFFF"
    ),
    WARM_CREAM(
        key = "WARM_CREAM",
        displayName = "Warm Paper",
        isLight = true,
        bgWidgetRes = R.drawable.bg_widget_cream,
        bgItemRes = R.drawable.bg_widget_item_cream,
        titleTextColor = Color.BLACK,
        contentTextColor = Color.BLACK,
        metaTextColor = Color.parseColor("#222222"),
        countBadgeTextColor = Color.BLACK,
        previewColorHex = "#FBF9F5"
    ),
    DEEP_SLATE(
        key = "DEEP_SLATE",
        displayName = "Deep Slate",
        isLight = false,
        bgWidgetRes = R.drawable.bg_widget_dark,
        bgItemRes = R.drawable.bg_widget_item_dark,
        titleTextColor = Color.WHITE,
        contentTextColor = Color.WHITE,
        metaTextColor = Color.parseColor("#DDDDDD"),
        countBadgeTextColor = Color.WHITE,
        previewColorHex = "#12161C"
    ),
    OLED_BLACK(
        key = "OLED_BLACK",
        displayName = "OLED Pure Black",
        isLight = false,
        bgWidgetRes = R.drawable.bg_widget_oled,
        bgItemRes = R.drawable.bg_widget_item_oled,
        titleTextColor = Color.WHITE,
        contentTextColor = Color.WHITE,
        metaTextColor = Color.parseColor("#DDDDDD"),
        countBadgeTextColor = Color.WHITE,
        previewColorHex = "#000000"
    );

    companion object {
        private const val PREFS_NAME = "widget_prefs"
        private const val PREF_KEY_THEME = "pref_widget_theme"

        fun getSelectedTheme(context: Context): WidgetTheme {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedKey = prefs.getString(PREF_KEY_THEME, DEEP_SLATE.key)
            return values().find { it.key == savedKey } ?: DEEP_SLATE
        }

        fun setSelectedTheme(context: Context, theme: WidgetTheme) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(PREF_KEY_THEME, theme.key).apply()
        }
    }
}
