package com.example.todolistandroid

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.sql.Time

@Serializable
data class Todo (
    @SerializedName("id") var Id: Long,
    @SerializedName("name") var Name: String? = null,
    @SerializedName("isComplete") var IsComplete: Boolean = false
)