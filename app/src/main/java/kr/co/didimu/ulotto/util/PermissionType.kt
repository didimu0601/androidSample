package kr.co.didimu.ulotto.util

import android.Manifest


// 20191014 psg STT
enum class PermissionType(val code: Int) {
    AUDIO(Constants.PERMISSION_AUDIO) {
        override fun getPermissionList(): Array<String> {
            return arrayOf(Manifest.permission.RECORD_AUDIO)
        }
    };

    //    var lateinit permissons: List<String>
    abstract fun getPermissionList(): Array<String>
}