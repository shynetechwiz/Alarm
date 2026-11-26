@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.studentalarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AlarmApp(store: AlarmStore, refreshKey: Int) {
    val context = LocalContext.current
    var alarms by remember(refreshKey) { mutableStateOf(store.load()) }
    var editing by remember { mutableStateOf<StudentAlarm?>(null) }

    fun commit(list: List<StudentAlarm>) {
        alarms = list.sortedWith(compareBy<StudentAlarm>({ it.hour }, { it.minute }))
        store.saveAll(alarms)
    }

    fun save(alarm: StudentAlarm) {
        commit(alarms.filter { it.id != alarm.id } + alarm)
        AlarmScheduler.schedule(context, alarm)
    }

    fun delete(alarm: StudentAlarm) {
        AlarmScheduler.cancel(context, alarm)
        commit(alarms.filter { it.id != alarm.id })
    }

    fun addRoutine() {
        val weekdays = setOf(2, 3, 4, 5, 6)
        listOf(
            StudentAlarm(label = "Wake up", category = AlarmCategory.WAKE_UP, hour = 6, minute = 30, weekdays = weekdays),
            StudentAlarm(label = "Leave for class", category = AlarmCategory.CLASS, hour = 7, minute = 45, weekdays = weekdays),
            StudentAlarm(label = "Homework time", category = AlarmCategory.HOMEWORK, hour = 17, minute = 0, weekdays = weekdays),
            StudentAlarm(label = "Study session", category = AlarmCategory.STUDY, hour = 19, minute = 0, weekdays = weekdays)
        ).forEach { save(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Student Alarms") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = StudentAlarm() }) {
                Icon(Icons.Default.Add, contentDescription = "Add alarm")
            }
        }
    ) { padding ->
        if (alarms.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("⏰", fontSize = 56.sp)
                Spacer(Modifier.size(12.dp))
                Text("No alarms yet", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.size(8.dp))
                Text(
                    "Add your own, or start with a school-day routine.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(16.dp))
                Button(onClick = { addRoutine() }) { Text("Add school-day routine") }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onClick = { editing = alarm },
                        onToggle = { save(alarm.copy(enabled = it)) },
                        onDelete = { delete(alarm) }
                    )
                }
            }
        }
    }

    editing?.let { current ->
        EditDialog(
            initial = current,
            onDismiss = { editing = null },
            onSave = { updated ->
                save(updated)
                editing = null
            }
        )
    }
}

@Composable
private fun AlarmCard(
    alarm: StudentAlarm,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(alarm.category.emoji, fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).alpha(if (alarm.enabled) 1f else 0.5f)) {
                Text(
                    alarm.timeString,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(alarm.label.ifBlank { alarm.category.title })
                Text(
                    alarm.repeatSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = alarm.enabled, onCheckedChange = onToggle)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete alarm")
            }
        }
    }
}

@Composable
private fun EditDialog(
    initial: StudentAlarm,
    onDismiss: () -> Unit,
    onSave: (StudentAlarm) -> Unit
) {
    val timeState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = false
    )
    var label by remember { mutableStateOf(initial.label) }
    var category by remember { mutableStateOf(initial.category) }
    var days by remember { mutableStateOf(initial.weekdays) }
    val dayLetters = listOf("S", "M", "T", "W", "T", "F", "S")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timeState)

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (e.g. Maths homework)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AlarmCategory.values().forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text("${cat.emoji} ${cat.title}") }
                        )
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (day in 1..7) {
                        val selected = day in days
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { days = if (selected) days - day else days + day },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                dayLetters[day - 1],
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { days = setOf(2, 3, 4, 5, 6) }) { Text("Weekdays") }
                    TextButton(onClick = { days = setOf(1, 7) }) { Text("Weekends") }
                    TextButton(onClick = { days = emptySet() }) { Text("Once") }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(
                            initial.copy(
                                label = label.trim(),
                                category = category,
                                hour = timeState.hour,
                                minute = timeState.minute,
                                weekdays = days,
                                enabled = true
                            )
                        )
                    }) { Text("Save") }
                }
            }
        }
    }
}
