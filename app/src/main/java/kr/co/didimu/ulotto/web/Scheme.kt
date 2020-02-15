package kr.co.didimu.ulotto.web

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Message
import androidx.core.app.ActivityCompat.startActivityForResult
import kr.co.didimu.ulotto.LoginKakaoActivity
import kr.co.didimu.ulotto.ui.login.LoginActivity
import kr.co.didimu.ulotto.ui.settings.SettingsActivity
import kr.co.didimu.ulotto.util.Constants
import kr.co.didimu.ulotto.util.MessageHandler
import kr.co.didimu.ulotto.util.PrintLog


enum class Scheme(private val host: String) {
    LOGIN("login"),
    LOGOUT("logout"),
    SETTINGS("settings"),
    MOVEURL("moveUrl"),
    CLOSE_FIND_WINDOW("closeFindview"),
    EXTERNALBROWSER("externalBrowser"),
    USE_AUDIO("useAudio"); //psg 20191014 :STT

    fun valueOf(): String {
        return this.host
    }

    companion object {
        val TAG by lazy { Scheme::class.java.simpleName }

        fun resolveUrl(context: Context?, uri: Uri): Boolean {
            if (!(uri.scheme?.equals(Constants.SCHEME_HB_APP, ignoreCase = true)
                    ?: return false)
            ) {
                return false
            }

            val host = uri.host ?: return false

            when {
                host.equals(LOGIN.host, ignoreCase = true) -> {
                    PrintLog.d(TAG, "scheme login url="+uri)
                    context ?: return false

                    val typeParam = uri.getQueryParameter("type")
                    if(typeParam.equals("kakao", ignoreCase = true)){
                        PrintLog.d(TAG, "scheme login kakao")
//                        val intT = Intent(this, LoginKakaoActivity::class.java)
//                        intT.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                        startActivityForResult(intT, 1111)

                        Intent().setClass(context, LoginKakaoActivity::class.java).also {
                            startActivityForResult(
                                context as Activity,
                                it,
                                Constants.REQUEST_KAKAO_NOUI_LOGIN,
                                null
                            )
                        }

                        return true
                    }else if(typeParam.equals("naver", ignoreCase = true)){
                        PrintLog.d(TAG, "scheme login naver")
                        return true
                    }
                    else {

                    }

                    Intent().setClass(context, LoginActivity::class.java).also {
                        startActivityForResult(
                            context as Activity,
                            it,
                            Constants.REQUEST_LOGIN,
                            null
                        )
                    }
                    return true
                }
                //20191002 psg close web and  login : closefindwindow
                host.equals(CLOSE_FIND_WINDOW.host, ignoreCase = true) -> {
                    PrintLog.d(TAG, "scheme closeFindview")
                    context ?: return false

                    Intent().setClass(context, LoginActivity::class.java).also {
                        startActivityForResult(
                            context as Activity,
                            it,
                            Constants.REQUEST_LOGIN,
                            null
                        )
                    }
                    return true
                }
                host.equals(LOGOUT.host, ignoreCase = true) -> {
                    PrintLog.d(TAG, "scheme logout")
                    val msg = Message.obtain()
                    msg?.what = Constants.MSG_LOGOUT

                    MessageHandler(context, context as Handler.Callback).sendMessage(msg)
                    return true
                }
                host.equals(SETTINGS.host, ignoreCase = true) -> {
                    PrintLog.d(TAG, "scheme settings")
                    context?.startActivity(Intent().apply {
                        setClass(
                            context,
                            SettingsActivity::class.java
                        )
                    })
                    return true
                }
                host.equals(MOVEURL.host, ignoreCase = true) -> {
                    PrintLog.d(TAG, "scheme moveurl")
                    val moveUrl = uri.getQueryParameter("url")
                    val msg = Message.obtain()
                    msg?.what = Constants.MSG_MOVE_URL
                    msg?.obj = moveUrl

                    MessageHandler(context, context as Handler.Callback).sendMessage(msg)
                    return true
                }
                host.equals(EXTERNALBROWSER.host, ignoreCase = true) -> {
                    PrintLog.d(TAG, "scheme externalBrowser")
                    val moveUrl = uri.getQueryParameter("url")
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(moveUrl))
                    context?.startActivity(browserIntent)
                    return true
                }

                host.equals(USE_AUDIO.host, ignoreCase = true) -> {//psg 20191014 :STT
                    PrintLog.d(TAG, "scheme useAudio")
                    val msg =  Message.obtain()
                    msg?.what = Constants.MSG_USE_AUDIO

                    MessageHandler(context, context as Handler.Callback).sendMessage(msg)
                    return true
                }

                else -> {
                    PrintLog.d(TAG, "not should override scheme")
                    return false
                }

            }
        }
    }
}