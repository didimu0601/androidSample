package kr.co.didimu.ulotto.data.repository

import kr.co.didimu.ulotto.api.HttpCallback
import kr.co.didimu.ulotto.data.dto.DeviceRegistration
import kr.co.didimu.ulotto.data.dto.DeviceResponse
import kr.co.didimu.ulotto.data.dto.LoginResponse
import kr.co.didimu.ulotto.data.dto.Version
import kr.co.didimu.ulotto.data.repository.remote.ServiceRemoteData
import com.google.gson.JsonObject
import javax.inject.Inject

//khm remoteService를 자동으로 provide해줌
class ServiceRepository @Inject constructor(private val remoteService: ServiceRemoteData) {

    fun requestVersion(version: String, callback: HttpCallback<Version>.() -> Unit) {
        remoteService.requestVersion(version, callback)
    }

    fun registerPromptDevice(deviceInfo: JsonObject, callback: HttpCallback<DeviceRegistration>.() -> Unit) {
        remoteService.registerPromptDevice(deviceInfo, callback)
    }

    fun confirmPromptDevice(deviceId: String, userId: String, callback: HttpCallback<DeviceResponse>.() -> Unit) {
        remoteService.confirmPromptDevice(deviceId, userId, callback)
    }

    fun doLogin(loginInfo: JsonObject, callback: HttpCallback<LoginResponse>.() -> Unit) {
        remoteService.doLogin(loginInfo, callback)
    }

    // 20191018 psg remove prompt device
    fun removePromptDevice(deviceInfo: JsonObject, callback: HttpCallback<DeviceRegistration>.() -> Unit) {
        remoteService.removePromptDevice(deviceInfo, callback)
    }
}