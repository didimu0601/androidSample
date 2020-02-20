package kr.co.didimu.ulotto.ui.main

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
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
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject


class MainActivity : BaseActivity(), Main.View, Handler.Callback {
    private val TAG by lazy {  MainActivity::class.java.simpleName }

    // 구현 방식의 차이 로컬 로그인 인경우는 필요하고, 웹로그인의 경우는 불필
    private val m_bUseCheckVerApi = false

    // 사용자권한 확인이 필요한경우 설정
    private val m_bUseCheckTerms = false

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
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        initNaverData()

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        PrintLog.d(TAG, "onNewIntent")

        handleIntent(intent)
    }

    override fun onBackPressed() {
        if (webview?.canGoBack() == true) {
            webview.goBack()
            return
        }
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

                val name64Str = Base64.encodeToString(nameStr?.toByteArray(), Base64.DEFAULT)
                val email64Str = Base64.encodeToString(emailStr?.toByteArray(), Base64.DEFAULT)
                val id64Str = Base64.encodeToString(idStr?.toByteArray(), Base64.DEFAULT)

//                    webview.loadUrl("${ServerType.webUrl}searchresult?q=${matches?.get(0)}")
//                var urlLoginParam = "${ServerType.webUrl}/_Ext/sns/kakao/kakaoLogin.php?userid=${idStr}&usernm=${nameStr}&email=${emailStr}"
                var urlLoginParam = "${ServerType.webUrl}/_Ext/sns/kakao/kakaoLoginApp.php?userid=${id64Str}&usernm=${name64Str}&email=${email64Str}"

                webview.loadUrl(urlLoginParam)
                PrintLog.d(TAG, "login kakao  = ${urlLoginParam}")
//              http://ulotto.didimu.co.kr/_Ext/sns/kakao/kakaoLogin.php?userid=aaa&usernm=aaa&email=aaa
            }
        }
        else if (requestCode == Constants.REQUEST_USE_AUDIO) {// psg 20191014 STT
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            PrintLog.d(TAG, "$matches")

            if (!TextUtils.isEmpty(matches?.get(0))) {
                webview.loadUrl("${ServerType.webUrl}searchresult?q=${matches?.get(0)}")
            }
        }
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

            Constants.MSG_USE_AUDIO -> { //psg 20191014 :STT
                PrintLog.d(TAG, "MSG_USE_AUDIO")
                ActivityCompat.requestPermissions(this@MainActivity,
                    PermissionType.AUDIO.getPermissionList(),
                    Constants.PERMISSION_AUDIO)
            }

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
//                mOauthAT.setText(accessToken)
//                mOauthRT.setText(refreshToken)
//                mOauthExpires.setText(expiresAt.toString())
//                mOauthTokenType.setText(tokenType)
//                mOAuthState.setText(mOAuthLoginInstance.getState(this@MainActivity).toString())
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
