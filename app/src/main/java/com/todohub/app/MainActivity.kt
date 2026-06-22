package com.todohub.app

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.todohub.app.adapter.TodoAdapter
import com.todohub.app.db.TodoDatabaseHelper
import com.todohub.app.model.Todo
import com.todohub.app.widget.TodayWidgetProvider
import com.todohub.app.widget.WeekWidgetProvider

class MainActivity : AppCompatActivity() {

    private lateinit var db: TodoDatabaseHelper
    private lateinit var adapter: TodoAdapter
    private var currentFilter = "today"
    private lateinit var tabToday: MaterialButton
    private lateinit var tabWeek: MaterialButton
    private lateinit var tabSystems: MaterialButton
    private lateinit var inputTodo: EditText
    private lateinit var spinnerSystem: Spinner
    private lateinit var spinnerPriority: Spinner
    private lateinit var btnAdd: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var textEmpty: TextView
    private lateinit var textStats: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = TodoDatabaseHelper(this)
        tabToday = findViewById(R.id.tabToday)
        tabWeek = findViewById(R.id.tabWeek)
        tabSystems = findViewById(R.id.tabSystems)
        inputTodo = findViewById(R.id.inputTodo)
        spinnerSystem = findViewById(R.id.spinnerSystem)
        spinnerPriority = findViewById(R.id.spinnerPriority)
        btnAdd = findViewById(R.id.btnAdd)
        recyclerView = findViewById(R.id.recyclerView)
        textEmpty = findViewById(R.id.textEmpty)
        textStats = findViewById(R.id.textStats)
        val systemAdapter = ArrayAdapter.createFromResource(
            this, R.array.systems, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerSystem.adapter = systemAdapter
        adapter = TodoAdapter(
            items = emptyList(),
            onToggle = { todo ->
                db.updateTodoDone(todo.id, !todo.done)
                refreshList()
                updateWidgets()
            },
            onDelete = { todo ->
                db.deleteTodo(todo.id)
                refreshList()
                updateWidgets()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        tabToday.setOnClickListener { switchTab("today") }
        tabWeek.setOnClickListener { switchTab("week") }
        tabSystems.setOnClickListener { switchTab("systems") }
        btnAdd.setOnClickListener { addTodo() }
        switchTab("today")
    }

    private fun switchTab(filter: String) {
        currentFilter = filter
        tabToday.isChecked = filter == "today"
        tabWeek.isChecked = filter == "week"
        tabSystems.isChecked = filter == "systems"
        refreshList()
    }

    private fun addTodo() {
        val title = inputTodo.text.toString().trim()
        if (title.isEmpty()) return
        val system = spinnerSystem.selectedItem.toString()
        val priority = spinnerPriority.selectedItem.toString()
        val dueDate = System.currentTimeMillis() + 23 * 3600 * 1000
        val todo = Todo(
            title = title, system = system, priority = priority,
            createdAt = System.currentTimeMillis(), dueDate = dueDate
        )
        db.insertTodo(todo)
        inputTodo.text.clear()
        refreshList()
        updateWidgets()
    }

    private fun refreshList() {
        val todos = when (currentFilter) {
            "today" -> db.getTodosForToday()
            "week" -> db.getTodosForThisWeek()
            else -> db.getAllTodos()
        }
        adapter.updateItems(todos)
        if (todos.isEmpty()) {
            textEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            textEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
        val total = db.getAllTodos().size
        val done = db.getAllTodos().count { it.done }
        textStats.text = "总计 $total  已完成 $done  待完成 ${total - done}"
    }

    private fun updateWidgets() {
        val mgr = AppWidgetManager.getInstance(this)
        val todayComp = ComponentName(this, TodayWidgetProvider::class.java)
        val weekComp = ComponentName(this, WeekWidgetProvider::class.java)
        val todayIds = mgr.getAppWidgetIds(todayComp)
        val weekIds = mgr.getAppWidgetIds(weekComp)
        if (todayIds.isNotEmpty()) TodayWidgetProvider().onUpdate(this, mgr, todayIds)
        if (weekIds.isNotEmpty()) WeekWidgetProvider().onUpdate(this, mgr, weekIds)
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}
