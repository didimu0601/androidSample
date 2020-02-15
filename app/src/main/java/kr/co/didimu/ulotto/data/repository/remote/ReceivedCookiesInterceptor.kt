package kr.co.didimu.ulotto.data.repository.remote

import android.webkit.CookieManager
import kr.co.didimu.ulotto.util.PrintLog
import kr.co.didimu.ulotto.util.ServerType
import kr.co.didimu.ulotto.util.SharedPref
import kr.co.didimu.ulotto.util.Utils
import okhttp3.Interceptor
import okhttp3.Response
import java.util.*


class ReceivedCookiesInterceptor : Interceptor {
    private val TAG by lazy {  ReceivedCookiesInterceptor::class.java.simpleName }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())

        if (originalResponse.headers("Set-Cookie").isNotEmpty()) {
            val cookies = HashSet<String>()

            for (header in originalResponse.headers("Set-Cookie")) {
                cookies.add(header)
                CookieManager.getInstance().setCookie(ServerType.webUrl, header)
                PrintLog.d(TAG, "ServerType.webUrl ${ServerType.webUrl} header $header")
            }

            Utils.flushCookies()
            PrintLog.d(TAG, "cookies $cookies")
            SharedPref.getInstance().setStringSet(SharedPref.PREF_COOKIES, cookies)
        }

        return originalResponse
    }

}