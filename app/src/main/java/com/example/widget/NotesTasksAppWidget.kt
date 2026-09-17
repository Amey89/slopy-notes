package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesTasksAppWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            notifyWidgetUpdate(context)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_notes_tasks)
            val theme = WidgetTheme.getSelectedTheme(context)

            // Apply selected theme styling and high-contrast text color
            views.setInt(R.id.widget_root, "setBackgroundResource", theme.bgWidgetRes)
            views.setTextColor(R.id.widget_title_text, theme.titleTextColor)
            views.setTextColor(R.id.widget_count_text, theme.countBadgeTextColor)
            views.setTextColor(R.id.widget_empty_view, theme.metaTextColor)

            // Open App on header click
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header_container, openAppPendingIntent)

            // Set up RemoteViewsService for the scrollable list
            val serviceIntent = Intent(context, NotesTasksWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_list_view, serviceIntent)
            views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view)

            // PendingIntent template for list items
            val itemClickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val itemClickPendingIntent = PendingIntent.getActivity(
                context,
                101,
                itemClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list_view, itemClickPendingIntent)

            // Update item count badge
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val starredNotesCount = db.noteDao().getStarredNotesDirect().size
                    val starredTasksCount = db.taskDao().getStarredTasksWithSubtasksDirect().size
                    val total = starredNotesCount + starredTasksCount
                    views.setTextViewText(R.id.widget_count_text, "$total Starred")
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
                } catch (_: Exception) {}
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun notifyWidgetUpdate(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, NotesTasksAppWidget::class.java)
                )
                if (ids.isNotEmpty()) {
                    appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list_view)
                    for (id in ids) {
                        updateAppWidget(context, appWidgetManager, id)
                    }
                }
            } catch (_: Exception) {}
        }
    }
}
