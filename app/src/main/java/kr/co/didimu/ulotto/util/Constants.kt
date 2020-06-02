package kr.co.didimu.ulotto.util

import kr.co.didimu.ulotto.BuildConfig

interface Constants {
    companion object {
        val IS_DEBUG_MODE = BuildConfig.DEBUG

        const val INTRO_DELAY_TIME = 3000L
        const val MSG_SPLASH_FINISHED = 0
        const val MSG_LOGOUT = 1
        const val MSG_MOVE_URL = 2
        const val MSG_USE_AUDIO = 3
        const val MSG_PAGELOAD_FINISHED = 4
        const val MSG_PAGELOAD_ERROR = 5
        const val KAKAO_NOUI_LOGIN = 6 //psg 20200219
        const val NAVER_NOUI_LOGIN = 7 //psg 20200219
        const val SCHEME_HB_APP = "ddulotto"
        const val RELSERVER = 0
        const val DEVSERVER = 1
        const val QASERVER = 2
        const val SSLRELSERVER = 3
        const val SSLDEVSERVER = 4
        const val SSLQASERVER = 5
        const val NO_ERRORCODE = -1
        const val HELP_PARTIAL = 1
        const val HELP_INSUFFICIENT = 2
        const val HELP_DIRTY = 3
        const val HELP_SLOW = 4
        const val HELP_FAST = 5
        const val IS_USE_BIOMETRIC = false
        const val REQUEST_LOGIN = 0
        const val REQUEST_USE_AUDIO = 1 //psg 20191014 :STT
        const val REQUEST_KAKAO_NOUI_LOGIN = 2 //psg 20200227
        const val REQUEST_SELECT_FILE = 3
        const val FILECHOOSER_RESULTCODE = 4

        const val PROMPT_ERROR_TEMPOLARYLOCK = 7
        const val PROMPT_ERROR_PERMANENTLOCK = 9
        const val POPUP_URL = "POPUP_URL"

        //khm useragent info
        const val appVer = "appVer"
        const val osType = "osType" //20:Android, 10:IOS
        const val osTypeA = "20"
        const val osVer = "osVer"
        const val deviceModel = "deviceModel"
        const val deviceId = "deviceId"

        const val PERMISSION_AUDIO = 1 //psg 20191014 :STT
        const val HYBRID_SITE_ID = "HYBRIDTEST" //psg

        fun getDownloadUrl(): String {
            return "ddulotto://externalBrowser?url=${ServerType.webUrl}setupDownload"
        }

        fun getFindIdUrl(): String {
            return "${ServerType.webUrl}findId"
        }

        fun getFindPwUrl(): String {
            return "${ServerType.webUrl}findPw"
        }

        fun getLoginScheme(): String {
            return "${Constants.SCHEME_HB_APP}://login"
        }
    }
}
