package com.example.healthyVibe.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Storage(private val context: Context) {

    private val prefs = context.getSharedPreferences(Keys.PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadHabits(): MutableList<Habit> {
        val json = prefs.getString(Keys.HABITS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Habit>>(){}.type
        return gson.fromJson(json, type)
    }

    fun saveHabits(list: List<Habit>) {
        prefs.edit().putString(Keys.HABITS, gson.toJson(list)).apply()
    }

    fun loadMoods(): MutableList<MoodEntry> {
        val json = prefs.getString(Keys.MOODS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<MoodEntry>>(){}.type
        return gson.fromJson(json, type)
    }

    fun saveMoods(list: List<MoodEntry>) {
        prefs.edit().putString(Keys.MOODS, gson.toJson(list)).apply()
    }

    fun setHydrationMinutes(min: Int) {
        prefs.edit().putInt(Keys.HYDRATION_MINUTES, min).apply()
    }

    fun getHydrationMinutes(): Int = prefs.getInt(Keys.HYDRATION_MINUTES, 60)

    fun getProfileName(): String = prefs.getString(Keys.PROFILE_NAME, "") ?: ""
    fun setProfileName(name: String) { prefs.edit().putString(Keys.PROFILE_NAME, name).apply() }

    fun getProfileBio(): String = prefs.getString(Keys.PROFILE_BIO, "") ?: ""
    fun setProfileBio(bio: String) { prefs.edit().putString(Keys.PROFILE_BIO, bio).apply() }

}
