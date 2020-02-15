package kr.co.didimu.ulotto.data.dto

data class DeviceResponse(val resultContent : /*String*/DeviceContent) : Base() {
    data class DeviceContent(val regDate: String)
}