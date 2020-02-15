package kr.co.didimu.ulotto.data.dto

import com.google.gson.JsonObject

data class DeviceRequest(val deviceId: String, val userId: String, val useYn: String = "Y") {

    fun toJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("udid", deviceId)
        json.addProperty("userId", userId)
        json.addProperty("useYn", useYn)

        return json
    }
}