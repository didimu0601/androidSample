package kr.co.didimu.ulotto.data.dto

import com.google.gson.JsonObject

data class LoginRequest(val userId: String, val password: String, val osVersion : String , val appVersion : String) {
    fun toJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("userid", userId)
        json.addProperty("passwd", password)
        json.addProperty("os", "android")
        json.addProperty("osversion", osVersion)
        json.addProperty("appversion", appVersion)

        return json
    }
}