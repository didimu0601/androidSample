package kr.co.didimu.ulotto.ui.main

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import kotlinx.android.synthetic.main.activity_main.*
import kr.co.didimu.ulotto.BaseActivity
import kr.co.didimu.ulotto.R
import kr.co.didimu.ulotto.ui.dialog.CustomAlertDialog
import kr.co.didimu.ulotto.ui.dialog.DialogSelectListener
import kr.co.didimu.ulotto.ui.dialog.TermsInfoDialog
import kr.co.didimu.ulotto.ui.intro.IntroSplashView
import kr.co.didimu.ulotto.ui.login.LoginActivity
import kr.co.didimu.ulotto.util.*
import kr.co.didimu.ulotto.web.ChromeClient
import kr.co.didimu.ulotto.web.Scheme
import kr.co.didimu.ulotto.web.WebClient
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection


class MainActivity : BaseActivity(), Main.View, Handler.Callback {
    private val TAG by lazy {  MainActivity::class.java.simpleName }

    // 구현 방식의 차이 로컬 로그인 인경우는 필요하고, 웹로그인의 경우는 불필
    private val m_bUseCheckVerApi = false

    // 사용자권한 확인이 필요한경우 설정
    private val m_bUseCheckTerms = false

    // file upload
    var mUploadMessage: ValueCallback<Uri>? = null
    var uploadMessage: ValueCallback<Array<Uri>>? = null

    @Inject
    override lateinit var presenter: Main.Presenter

    private lateinit var introSplashView: IntroSplashView
    override val isActive: Boolean
        get() = !isFinishing && !isDestroyed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PrintLog.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        startSplash()
        initWebView()

        if(m_bUseCheckVerApi){
            //버전체크하는 버전의 경우
            requestVersion()
        }else{
            //바로웹으로 이동하는경우 아무것도 하지 않음.
            checkTermsAgree()
        }

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    PrintLog.d(TAG, "getInstanceId failed"+task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                //val msg = getString(R.string.msg_token_fmt, token)
                PrintLog.d(TAG, "msg_token ="+token)
                //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })

        //kakao key hash
        try {
            val info =
                packageManager.getPackageInfo("kr.co.didimu.ulotto", PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val tkeyHash : String = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                Log.e("KeyHash:", tkeyHash)
//                Toast.makeText(baseContext, "KeyHash:"+ tkeyHash, Toast.LENGTH_SHORT).show()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        initNaverData()

        var  tSwipe : SwipeRefreshLayout = findViewById(R.id.swiperefresh)
        tSwipe.setOnRefreshListener {
            Log.i(TAG, "onRefresh called from SwipeRefreshLayout")

            // This method performs the actual data-refresh operation.
            // The method calls setRefreshing(false) when it's finished.
            myUpdateOperation()
        }
    }

    private fun myUpdateOperation() {
        webview.reload()
        var  tSwipe :SwipeRefreshLayout = findViewById(R.id.swiperefresh)
        tSwipe.setRefreshing(true);

    }

    private fun onPageReloadFinished() {
        var  tSwipe :SwipeRefreshLayout = findViewById(R.id.swiperefresh)
        tSwipe.setRefreshing(false);
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        PrintLog.d(TAG, "onNewIntent")

        handleIntent(intent)
    }

    override fun onBackPressed() {
        if (webview?.canGoBack() == true)
        {
            var weburl : String? = webview?.getUrl()
            if( weburl != null && !weburl?.equals(ServerType.webUrl,true)){
                webview.goBack()
                return
            }
        }
//        if (webview?.canGoBack() == true) {
//            webview.goBack()
//            return
//        }
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        PrintLog.d(TAG, "onStart")
        presenter.start(this)
    }

    override fun onStop() {
        super.onStop()
        PrintLog.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        PrintLog.d(TAG, "onDestroy")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        PrintLog.d(TAG, "onActivityResult")
        PrintLog.d(TAG, "ServerType webUrl ${ServerType.webUrl}" )
        PrintLog.d(TAG, "cookie2 ${CookieManager.getInstance().getCookie(ServerType.webUrl)}")

        if (requestCode == Constants.REQUEST_LOGIN) {
            if(resultCode == Activity.RESULT_OK) {
                loadDefaultUrl()
            }
        }
        else if (requestCode == Constants.REQUEST_KAKAO_NOUI_LOGIN) {
            if(resultCode == Activity.RESULT_OK) {

                val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                PrintLog.d(TAG, "$matches")

                var nameStr = data?.getStringExtra("name")
                var emailStr = data?.getStringExtra("email")
                var idStr = data?.getStringExtra("kkId")
                if(nameStr==null) nameStr = ""
                if(emailStr==null) emailStr = ""
                if(idStr==null) idStr = ""

                afterKakaoLogin(nameStr,emailStr,idStr)
            }
        }
        else if (requestCode == Constants.REQUEST_USE_AUDIO) {// psg 20191014 STT
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            PrintLog.d(TAG, "$matches")

            if (!TextUtils.isEmpty(matches?.get(0))) {
                webview.loadUrl("${ServerType.webUrl}searchresult?q=${matches?.get(0)}")
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode === Constants.REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return
                print("result code = " + resultCode)
                var results: Array<Uri>? = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                uploadMessage?.onReceiveValue(results)
                uploadMessage = null
            }
        } else if (requestCode === Constants.FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return
            val result = if (intent == null || resultCode !== RESULT_OK) null else intent.data
            mUploadMessage?.onReceiveValue(result)
            mUploadMessage = null
        }
    }

    private fun afterKakaoLogin(nameStr : String , emailStr :String , idStr:String){
        var fcmRealStr =   SharedPref.getInstance().getString(SharedPref.PREF_FCM_KEY_REL)
        var fcmDevStr = SharedPref.getInstance().getString(SharedPref.PREF_FCM_KEY_DEV)
        if(fcmRealStr == null){
            fcmRealStr = ""
        }

        if(fcmDevStr == null){
            fcmDevStr = ""
        }

        val name64Str = Base64.encodeToString(nameStr?.toByteArray(), Base64.DEFAULT)
        val email64Str = Base64.encodeToString(emailStr?.toByteArray(), Base64.DEFAULT)
        val id64Str = Base64.encodeToString(idStr?.toByteArray(), Base64.DEFAULT)
        val fcmDevKeyStr = Base64.encodeToString(fcmDevStr?.toByteArray(), Base64.DEFAULT)
        val fcmRelKeyStr = Base64.encodeToString(fcmRealStr?.toByteArray(), Base64.DEFAULT)

        var urlLoginParam = "${ServerType.webUrl}/_Ext/sns/kakao/kakaoLoginApp.php?userid=${id64Str}&usernm=${name64Str}&email=${email64Str}&fcmKeyReal=${fcmRelKeyStr}&fcmKeyDev=${fcmDevKeyStr}"

        webview.loadUrl(urlLoginParam)
        PrintLog.d(TAG, "login kakao  = ${urlLoginParam}")

    }

    private fun afterNaverLogin(nameStr : String , emailStr :String , idStr:String){
        var fcmRealStr =   SharedPref.getInstance().getString(SharedPref.PREF_FCM_KEY_REL)
        var fcmDevStr = SharedPref.getInstance().getString(SharedPref.PREF_FCM_KEY_DEV)
        if(fcmRealStr == null){
            fcmRealStr = ""
        }

        if(fcmDevStr == null){
            fcmDevStr = ""
        }

        val name64Str = Base64.encodeToString(nameStr?.toByteArray(), Base64.DEFAULT)
        val email64Str = Base64.encodeToString(emailStr?.toByteArray(), Base64.DEFAULT)
        val id64Str = Base64.encodeToString(idStr?.toByteArray(), Base64.DEFAULT)
        val fcmDevKeyStr = Base64.encodeToString(fcmDevStr?.toByteArray(), Base64.DEFAULT)
        val fcmRelKeyStr = Base64.encodeToString(fcmRealStr?.toByteArray(), Base64.DEFAULT)

        var urlLoginParam = "${ServerType.webUrl}/_Ext/sns/naver/naverLoginApp.php?userid=${id64Str}&usernm=${name64Str}&email=${email64Str}&fcmKeyReal=${fcmRelKeyStr}&fcmKeyDev=${fcmDevKeyStr}"

        webview.loadUrl(urlLoginParam)
        PrintLog.d(TAG, "login naver  = ${urlLoginParam}")
    }
    // psg 20191014 STT
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            Constants.PERMISSION_AUDIO -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PrintLog.d(TAG, "onRequestPermissionsResult PERMISSION_AUDIO")

                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ko-KR"); //언어지정입니다.
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);   //검색을 말한 결과를 보여주는 갯수
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "검색어를 말해 주세요.")

                    startActivityForResult(intent, Constants.REQUEST_USE_AUDIO)
                }
            }
        }
    }

    override fun handleMessage(msg: Message?): Boolean {
        when (msg?.what) {
            Constants.MSG_SPLASH_FINISHED -> {
                PrintLog.d(TAG, "MSG_SPLASH_FINISHED")
                SharedPref.getInstance().setBoolean(SharedPref.PREF_IS_FIRST_COMPLETE, true)
                handleIntent(intent)
                layout_main.removeView(introSplashView)
            }

            Constants.MSG_PAGELOAD_FINISHED -> {
                PrintLog.d(TAG, "MSG_PAGELOAD_FINISHED")
                onPageReloadFinished()
            }

            Constants.MSG_LOGOUT -> {
                PrintLog.d(TAG, "MSG_LOGOUT")
                Utils.deleteCookie()
                SharedPref.getInstance().setBoolean(SharedPref.PREF_AUTO_LOGIN, false)
                SharedPref.getInstance().setStringSet(SharedPref.PREF_COOKIES, null)

                //20191022 psg : clear passwd if not in prompt state
                if(!SharedPref.getInstance().getBoolean(SharedPref.PREF_USE_PROMPT_LOGIN)){
                    SharedPref.getInstance().setString(SharedPref.PREF_PW,"")
                }

                //20191029 psg : clear direct login
                //SharedPref.getInstance().setBoolean(SharedPref.PREF_BIOMETRICS_DIRECT_PRESS_START,false)
                val isPromtLogin = SharedPref.getInstance().getBoolean(SharedPref.PREF_USE_PROMPT_LOGIN)
                val isPromtSuccess = SharedPref.getInstance().getBoolean(SharedPref.PREF_IS_SUCCESS_PROMPT_LOGIN)
                if(isPromtLogin && isPromtSuccess){
                    SharedPref.getInstance().setBoolean(SharedPref.PREF_BIOMETRICS_DIRECT_PRESS_START,true)
                }else{
                    SharedPref.getInstance().setBoolean(SharedPref.PREF_BIOMETRICS_DIRECT_PRESS_START,false)
                }

                val uri = Uri.parse(Constants.getLoginScheme())
                Scheme.resolveUrl(this@MainActivity, uri)
//                webview.reload()
            }

            Constants.MSG_MOVE_URL -> {
                PrintLog.d(TAG, "MSG_MOVE_URL")
                val moveUrl = msg.obj as String
                PrintLog.d(TAG, "MSG_MOVE_URL $moveUrl")
                if (!TextUtils.isEmpty(moveUrl) || moveUrl.startsWith("http", ignoreCase = true)) {
                    webview?.loadUrl(moveUrl)
                }
            }

//            Constants.MSG_USE_AUDIO -> { //psg 20191014 :STT
//                PrintLog.d(TAG, "MSG_USE_AUDIO")
//                ActivityCompat.requestPermissions(this@MainActivity,
//                    PermissionType.AUDIO.getPermissionList(),
//                    Constants.PERMISSION_AUDIO)
//            }

            Constants.NAVER_NOUI_LOGIN -> { //naver login
                PrintLog.d(TAG, "naver Login")
                doNaverLogin()
            }

        }
        return true
    }

//    private val OAUTH_CLIENT_ID = "Fr9EplM4caqhSqrNOrri"
    private val OAUTH_CLIENT_ID = "pFVJ12VZ0FMZakZkUsYF"

    private val OAUTH_CLIENT_SECRET = "ktHfFRmz4n"
//    private val OAUTH_CLIENT_SECRET = "i_2m_ej_PL"
    private val OAUTH_CLIENT_NAME = "ULotto"

    private var mOAuthLoginInstance: OAuthLogin? = null

    private fun initNaverData(){
        mOAuthLoginInstance = OAuthLogin.getInstance()
        mOAuthLoginInstance?.init(
            this@MainActivity
            ,OAUTH_CLIENT_ID
            ,OAUTH_CLIENT_SECRET
            ,OAUTH_CLIENT_NAME

        )
    }

    /**
     * startOAuthLoginActivity() 호출시 인자로 넘기거나, OAuthLoginButton 에 등록해주면 인증이 종료되는 걸 알 수 있다.
     */
    private val mOAuthLoginHandler = object : OAuthLoginHandler() {
        override fun run(success: Boolean) {
            if (success) {
                val accessToken = mOAuthLoginInstance?.getAccessToken(this@MainActivity)
                val refreshToken = mOAuthLoginInstance?.getRefreshToken(this@MainActivity)
                val expiresAt = mOAuthLoginInstance?.getExpiresAt(this@MainActivity)
                val tokenType = mOAuthLoginInstance?.getTokenType(this@MainActivity)
                if(accessToken != null){
                    getNaverPofileFromHttp(accessToken)
                }
            } else {
                val errorCode = mOAuthLoginInstance?.getLastErrorCode(this@MainActivity)?.code
                val errorDesc = mOAuthLoginInstance?.getLastErrorDesc(this@MainActivity)
                Toast.makeText(
                    this@MainActivity,
                    "errorCode:$errorCode, errorDesc:$errorDesc",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getNaverPofileFromHttp(accessToken:String) {

        val thread = Thread {
            println("${Thread.currentThread()} has run.")
            var header = "Bearer " + accessToken

            val mURL = URL("https://openapi.naver.com/v1/nid/me")
            val connection: HttpsURLConnection = mURL.openConnection() as HttpsURLConnection


            with(connection) {
                // optional default is GET
                requestMethod = "GET"

                setRequestProperty("Authorization",header)

                println("URL : $url")
                println("Response Code : $responseCode")

                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuffer()
                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()
                    println("Response : $response")

                    var jsonObj = JSONObject(response.toString())
                    if(jsonObj!=null){
                        val retCode = jsonObj.get("resultcode")
                        if((retCode as String).toInt() == 0){
                            val retResponse :JSONObject = jsonObj.get("response") as JSONObject
                            if(retResponse!=null){
                                var idStr = retResponse.get("id")
                                var nameStr = ""
                                try {
                                    val nameObj = retResponse.get("name")
                                    if(nameObj!=null){
                                        nameStr = nameObj as String
                                    }

                                } catch (e: Exception) {
                                    //e.printStackTrace()
                                }
                                var emailStr = ""
                                try {
                                    val emailObj = retResponse.get("email")
                                    if(emailObj!=null){
                                        emailStr = emailObj as String
                                    }
                                } catch (e: Exception) {
                                    //e.printStackTrace()
                                }
//                                val nickStr = retResponse.get("nickname")
                                if(idStr == null) idStr = ""
                                if(nameStr == null) nameStr = ""
                                if(emailStr == null) emailStr = ""

                                this@MainActivity.runOnUiThread(java.lang.Runnable {
                                    afterNaverLogin(nameStr as String,emailStr as String,idStr as String)
                                })

                            }
                        }
                        else{
                            this@MainActivity.runOnUiThread(java.lang.Runnable {
                                Toast.makeText(
                                    this@MainActivity,
                                    "errorCode:$retCode, errorDesc:naver login failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            })
                        }
                    }
                }
            }
        }
        thread.start()

    }

    private fun doNaverLogin(){
        mOAuthLoginInstance?.startOauthLoginActivity(this@MainActivity, mOAuthLoginHandler)
    }

    override fun onHttpFailure() {
        PrintLog.d(TAG, "onHttpFailure")
        failHttp()
    }

    override fun checkVersion(isForceUpdate: Boolean, isUpdate: Boolean) {
        PrintLog.d(TAG, "checkVersion $isForceUpdate $isUpdate")

        if (!isForceUpdate && !isUpdate) {
            SharedPref.getInstance().setBoolean(SharedPref.PREF_IS_SHOW_UPDATE_BUTTON, false)
            checkTermsAgree()
            return
        }

        SharedPref.getInstance().setBoolean(SharedPref.PREF_IS_SHOW_UPDATE_BUTTON, true)
        if (isForceUpdate) {
            CustomAlertDialog.Builder(this@MainActivity).apply {
                setMsg(getString(R.string.msg_force_update_application))
                setPositiveButton {
                    val uri = Uri.parse(Constants.getDownloadUrl())
                    Scheme.resolveUrl(this@MainActivity, uri)
                    Utils.finishApplication(this@MainActivity)
                }
            }.build()
        } else {
            CustomAlertDialog.Builder(this@MainActivity).apply {
                setMsg(getString(R.string.msg_update_application))
                setPositiveButton {
                    val uri = Uri.parse(Constants.getDownloadUrl())
                    Scheme.resolveUrl(this@MainActivity, uri)
                    Utils.finishApplication(this@MainActivity)
                }
                setNegativeButton {
                    checkTermsAgree()
                }
            }.build()
        }
    }

    override fun checkTermsAgree() {
//        if (isTermsAgree()) { //khm because loginActivity(앱 구동 시 실행되는 경우가 있는 경우) is native
//            loadUrl(ServerType.webUrl)
//        } else {
//            showPrivacyInfo()
//        }
        if (!isTermsAgree() && m_bUseCheckTerms) {
            showPrivacyInfo()
        } else {
            if(!m_bUseCheckVerApi){
                loadDefaultUrl()
                return;
            }
            //loadUrl(ServerType.webUrl) : direct go web
            PrintLog.d(TAG, "checkTermsAgree")
            val id = Crypto.AESDecode(SharedPref.getInstance().getString(SharedPref.PREF_ID),
                Crypto.getSecureKey(this))?: ""
            val pw = Crypto.AESDecode(SharedPref.getInstance().getString(SharedPref.PREF_PW),
                Crypto.getSecureKey(this))?: ""

            if (!isAutoLogin() ) {

                //20191023 psg가 바이오 로그인인 경우 바로 체크하도록 처리추
                val isPromtLogin = SharedPref.getInstance().getBoolean(SharedPref.PREF_USE_PROMPT_LOGIN)
                val isPromtSuccess = SharedPref.getInstance().getBoolean(SharedPref.PREF_IS_SUCCESS_PROMPT_LOGIN)
                if(isPromtLogin && isPromtSuccess){
                    SharedPref.getInstance().setBoolean(SharedPref.PREF_BIOMETRICS_DIRECT_PRESS_START,true)
                }else{
                    SharedPref.getInstance().setBoolean(SharedPref.PREF_BIOMETRICS_DIRECT_PRESS_START,false)
                }

                Intent().setClass(this, LoginActivity::class.java).also {
                    ActivityCompat.startActivityForResult(
                        this as Activity,
                        it,
                        Constants.REQUEST_LOGIN,
                        null
                    )
                }
                return
            }

            presenter.doAutoLogin(id, pw)
        }
    }

    private fun requestVersion() {
        presenter.requestVersion(Utils.getAppVersion(this@MainActivity))
    }

    private fun isAutoLogin(): Boolean {
        return SharedPref.getInstance().getBoolean(SharedPref.PREF_AUTO_LOGIN)
    }

    private fun startSplash() {
        introSplashView = IntroSplashView(this)
        layout_main.addView(introSplashView,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
//        introSplashView.startSplash()
    }

    private fun showPrivacyInfo() {
        val dialog = TermsInfoDialog.newInstance(object : DialogSelectListener {
            override fun select(isPositive: Boolean) {
                if (isPositive) {
                    loadDefaultUrl()
                    SharedPref.getInstance().setBoolean(SharedPref.PREF_IS_TERMS_AGREE, true)
                } else {
                    showTermsPopup()
                }
            }
        })

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(dialog, TAG)
        transaction.commitAllowingStateLoss()
    }

    private fun initWebView() {
        Utils.initCookie()

        SharedPref.getInstance().setBoolean(SharedPref.PREF_IS_FIRST_COMPLETE, false)
        webview.webChromeClient = ChromeClient()
        webview.webViewClient = WebClient()

        CookieManager.getInstance().setCookie(ServerType.webUrl, "AppInfo=DDU-lotto-${Utils.getAppVersion(this)}")
        Utils.flushCookies()
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return

        PrintLog.d(TAG, "handleIntent ${intent.data}")
        Scheme.resolveUrl(this, uri)
    }

    private fun loadUrl(url: String) {
        PrintLog.d(TAG, "loadUrl $url")
        PrintLog.d(TAG, "cookie1 ${CookieManager.getInstance().getCookie(url)}")
        CookieManager.getInstance().setCookie(url, "AppInfo=DDU-lotto-${Utils.getAppVersion(this)}")

        webview?.loadUrl(url)
    }

    private fun loadDefaultUrl() {
        //todelete : this is test page
        var bUseTestFile = false

        if(bUseTestFile){
            //load from file
            val filePath = "file:///android_asset/www/index.html"
            loadUrl(filePath)
        }else{
            // load from url
            loadUrl(ServerType.webUrl)
        }
    }

    private fun isTermsAgree(): Boolean {
        return SharedPref.getInstance().getBoolean(SharedPref.PREF_IS_TERMS_AGREE)
    }

    private fun showTermsPopup() {
       CustomAlertDialog.Builder(this@MainActivity).apply {
           setMsg(getString(R.string.msg_terms_warning_not_agree))
           setPositiveButton(getString(R.string.action_finish_application)) { Utils.finishApplication(this@MainActivity) }
           setNegativeButton(getString(R.string.action_cancel)) {}
       }.build()
    }
}
