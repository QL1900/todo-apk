package com.todohub.app.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.todohub.app.R
import com.todohub.app.db.TodoDatabaseHelper
import com.todohub.app.model.Todo
import com.todohub.app.util.DateUtils

class TodoWidgetFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private val filter: String = intent.getStringExtra("filter") ?: "today"
    private var items: List<Todo> = emptyList()
    private val db = TodoDatabaseHelper(context)

    override fun onCreate() {}

    override fun onDataSetChanged() {
        items = when (filter) {
            "today" -> db.getTodosForToday()
            "week" -> db.getTodosForThisWeek()
            else -> db.getAllTodos()
        }
    }

    override fun onDestroy() {
        db.close()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        val todo = items[position]
        val rv = RemoteViews(context.packageName, R.layout.widget_list_item)
        rv.setTextViewText(R.id.widgetItemTitle, todo.title)
        rv.setTextViewText(R.id.widgetItemSystem, todo.system)
        if (todo.dueDate != null) {
            rv.setTextViewText(R.id.widgetItemDate, DateUtils.formatDate(todo.dueDate))
            rv.setViewVisibility(R.id.widgetItemDate, android.view.View.VISIBLE)
        } else {
            rv.setViewVisibility(R.id.widgetItemDate, android.view.View.GONE)
        }
        if (todo.priority == "紧急") {
            rv.setTextViewText(R.id.widgetItemPriority, "紧急")
            rv.setViewVisibility(R.id.widgetItemPriority, android.view.View.VISIBLE)
        } else {
            rv.setViewVisibility(R.id.widgetItemPriority, android.view.View.GONE)
        }
        if (todo.done) {
            rv.setInt(R.id.widgetItemTitle, "setPaintFlags",
                android.graphics.Paint.STRIKE_THRU_TEXT_FLAG or android.graphics.Paint.ANTI_ALIAS_FLAG)
            rv.setInt(R.id.widgetItemTitle, "setTextColor",
                0xFF9CA3AF.toInt())
        } else {
            rv.setInt(R.id.widgetItemTitle, "setTextColor",
                0xFF1F2937.toInt())
        }
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = items[position].id
    override fun hasStableIds(): Boolean = true
}
