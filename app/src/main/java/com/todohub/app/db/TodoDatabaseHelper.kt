package com.todohub.app.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.todohub.app.model.Todo
import com.todohub.app.util.DateUtils
import java.util.Calendar

class TodoDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "todohub.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_TODOS = "todos"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_SYSTEM = "system_name"
        private const val COL_DONE = "done"
        private const val COL_PRIORITY = "priority"
        private const val COL_CREATED_AT = "created_at"
        private const val COL_DUE_DATE = "due_date"
        private const val COL_NOTES = "notes"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE $TABLE_TODOS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_SYSTEM TEXT NOT NULL,
                $COL_DONE INTEGER NOT NULL DEFAULT 0,
                $COL_PRIORITY TEXT NOT NULL DEFAULT '普通',
                $COL_CREATED_AT INTEGER NOT NULL,
                $COL_DUE_DATE INTEGER,
                $COL_NOTES TEXT DEFAULT ''
            )"""
        )
        seedData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TODOS")
        onCreate(db)
    }

    private fun seedData(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()
        val todayStart = DateUtils.todayStart().time
        val tomorrowLong = todayStart + 86400000L
        val day3Long = todayStart + 3 * 86400000L
        val day5Long = todayStart + 5 * 86400000L

        val seed = listOf(
            Todo(title = "确认飞书审批表单流程", system = "飞书", priority = "紧急", createdAt = now, dueDate = todayStart + 23 * 3600 * 1000),
            Todo(title = "同步钉钉日程至团队日历", system = "钉钉", createdAt = now, dueDate = todayStart + 23 * 3600 * 1000),
            Todo(title = "修复 Jira EPIC-342 回归缺陷", system = "Jira", priority = "紧急", createdAt = now, dueDate = tomorrowLong),
            Todo(title = "Review GitHub PR #892 代码", system = "GitHub Issues", createdAt = now, dueDate = tomorrowLong),
            Todo(title = "企业微信周报数据汇总", system = "企业微信", createdAt = now, dueDate = day3Long),
            Todo(title = "飞书文档撰写 Q3 技术方案", system = "飞书", createdAt = now, dueDate = day3Long),
            Todo(title = "钉钉审批流节点优化方案评审", system = "钉钉", done = true, createdAt = now, dueDate = day5Long),
            Todo(title = "Jira 看板 Sprint 计划调整", system = "Jira", done = true, createdAt = now, dueDate = day5Long),
        )

        for (t in seed) {
            val cv = ContentValues().apply {
                put(COL_TITLE, t.title)
                put(COL_SYSTEM, t.system)
                put(COL_DONE, if (t.done) 1 else 0)
                put(COL_PRIORITY, t.priority)
                put(COL_CREATED_AT, t.createdAt)
                put(COL_DUE_DATE, t.dueDate)
                put(COL_NOTES, t.notes)
            }
            db.insert(TABLE_TODOS, null, cv)
        }
    }

    fun insertTodo(todo: Todo): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_TITLE, todo.title)
            put(COL_SYSTEM, todo.system)
            put(COL_DONE, if (todo.done) 1 else 0)
            put(COL_PRIORITY, todo.priority)
            put(COL_CREATED_AT, todo.createdAt)
            put(COL_DUE_DATE, todo.dueDate)
            put(COL_NOTES, todo.notes)
        }
        return db.insert(TABLE_TODOS, null, cv)
    }

    fun updateTodoDone(id: Long, done: Boolean) {
        val db = writableDatabase
        val cv = ContentValues().apply { put(COL_DONE, if (done) 1 else 0) }
        db.update(TABLE_TODOS, cv, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun deleteTodo(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_TODOS, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun getAllTodos(): List<Todo> = queryTodos(null, null)
    fun getTodosForToday(): List<Todo> = queryTodos(
        "$COL_DUE_DATE >= ? AND $COL_DUE_DATE < ?",
        arrayOf(DateUtils.todayStart().time.toString(), DateUtils.todayEnd().time.toString())
    )
    fun getTodosForThisWeek(): List<Todo> = queryTodos(
        "$COL_DUE_DATE >= ? AND $COL_DUE_DATE <= ?",
        arrayOf(DateUtils.weekStart().time.toString(), DateUtils.weekEnd().time.toString())
    )

    private fun queryTodos(selection: String?, selectionArgs: Array<String>?): List<Todo> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TODOS, null, selection, selectionArgs,
            null, null, "$COL_DONE ASC, $COL_DUE_DATE ASC, $COL_CREATED_AT DESC"
        )
        val result = mutableListOf<Todo>()
        cursor.use { c ->
            while (c.moveToNext()) {
                result.add(cursorToTodo(c))
            }
        }
        return result
    }

    private fun cursorToTodo(c: Cursor): Todo = Todo(
        id = c.getLong(c.getColumnIndexOrThrow(COL_ID)),
        title = c.getString(c.getColumnIndexOrThrow(COL_TITLE)),
        system = c.getString(c.getColumnIndexOrThrow(COL_SYSTEM)),
        done = c.getInt(c.getColumnIndexOrThrow(COL_DONE)) != 0,
        priority = c.getString(c.getColumnIndexOrThrow(COL_PRIORITY)),
        createdAt = c.getLong(c.getColumnIndexOrThrow(COL_CREATED_AT)),
        dueDate = if (c.isNull(c.getColumnIndexOrThrow(COL_DUE_DATE))) null
                   else c.getLong(c.getColumnIndexOrThrow(COL_DUE_DATE)),
        notes = c.getString(c.getColumnIndexOrThrow(COL_NOTES))
    )
}
