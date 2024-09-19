package com.example.todolistandroid

import kotlinx.serialization.Serializable

@Serializable
class Todo(
    var todoName: String,
    var Done: Boolean,
    var todoId: String,
    var Number: Int) {
}