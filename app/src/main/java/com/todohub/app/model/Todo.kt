package com.todohub.app.model

data class Todo(
    val id: Long = 0,
    val title: String,
    val system: String,
    val done: Boolean = false,
    val priority: String = "普通",
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val notes: String = ""
)
