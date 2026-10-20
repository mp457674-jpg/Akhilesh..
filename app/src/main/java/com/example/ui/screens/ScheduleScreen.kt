package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Reminder
import com.example.ui.components.CyberCard
import com.example.ui.theme.CardNavy
import com.example.ui.theme.CardNavyBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleScreen(
    reminders: List<Reminder>,
    onToggleReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    onAddReminder: (title: String, time: String, date: String, category: String, priority: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CHRONO LOG & AGENDAS",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberCyan
                )
                Text(
                    text = "${reminders.count { !it.isCompleted }} PENDING • ${reminders.count { it.isCompleted }} ARCHIVED",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan.copy(alpha = 0.2f),
                    contentColor = CyberCyan
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.border(1.dp, CyberCyan, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("NEW EVENT", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Empty",
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NO SCHEDULED DIRECTIVES",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tell Jarvis \"Remind me to call Rahul at 5 PM\" or tap NEW EVENT to schedule.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reminders, key = { it.id }) { item ->
                    ReminderCard(
                        reminder = item,
                        onToggle = { onToggleReminder(item) },
                        onDelete = { onDeleteReminder(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, time, date, category, priority ->
                onAddReminder(title, time, date, category, priority)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (reminder.isCompleted) CardNavyBorder else when (reminder.priority) {
            "HIGH" -> WarningRed.copy(alpha = 0.6f)
            "MEDIUM" -> NeonAmber.copy(alpha = 0.5f)
            else -> CyberCyan.copy(alpha = 0.4f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = "Toggle completion",
                        tint = if (reminder.isCompleted) MatrixGreen else CyberCyan
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (reminder.isCompleted) TextMuted else TextPrimary,
                        modifier = if (reminder.isCompleted) Modifier.then(
                            Modifier.padding(end = 4.dp)
                        ) else Modifier
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${reminder.dateStr} • ${reminder.timeStr}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CardNavyBorder)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = reminder.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("05:00 PM") }
    var date by remember {
        mutableStateOf(SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date()))
    }
    var category by remember { mutableStateOf("Schedule") }
    var priority by remember { mutableStateOf("MEDIUM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "PROGRAM NEW DIRECTIVE",
                style = MaterialTheme.typography.titleMedium,
                color = CyberCyan
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Directive / Task Title", color = TextMuted) },
                    placeholder = { Text("e.g. Call Client or Take Vitamins", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CardNavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CardNavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CardNavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, time, date, category, priority)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = DeepSpace
                )
            ) {
                Text("SAVE", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextMuted)
            }
        },
        containerColor = CardNavy,
        shape = RoundedCornerShape(12.dp)
    )
}
