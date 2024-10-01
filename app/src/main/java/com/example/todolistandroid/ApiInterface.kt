import com.example.todolistandroid.Todo
import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {
    @GET("GetTodoList")
    fun getExampleData(): Call<List<Todo>>
}