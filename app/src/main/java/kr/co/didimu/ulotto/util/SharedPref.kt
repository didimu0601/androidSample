package kr.co.didimu.ulotto.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

@Suppress("unused")
class SharedPref(context: Context) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object SharedKey {
        const val PREF_SERVER_MODE = "PREF_SERVER_MODE"
        const val PREF_IS_TERMS_AGREE = "PREF_IS_TERMS_AGREE"
        const val PREF_SECURE_KEY = "PREF_SECURE_KEY"
        const val PREF_AUTO_LOGIN = "PREF_AUTO_LOGIN"
        const val PREF_SAVE_ID = "PREF_SAVE_ID"
        const val PREF_ID = "PREF_ID"
        const val PREF_PW = "PREF_PW"
        const val PREF_COOKIES = "PREF_COOKIES"
        const val PREF_IS_FIRST_COMPLETE = "PREF_IS_FIRST_COMPLETE"
        const val PREF_USE_PROMPT_LOGIN = "PREF_USE_PROMPT_LOGIN"
        const val PREF_IS_SUCCESS_PROMPT_LOGIN = "PREF_AVAILABLE_PROMPT_LOGIN"
        const val PREF_IS_SHOW_UPDATE_BUTTON = "PREF_IS_SHOW_UPDATE_BUTTON"
        //20191022 psg : biologin btn pressed initailly
        const val PREF_BIOMETRICS_DIRECT_PRESS_START ="PREF_BIOMETRICS_DIRECT_PRESS_START"

        @Volatile private lateinit var instance: SharedPref

        fun initialize(context: Context) {
            synchronized(this) {
                SharedPref(context).also { instance = it }
            }
        }

        fun getInstance() = instance
    }

    fun getString(key: String): String {
        return sharedPreferences.getString(key, "") ?: ""
    }

    fun setString(key: String, value: String?) {
        value ?: return

        with (sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun setBoolean(key: String, value: Boolean) {
        with (sharedPreferences.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun setInt(key: String, value: Int) {
        with (sharedPreferences.edit()) {
            putInt(key, value)
            apply()
        }
    }

    fun getStringSet(key: String): Set<String>? {
        return sharedPreferences.getStringSet(key, null)
    }

    fun setStringSet(key: String, value: Set<String>?) {
        with (sharedPreferences.edit()) {
            putStringSet(key, value)
            apply()
        }
    }
}