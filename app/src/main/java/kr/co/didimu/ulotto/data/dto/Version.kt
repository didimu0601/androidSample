package kr.co.didimu.ulotto.data.dto

data class Version(var appUpdateInfo: AppUpdateInfo) : Base() {
    //data class VersionContent(val forceUpdateYn: String, val versionNo: String)
    data class AppUpdateInfo(val android_new: String, val android_fupdate: String)
}