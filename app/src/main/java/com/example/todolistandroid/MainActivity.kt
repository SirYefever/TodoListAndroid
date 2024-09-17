package com.example.todolistandroid

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : ComponentActivity() {

    var todoMap = mutableMapOf<String, Todo>()
    var todoCounter: Int = 0
    var todoIterator: Int = 0
    var doneCounter: Int = 0

    @Serializable
    class Todo(var todoName: String, var DONE: Boolean, var todoId: String, var Number: Int) {

    }

    fun addTodoManually(): String {
        val name = "Todo №" + (todoIterator + 1).toString()
        val id = "todo" + todoIterator.toString()
        addTodo(name, false, id)
        return id
    }

    fun addTodo(name: String, done: Boolean, id: String){
        todoIterator++
        todoCounter++
        var todo = Todo(name, done, id, todoIterator)
        todoMap[id] = todo
    }

    fun showInputDialog(id: String) {
        // Inflate the dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val editTextInput = dialogView.findViewById<EditText>(R.id.alertTextEdit)

        // Build the dialog
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("User Input")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val userInput = editTextInput.text.toString()
                // Change appropriate task properties
                todoMap[id]!!.todoName = userInput
                // Take textView by id that is now templated and change its text property with setText(?)
                var parentNode: LinearLayout = findViewById(R.id.mainContainer)
                var childNode: TextView = parentNode.findViewWithTag<TextView>("todoNameView" + todoMap[id]!!.Number.toString())
                childNode.setText(userInput)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        // Show the dialog
        dialogBuilder.create().show()
        println(todoMap.toString())
    }

    fun constructTodoView(id: String): LinearLayout{
        val todoScrollView = LinearLayout(this)
//        var linearLayoutId = View.generateViewId()
        val linearLayoutId = "todoView" + todoIterator.toString()
//        todoScrollView.id = linearLayoutId
        todoScrollView.setTag(linearLayoutId)
        todoScrollView.orientation = LinearLayout.HORIZONTAL

        val todoNameView = TextView(this)
        todoNameView.text = todoMap[id]!!.todoName
        todoNameView.tag = "todoNameView" + todoMap[id]!!.Number.toString()

        val redactButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "✏\uFE0F"
        }
        redactButton.setOnClickListener {
            // Change related task message to the new one that you will get from user
            // Create alertDialog
            showInputDialog(id)
        }


        val deleteButton = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "\uD83D\uDDD1\uFE0F"
        }
        deleteButton.setOnClickListener {
            var parentNode: LinearLayout = findViewById(R.id.mainContainer)
            var childNode: LinearLayout = parentNode.findViewWithTag<LinearLayout>(linearLayoutId)
            parentNode.removeView(childNode)
            todoMap.remove(id)
        }

        todoScrollView.addView(todoNameView)
        todoScrollView.addView(redactButton)
        todoScrollView.addView(deleteButton)
        return todoScrollView
    }

    fun exportAsJson() {
        val gson = Gson()
        var file = File(this.filesDir.toString(), "Todo_List")
        file.writeText(gson.toJson(todoMap.values))

    }

    fun importAsJson() {
        val MainScrollView: LinearLayout = findViewById<LinearLayout>(R.id.mainContainer)
        val gson = Gson()
        val filePath = this.getFilesDir().getPath() + "/Todo_List"
        val fileContent = File(filePath).readText()
        val todoListType = object: TypeToken<List<Todo>>() {}.type
        val todoList: List<Todo> = gson.fromJson(fileContent, todoListType)
        for (todo in todoList) {
            addTodo(todo.todoName, todo.DONE, todo.todoId)
            MainScrollView.addView(constructTodoView(todo.todoId))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
        }
        setContentView(R.layout.activity_main)
        val AddTodoButton: Button = findViewById<Button>(R.id.buttonAdd)
        val MainScrollView: LinearLayout = findViewById<LinearLayout>(R.id.mainContainer)
        AddTodoButton.setOnClickListener {
            val id = addTodoManually()
            MainScrollView.addView(constructTodoView(id))
        }
        val ExportButton: Button = findViewById<Button>(R.id.buttonExport)
        ExportButton.setOnClickListener {
            exportAsJson()
        }
        val ImportButton: Button = findViewById<Button>(R.id.buttonImport)
        ImportButton.setOnClickListener {
            importAsJson()
        }
    }
}
