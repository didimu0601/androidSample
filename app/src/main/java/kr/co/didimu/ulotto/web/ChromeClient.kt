package kr.co.didimu.ulotto.web

import android.app.Activity
import android.text.TextUtils
import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import kr.co.didimu.ulotto.ui.dialog.CustomAlertDialog
import kr.co.didimu.ulotto.util.PrintLog

class ChromeClient: WebChromeClient() {
    private val TAG by lazy { ChromeClient::class.java.simpleName }

    override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
        val msg = cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId()
        PrintLog.d(TAG, msg)
        return true
    }

    override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
        PrintLog.d(TAG, "onJsAlert() message = $message, url = $url")

        if (!TextUtils.isEmpty(message)) {
            try {
                if (isActive(view.context as AppCompatActivity)) {
//                    AlertDialog.Builder(view.context)
//                        .setCancelable(false)
//                        .setOnCancelListener { result.cancel() }
//                        .setMessage(message)
//                        .setPositiveButton(android.R.string.ok) { dialog, which -> result.confirm() }
//                        .show()
                    CustomAlertDialog.Builder(view.context).apply {
                        setMsg(message)
                        setCancelable(false) { result.cancel() }
                        setPositiveButton(positiveId = android.R.string.ok) { result.confirm() }
                    }.build()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                result.confirm()
            }

            return true
        }


        return false
    }

    override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
        PrintLog.d(TAG, "onJsConfirm() message = $message, url = $url")
        if (!TextUtils.isEmpty(message)) {
            if (isActive(view.context as AppCompatActivity)) {
                CustomAlertDialog.Builder(view.context). apply {
                    setMsg(message)
                    setPositiveButton(positiveId = android.R.string.ok) { result.confirm() }
                    setNegativeButton(negativeId = android.R.string.cancel) { result.cancel() }
                    setCancelable(false) { result.cancel() }
                }.build()
            }
            return true
        }

        return false
    }

    private fun isActive(activity: Activity): Boolean {
        return !activity.isFinishing && !activity.isDestroyed
    }
}