package com.example.todolistandroid

class TodoModel {
    constructor(Name: String, IsComplete: Boolean) {
        todoName = Name
        isComplete = IsComplete
    }
    var todoName: String?
    var isComplete: Boolean
}