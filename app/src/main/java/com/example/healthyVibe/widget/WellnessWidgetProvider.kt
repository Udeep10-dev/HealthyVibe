package com.example.healthyVibe.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.healthyVibe.R
import com.example.healthyVibe.data.Storage

class WellnessWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_wellness)
        // compute percent from saved habits
        val storage = Storage(context)
        val habits = storage.loadHabits()
        var total = 0
        var done = 0
        habits.forEach {
            total += it.timesPerDay
            done += it.done
        }
        val percent = if (total == 0) 0 else (done * 100 / total)
        views.setTextViewText(R.id.tvWidgetPercent, "$percent%")
        manager.updateAppWidget(appWidgetId, views)
    }
}
