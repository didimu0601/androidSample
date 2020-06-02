package kr.co.didimu.ulotto.web

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kr.co.didimu.ulotto.util.*


class WebClient: WebViewClient() {
    private val TAG by lazy {  WebClient::class.java.simpleName }

    private val EXTERNAL_URL_SCHEME = "ddulotto://externalBrowser?url="

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        PrintLog.d(TAG, "onPageStarted $url")
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        PrintLog.d(TAG, "onPageFinished $url")

        if (!SharedPref.getInstance().getBoolean(SharedPref.PREF_IS_FIRST_COMPLETE)) {
            PrintLog.d(TAG, "onPageFinished Send MSG_SPLASH_FINISHED")
            val msg = Message.obtain()
            msg.what = Constants.MSG_SPLASH_FINISHED
            MessageHandler(view?.context, view?.context as Handler.Callback).sendMessage(msg)
        }
        else{
            val msg = Message.obtain()
            msg.what = Constants.MSG_PAGELOAD_FINISHED
            MessageHandler(view?.context, view?.context as Handler.Callback).sendMessage(msg)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val uri: Uri = request?.url ?: return super.shouldOverrideUrlLoading(view, request)
        PrintLog.d(TAG, "shouldOverrideUrlLoading $uri")

        if (uri.scheme.equals("tel", ignoreCase = true)) {
            val intent = Intent(Intent.ACTION_DIAL, uri)
            view?.context?.startActivity(intent)
            return true
        } else if (Scheme.resolveUrl(view?.context, uri)) {
            return true
        }

        return checkExternalURL(view?.context, uri)
    }

    private fun checkExternalURL(context: Context?, uri: Uri): Boolean {
        val host = Uri.parse(ServerType.webUrl).host
        if (host.equals(uri.host, ignoreCase = true)) {
            return false
        }

        val externalUrl = StringBuilder(EXTERNAL_URL_SCHEME)
        externalUrl.append(uri.toString())

        PrintLog.d(TAG, "checkExternalURL $externalUrl")
        return Scheme.resolveUrl(context, Uri.parse(externalUrl.toString()))
    }
}