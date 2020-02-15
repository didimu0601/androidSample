package kr.co.didimu.ulotto.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import androidx.core.app.ActivityCompat
import java.util.*


class Utils {
    companion object {
        private val TAG by lazy {  Utils::class.java.simpleName }

        fun getUserAgent(context: Context): String {
            val SLASH = "/"
            val SPACE = " "

            val userAgent = StringBuilder()
            userAgent.append(SPACE).append(Constants.appVer).append(SLASH).append(getAppVersion(context))
                .append(SPACE).append(Constants.osType).append(SLASH).append(Constants.osTypeA)
                .append(SPACE).append(Constants.osVer).append(SLASH).append(Build.VERSION.RELEASE)
                .append(SPACE).append(Constants.deviceModel).append(SLASH).append(Build.MODEL.replace(' ', '_'))
                .append(SPACE).append(Constants.deviceId).append(SLASH).append(getAndroidId(context))

            return userAgent.toString()
        }

        fun getAndroidId(context: Context): String {
            val androidId = "" + android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )

            PrintLog.d(TAG, "getAndroidId() : $androidId")
            return androidId
        }

        fun getUUID(): String {
            var uniqueID = UUID.randomUUID().toString()
            return uniqueID
        }

        fun finishApplication(activity: Activity, isProcessKill: Boolean = false) {
            ActivityCompat.finishAffinity(activity)

            if(isProcessKill) {
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }

        fun initCookie() {
            CookieManager.getInstance().removeAllCookies(null)
            flushCookies()
        }

        fun deleteCookie() {
            val cookies = SharedPref.getInstance().getStringSet(SharedPref.PREF_COOKIES)?: return

            for (cookie in cookies)  {
                if (cookie.contains("JSESSIONID")) {
                    val regex = "JSESSIONID=([^;]*)".toRegex()

                    CookieManager.getInstance()
                        .setCookie(ServerType.webUrl, regex.replace(cookie, "JSESSIONID= "))
                } else if (cookie.contains("SPRING_SECURITY_REMEMBER_ME_COOKIE")) {
                    val regex = "SPRING_SECURITY_REMEMBER_ME_COOKIE=([^;]*)".toRegex()

                    CookieManager.getInstance()
                        .setCookie(ServerType.webUrl, regex.replace(cookie, "SPRING_SECURITY_REMEMBER_ME_COOKIE= "))
                }
            }
//            val cookieString = "SPRING_SECURITY_REMEMBER_ME_COOKIE=''"
//            CookieManager.getInstance().setCookie(ServerType.webUrl, cookieString)

            flushCookies()
        }

        fun flushCookies() {
            CookieManager.getInstance().flush()
        }

        fun getAppVersion(context: Context): String {
            var version = ""
            try {
                val i = context.packageManager.getPackageInfo(context.packageName, 0)
                version = i.versionName
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return version
        }

        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus

            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun pixelFromDp(context: Context?, dp: Float): Int { //khm To-Do
            context ?: return 0
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
        }
    }
}
