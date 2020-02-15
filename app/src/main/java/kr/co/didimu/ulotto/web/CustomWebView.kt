package kr.co.didimu.ulotto.web

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.URLUtil.guessFileName
import android.webkit.WebView
import android.widget.Toast
import kr.co.didimu.ulotto.R
import kr.co.didimu.ulotto.ui.dialog.CustomAlertDialog
import kr.co.didimu.ulotto.util.PrintLog
import kr.co.didimu.ulotto.util.Utils


class CustomWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    init {
        PrintLog.d("CustomWebView", "init")
        initSettings()
    }

    @SuppressWarnings("SetJavaScriptEnabled")
    private fun initSettings() {
        settings.javaScriptEnabled = true
//        settings.builtInZoomControls = false
//        settings.useWideViewPort = true
        settings.domStorageEnabled = true
//        settings.pluginState = WebSettings.PluginState.ON // Plug In 허용
        settings.setSupportMultipleWindows(true) // Multiple window ons
        settings.javaScriptCanOpenWindowsAutomatically = true
//        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        var userAgent = settings.getUserAgentString();

        //settings.setUserAgentString(userAgent + " APP_DIDIMU_Android")
        settings.setUserAgentString(userAgent + " DDU-lotto")


        //Enable 3rd party cookie, Disable하면 보안 키패드가 생성 안됨.
        val cookieMgr = CookieManager.getInstance()
        cookieMgr.setAcceptCookie(true)
        cookieMgr.setAcceptThirdPartyCookies(this, true) // false 설정 시 오류 발생

        isHorizontalScrollBarEnabled = true
//        setHorizontalScrollbarOverlay(true)
        isVerticalScrollBarEnabled = true
//        setVerticalScrollbarOverlay(true)
        isScrollbarFadingEnabled = true

        //Image Resizing to fit screen width
        settings.loadWithOverviewMode = true
//        settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        settings.userAgentString = Utils.getUserAgent(context)

        //20191023 psg : 릴리즈 후에도 웹 디버깅 가능하도록 임시로 해
//        if (BuildConfig.DEBUG) {
//            setWebContentsDebuggingEnabled(true)
//        }
        setWebContentsDebuggingEnabled(true)


        setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.setDataAndType(Uri.parse(url), mimeType)

            if (!isExecutableApplication(context, browserIntent)) {
                CustomAlertDialog.Builder(context).apply {
                    setMsg(msgId = R.string.msg_download_file)
                    setPositiveButton(positiveId = android.R.string.ok) {
                        downloadFile(
                            url,
                            userAgent,
                            contentDisposition,
                            mimeType,
                            contentLength
                        )
                    }
                    setNegativeButton(negativeId = android.R.string.cancel) {}
                }.build()
            } else {
                val chooser =
                    Intent.createChooser(browserIntent, context.getString(R.string.app_name))
                chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK // optional
                context.startActivity(chooser)
            }
        }
    }

    private fun downloadFile(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long
    ) {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setMimeType(mimeType)
            //------------------------COOKIE!!------------------------
            val cookies = CookieManager.getInstance().getCookie(url)
            addRequestHeader("cookie", cookies)
            //------------------------COOKIE!!------------------------
            addRequestHeader("User-Agent", userAgent)
            setDescription("Downloading file...")
            setTitle(guessFileName(url, contentDisposition, mimeType))
            allowScanningByMediaScanner()
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(
                context,
                DIRECTORY_DOWNLOADS,
                guessFileName(url, contentDisposition, mimeType)
            )
        }

        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
        downloadManager?.enqueue(request)

        Toast.makeText(context, "Downloading File", Toast.LENGTH_LONG).show()
    }

    private fun isExecutableApplication(context: Context, intent: Intent): Boolean {
        val packageManager = context.packageManager
        val list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return list.size > 0
    }

    override fun loadUrl(url: String?) {
        super.loadUrl(url)

        if (url?.length ?: return < 1000) {
            PrintLog.d("CustomWebView", "loadUrl : $url")
            PrintLog.d("CustomWebView", "cookie1 ${CookieManager.getInstance().getCookie(url)}")
        } else {
            PrintLog.d("CustomWebView", "loadUrl : ${url.substring(0, 100)}")
            PrintLog.d("CustomWebView", "cookie1 ${CookieManager.getInstance().getCookie(url)}")
        }
    }
}