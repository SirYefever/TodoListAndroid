package com.example.todolistandroid

import ApiInterface
import RetrofitInstance
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader


class MainActivity : ComponentActivity() {
    private lateinit var apiInterface: ApiInterface

    var todoMap = mutableMapOf<String, Todo>()
    var todoCounter: Int = 0
    var todoIterator: Int = 0

    private fun getApiInterface() {
        apiInterface = RetrofitInstance.getInstance().create(ApiInterface::class.java)
    }

    private fun getTodoList() {
        val call = apiInterface.getExampleData()
        call.enqueue(object : Callback<List<Todo>> {
            override fun onResponse(call: Call<List<Todo>>, response: Response<List<Todo>>) {
                if (response.isSuccessful && response.body()!=null){
                    response.body()!!.forEach { element ->
                        addTodo(element.todoName, element.Done, element.todoId)
                    }
                }
            }
            override fun onFailure(call: Call<List<Todo>>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun addTodoManually(): String {
        val name = "Todo №" + (todoIterator + 1).toString()
        val id = "todo" + todoIterator.toString()
        addTodo(name, false, id)
        return id
    }

    fun addTodo(name: String, done: Boolean, id: String){
        val blankMessageView = findViewById<TextView>(R.id.blankScreenMessage)
        if (blankMessageView != null) {
            val parent = blankMessageView.parent as ViewGroup
            parent.removeView(blankMessageView)
        }
        todoIterator++
        todoCounter++
        var todo = Todo(name, done, id, todoIterator)
        todoMap[id] = todo
    }

    fun showRedactDialog(id: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val editTextInput = dialogView.findViewById<EditText>(R.id.alertTextEdit)

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Set description")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val userInput = editTextInput.text.toString()
                todoMap[id]!!.todoName = userInput
                val parentNode: LinearLayout = findViewById(R.id.mainContainer)
                val childNode: TextView = parentNode.findViewWithTag<TextView>("todoNameView" + todoMap[id]!!.Number.toString())
                childNode.setText(userInput)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        dialogBuilder.create().show()
    }

    fun constructTodoView(id: String): LinearLayout{
        val todoScrollView = LinearLayout(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.leftMargin = 60

        val linearLayoutId = "todoView" + todoIterator.toString()
        todoScrollView.setTag(linearLayoutId)
        todoScrollView.orientation = LinearLayout.HORIZONTAL

        val checkBox = CheckBox(this)
        if (todoMap[id]!!.Done) {
            checkBox.isChecked = true
        }
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                todoMap[id]!!.Done = !todoMap[id]!!.Done
            } else {
                todoMap[id]!!.Done = !todoMap[id]!!.Done
            }
        }

        val todoNameView = TextView(this)
        todoNameView.textSize = 22F
        todoNameView.text = todoMap[id]!!.todoName
        todoNameView.tag = "todoNameView" + todoMap[id]!!.Number.toString()

        val redactButton = Button(this).apply {
            text = "✏\uFE0F"
        }
        redactButton.setOnClickListener {
            showRedactDialog(id)
        }

        val deleteButton = Button(this).apply {
            text = "\uD83D\uDDD1\uFE0F"
        }
        deleteButton.setOnClickListener {
            var parentNode: LinearLayout = findViewById(R.id.mainContainer)
            var childNode: LinearLayout = parentNode.findViewWithTag<LinearLayout>(linearLayoutId)
            parentNode.removeView(childNode)
            todoMap.remove(id)
        }

        todoScrollView.addView(checkBox, layoutParams)
        todoScrollView.addView(todoNameView)
        todoScrollView.addView(redactButton)
        todoScrollView.addView(deleteButton)

        todoNameView.layoutParams.width = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            160f,
            resources.displayMetrics).toInt()

        return todoScrollView
    }

    fun showExportDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val editTextInput = dialogView.findViewById<EditText>(R.id.alertTextEdit)
        var userInput: String = "Todo_List"

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Enter file name")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                userInput = editTextInput.text.toString()
                exportAsJson(userInput)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        dialogBuilder.create().show()
    }

    fun exportAsJson(fileName: String) {
        val gson = Gson()
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toURI().path, fileName)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        file.writeText(gson.toJson(todoMap.values))
    }

    fun importAsJson(data: InputStream) {
        val MainScrollView: LinearLayout = findViewById<LinearLayout>(R.id.mainContainer)
        todoMap.clear() // Not sure if it's better to remove all previous todos when importing
        MainScrollView.removeAllViews()

        val gson = Gson()

        val todoListType = object: TypeToken<List<Todo>>() {}.type
        val reader = InputStreamReader(data)
        val todoList: List<Todo> = gson.fromJson(reader, todoListType)
        for (todo in todoList) {
            addTodo(todo.todoName, todo.Done, todo.todoId)
            MainScrollView.addView(constructTodoView(todo.todoId))
        }
    }

    fun callImportWindow() {
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123 && resultCode == RESULT_OK) {
            val selectedFile = data?.dataString
            val test1 = Uri.parse(selectedFile)
            contentResolver.openInputStream(test1).use {
                if (it != null) {
                    importAsJson(it)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val AddTodoButton: Button = findViewById<Button>(R.id.buttonAdd)
        val MainScrollView: LinearLayout = findViewById<LinearLayout>(R.id.mainContainer)

        AddTodoButton.setOnClickListener {
            val id = addTodoManually()
            MainScrollView.addView(constructTodoView(id))
        }
        val ExportButton: Button = findViewById<Button>(R.id.buttonExport)
        ExportButton.setOnClickListener {
            showExportDialog()
        }
        val ImportButton: Button = findViewById<Button>(R.id.buttonImport)
        ImportButton.setOnClickListener {
            callImportWindow()
        }
        getApiInterface()
        getTodoList()
    }
}
