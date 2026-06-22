package com.todohub.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.todohub.app.R
import com.todohub.app.model.Todo
import com.todohub.app.util.DateUtils

class TodoAdapter(
    private var items: List<Todo>,
    private val onToggle: (Todo) -> Unit,
    private val onDelete: (Todo) -> Unit
) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkDone)
        val titleText: TextView = view.findViewById(R.id.textTitle)
        val systemTag: TextView = view.findViewById(R.id.textSystem)
        val priorityTag: TextView = view.findViewById(R.id.textPriority)
        val dateText: TextView = view.findViewById(R.id.textDate)
        val dueText: TextView = view.findViewById(R.id.textDue)
        val deleteBtn: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todo = items[position]
        holder.titleText.text = todo.title
        holder.titleText.alpha = if (todo.done) 0.5f else 1f
        if (todo.done) {
            holder.titleText.paintFlags = holder.titleText.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.titleText.paintFlags = holder.titleText.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.systemTag.text = todo.system
        if (todo.priority == "紧急") {
            holder.priorityTag.visibility = View.VISIBLE
            holder.priorityTag.text = "紧急"
        } else {
            holder.priorityTag.visibility = View.GONE
        }

        if (todo.dueDate != null) {
            holder.dateText.visibility = View.VISIBLE
            holder.dateText.text = DateUtils.formatDate(todo.dueDate)
            val dueStr = DateUtils.dueText(todo.dueDate)
            holder.dueText.visibility = View.VISIBLE
            holder.dueText.text = dueStr
        } else {
            holder.dateText.visibility = View.GONE
            holder.dueText.visibility = View.GONE
        }

        holder.checkBox.isChecked = todo.done
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.setOnClickListener { onToggle(todo) }
        holder.deleteBtn.setOnClickListener { onDelete(todo) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Todo>) {
        items = newItems
        notifyDataSetChanged()
    }
}
