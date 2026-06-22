package com.todohub.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.todohub.app.MainActivity
import com.todohub.app.R

class WeekWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_week_item)
            val adapterIntent = Intent(context, TodoWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                putExtra("filter", "week")
                data = android.net.Uri.parse(toUri(id))
            }
            views.setRemoteAdapter(R.id.widgetList, adapterIntent)
            views.setEmptyView(R.id.widgetList, R.id.widgetEmpty)
            val clickIntent = Intent(context, MainActivity::class.java)
            val pi = PendingIntent.getActivity(context, id + 1000, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setPendingIntentTemplate(R.id.widgetList, pi)
            views.setTextViewText(R.id.widgetTitle, "本周待办")
            mgr.updateAppWidget(id, views)
        }
        super.onUpdate(context, mgr, ids)
    }

    private fun toUri(id: Int): String = "todohub://widget/week/$id"
}
