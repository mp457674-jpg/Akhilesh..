package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timeStr: String,
    val dateStr: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val category: String = "Task", // Schedule, Call, Task, Health, Work
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val isCompleted: Boolean = false
)
