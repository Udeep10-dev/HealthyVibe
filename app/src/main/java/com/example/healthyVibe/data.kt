package com.example.healthyVibe.data

data class Habit(
    var name: String,
    var timesPerDay: Int,
    var done: Int = 0,
    var description: String = "",
    var timeHHmm: String? = null   // "HH:mm" or null if not set
)


data class MoodEntry(
    val emoji: String,
    val timestamp: Long,
    var description: String? = null
)

object Keys {
    const val PREFS = "wellness_prefs"
    const val HABITS = "habits_json"
    const val MOODS = "moods_json"
    const val HYDRATION_MINUTES = "hydration_minutes"
    const val PROFILE_NAME = "profile_name"
    const val PROFILE_BIO = "profile_bio"
}

