package com.example.todolistandroid

import ApiInterface
import RetrofitInstance
import android.app.AlertDialog
import android.os.Bundle
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Console


class MainActivity : ComponentActivity() {
    private lateinit var apiInterface: ApiInterface

    val gson = Gson()
//    var todoMap = mutableMapOf<Long, Todo>()
    var todoList = mutableListOf<Todo>()
    var doneCounter: Int = 0
    var todoIterator: Int = 0
    var handledTodo = Todo(0, null, false)

    private fun getApiInterface() {
        apiInterface = RetrofitInstance.getInstance().create(ApiInterface::class.java)
    }

    private fun onSuccessHandler(todo: Todo) {
        handledTodo = todo
    }

    private fun getTodoById(id: Long) {
        val call = apiInterface.getTodo(id)
        call.enqueue(object : Callback<Todo> {
            override fun onResponse(call: Call<Todo>, response: Response<Todo>) {
                if (response.isSuccessful && response.body()!=null){
                    var result = response.body()
                    if (result != null) {
                        onSuccessHandler(result)
                    }
                }
            }
            override fun onFailure(call: Call<Todo>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun getTodoListFromDb() {
        //TODO clear existing todos
        var parentNode: LinearLayout = findViewById(R.id.mainContainer)
        parentNode.removeAllViews()
        val call = apiInterface.getTodoList()
        call.enqueue(object : Callback<List<Todo>> {
            override fun onResponse(call: Call<List<Todo>>, response: Response<List<Todo>>) {
                if (response.isSuccessful && response.body()!=null){
                    response.body()!!.forEach { element ->
                        addTodoView(element)
                    }
                }
            }
            override fun onFailure(call: Call<List<Todo>>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    fun addTodoManually() {
        val name = "Todo №" + (todoIterator + 1).toString()
        val todoModel = TodoModel(name, false)
        postTodo(todoModel)
        getTodoListFromDb()
    }

    fun postTodo(todoModel: TodoModel) {
        todoIterator++
        val call = apiInterface.addTodo(todoModel)
        call.enqueue(object : Callback<Todo> {
            override fun onResponse(call: Call<Todo>, response: Response<Todo>) {
                if (response.isSuccessful && response.body()!=null){
                    getTodoListFromDb()
                }
            }
            override fun onFailure(call: Call<Todo>, t: Throwable) {
                t.printStackTrace()
            }
        })

    }

    fun manageRedacting(id: Long) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val editTextInput = dialogView.findViewById<EditText>(R.id.alertTextEdit)
        val isCompleteStatus = getTodoById(id)

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Set description")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val userInput = editTextInput.text.toString()
                val todoModel = TodoModel(userInput, handledTodo.IsComplete)
                val call = apiInterface.redactTodoItem(id, todoModel)
                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful && response.body()!=null){
                            //Nothing I guess...
                        }
                    }
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
                val parentNode: LinearLayout = findViewById(R.id.mainContainer)
                val childNode: TextView = parentNode.findViewWithTag<TextView>("todoNameView" + id)
                childNode.setText(userInput)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        dialogBuilder.create().show()
    }

    fun addTodoView(todo: Todo) {
        val blankMessageView = findViewById<TextView>(R.id.blankScreenMessage)
        if (blankMessageView != null) {
            val parent = blankMessageView.parent as ViewGroup
            parent.removeView(blankMessageView)
        }
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
        if (todo.IsComplete) {
            checkBox.isChecked = true
        }
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            val call = apiInterface.changeCompleteStatus(todo.Id)
            call.enqueue(object : Callback<Todo> {
                override fun onResponse(call: Call<Todo>, response: Response<Todo>) {
                    if (response.isSuccessful && response.body()!=null){
                        //TODO
                        getTodoListFromDb()
                    }
                }
                override fun onFailure(call: Call<Todo>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        }

        val todoNameView = TextView(this)
        todoNameView.textSize = 22F
        todoNameView.text = todo.Name
        todoNameView.tag = "todoNameView" + todo.Id

        val redactButton = Button(this).apply {
            text = "✏\uFE0F"
        }
        redactButton.setOnClickListener {
            manageRedacting(todo.Id)
        }

        val deleteButton = Button(this).apply {
            text = "\uD83D\uDDD1\uFE0F"
        }
        deleteButton.setOnClickListener {
            //TODO call the delete query
            val call = apiInterface.deleteTodo(todo.Id)
            call.enqueue(object : Callback<Todo> {
                override fun onResponse(call: Call<Todo>, response: Response<Todo>) {
                    if (response.isSuccessful && response.body()!=null){
                        getTodoListFromDb()
                    }
                }
                override fun onFailure(call: Call<Todo>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        }

        todoScrollView.addView(checkBox, layoutParams)
        todoScrollView.addView(todoNameView)
        todoScrollView.addView(redactButton)
        todoScrollView.addView(deleteButton)

        todoNameView.layoutParams.width = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            160f,
            resources.displayMetrics).toInt()

        val MainScrollView: LinearLayout = findViewById<LinearLayout>(R.id.mainContainer)

        MainScrollView.addView(todoScrollView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val AddTodoButton: Button = findViewById<Button>(R.id.buttonAdd)
        val MainScrollView: LinearLayout = findViewById<LinearLayout>(R.id.mainContainer)

        AddTodoButton.setOnClickListener {
            addTodoManually()
        }
        val RefreshButton: Button = findViewById<Button>(R.id.refreshB)
        RefreshButton.setOnClickListener {
            getTodoListFromDb()
        }
        getApiInterface()
        getTodoListFromDb()
    }
}
