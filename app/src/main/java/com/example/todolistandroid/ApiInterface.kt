import com.example.todolistandroid.Todo
import com.example.todolistandroid.TodoModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {
    @GET("GetTodoList")
    fun getTodoList(): Call<List<Todo>>
    @GET("GetTodo/{id}")
    fun getTodo(@Path("id") id: Long): Call<Todo>
    @GET("GetTodoCounter")
    fun getTodoCounter(): Call<Int>
    @PUT("ChangeTodoName/{id}")
    fun redactTodoItem(@Path("id") id: Long, @Query("newName") string: String): Call<String> // What should it return??? maybe okhttp3.Call?
    @PUT("ChangeCompleteStatus/{id}")
    fun changeCompleteStatus(@Path("id") id: Long): Call<Todo>
    @POST("AddTodo")
    fun addTodo(@Body todoModel: TodoModel): Call<Todo>
    @PUT("LoadTodoList")
    fun loadTodoList(): Call<List<Todo>>
    @DELETE("DeleteTodo/{id}")
    fun deleteTodo(@Path("id") id: Long): Call<Todo>
}