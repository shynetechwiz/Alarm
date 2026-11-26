package com.example.studentalarm

import android.content.Context
import org.json.JSONArray

/** Saves alarms on the device as JSON in SharedPreferences. */
class AlarmStore(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences("student_alarms", Context.MODE_PRIVATE)

    fun load(): List<StudentAlarm> {
        val raw = prefs.getString("alarms", null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { StudentAlarm.fromJson(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    fun saveAll(list: List<StudentAlarm>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("alarms", arr.toString()).apply()
    }

    fun get(id: String): StudentAlarm? = load().firstOrNull { it.id == id }

    fun upsert(alarm: StudentAlarm) {
        saveAll(load().filter { it.id != alarm.id } + alarm)
    }
}
