package com.example.studentalarm

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class AlarmCategory(val title: String, val emoji: String) {
    WAKE_UP("Wake Up", "🌅"),
    CLASS("Class", "🏫"),
    STUDY("Study Session", "📚"),
    HOMEWORK("Homework", "✏️"),
    EXAM("Exam", "🎓"),
    OTHER("Other", "🔔")
}

data class StudentAlarm(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val category: AlarmCategory = AlarmCategory.WAKE_UP,
    val hour: Int = 7,
    val minute: Int = 0,
    /** Calendar weekdays: 1 = Sunday ... 7 = Saturday. Empty means one-time. */
    val weekdays: Set<Int> = emptySet(),
    val enabled: Boolean = true
) {
    val timeString: String
        get() {
            val h12 = if (hour % 12 == 0) 12 else hour % 12
            val ampm = if (hour < 12) "AM" else "PM"
            return "%d:%02d %s".format(h12, minute, ampm)
        }

    val repeatSummary: String
        get() {
            val names = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            return when {
                weekdays.isEmpty() -> "Once"
                weekdays.size == 7 -> "Every day"
                weekdays == setOf(2, 3, 4, 5, 6) -> "Weekdays"
                weekdays == setOf(1, 7) -> "Weekends"
                else -> weekdays.sorted().joinToString(" ") { names[it - 1] }
            }
        }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("label", label)
        put("category", category.name)
        put("hour", hour)
        put("minute", minute)
        put("weekdays", JSONArray(weekdays.sorted()))
        put("enabled", enabled)
    }

    companion object {
        fun fromJson(o: JSONObject): StudentAlarm {
            val days = o.optJSONArray("weekdays") ?: JSONArray()
            return StudentAlarm(
                id = o.getString("id"),
                label = o.optString("label", ""),
                category = runCatching { AlarmCategory.valueOf(o.getString("category")) }
                    .getOrDefault(AlarmCategory.OTHER),
                hour = o.getInt("hour"),
                minute = o.getInt("minute"),
                weekdays = (0 until days.length()).map { days.getInt(it) }.toSet(),
                enabled = o.optBoolean("enabled", true)
            )
        }
    }
}
