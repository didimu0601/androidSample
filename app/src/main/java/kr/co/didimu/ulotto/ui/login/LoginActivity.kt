package kr.co.didimu.ulotto.ui.login

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import kr.co.didimu.ulotto.BaseActivity
import kr.co.didimu.ulotto.R
import kr.co.didimu.ulotto.data.dto.LoginRequest
import kr.co.didimu.ulotto.ui.dialog.CustomAlertDialog
import kr.co.didimu.ulotto.ui.dialog.PopupFragment
import kr.co.didimu.ulotto.ui.login.prompt.PromptUtils
import kr.co.didimu.ulotto.util.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_prompt_login.*
import javax.inject.Inject

class LoginActivity : BaseActivity(), Login.View {
    private val TAG by lazy {  LoginActivity::class.java.simpleName }
    private val MSG_NOT_DEFINE = -1

    @Inject
    override lateinit var presenter: Login.Presenter

    override val isActive
        get() = !isDestroyed && !isFinishing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (!PromptUtils.isSupportedBio(this)) {
            switch_use_bio.isChecked = false
            switch_use_bio.visibility = View.INVISIBLE
        }

        switch_use_bio.isChecked = SharedPref.getInstance().getBoolean(SharedPref.PREF_USE_PROMPT_LOGIN)
        if (switch_use_bio.isChecked) {
            confirmPromptDevice()
        }

        switch_use_bio.setOnCheckedChangeListener { buttonView, isChecked ->
            PrintLog.d(TAG, "setOnCheckedChangeListener isChecked $isChecked")
            SharedPref.getInstance().setBoolean(SharedPref.PREF_USE_PROMPT_LOGIN, isChecked)

            if (!buttonView.isPressed) { //checking user press switchButton
                return@setOnCheckedChangeListener
            }

            if (isChecked && !isSuccededLogin()) {
                return@setOnCheckedChangeListener
            }

            //20191018 psg
            if(!isChecked){
                clearPromptInfo()
            }
            setLoginLayout(isChecked,true)
        }

        if (SharedPref.getInstance().getBoolean(SharedPref.PREF_SAVE_ID)) {
            check_save_id.isChecked = true
            val id = Crypto.AESDecode(SharedPref.getInstance().getString(SharedPref.PREF_ID), Crypto.getSecureKey(this))
            edit_id.setText(id)
        }

        if (SharedPref.getInstance().getBoolean(SharedPref.PREF_AUTO_LOGIN)) {
            check_auto_login.isChecked = true
        }

        // 20191028 psg : animation 이미지만 스케일링하여 보여준다.
        val scale= this.resources.displayMetrics.densityDpi.toFloat()/ DisplayMetrics.DENSITY_DEFAULT

        val dpWidthInPx : Int = (140 * scale + 0.5f).toInt()
        val dpHeightInPx : Int = (140 * scale + 0.5f).toInt()

        Glide.with(this)
            .load(R.raw.fingerprint_ani)
            .asGif()
            .override(dpWidthInPx,dpHeightInPx)
            .into(image_status_ani)
    }

    override fun onBackPressed() {
        PrintLog.d(TAG, "onBackPressed")

        val count = supportFragmentManager.backStackEntryCount
        PrintLog.d(TAG, "backStackEntryCount $count")

        if (count != 0) {
            supportFragmentManager.popBackStack()
            return
        }

        //20191023 psg changed closing logic
        super.onBackPressed()
//        CustomAlertDialog.Builder(this).apply {
//            setMsg(getString(R.string.msg_must_login))
//            setPositiveButton{}
//            setNegativeButton {
//                cancelPromptLogin()
//                Utils.finishApplication(this@LoginActivity) }
//        }.build()
    }

    override fun onResume() {
        super.onResume()
        PrintLog.d(TAG, "onResume")
    }

    override fun onStart() {
        super.onStart()
        PrintLog.d(TAG, "onStart")
        presenter.start(this)
    }

    override fun onStop() {
        super.onStop()
        PrintLog.d(TAG, "onStop")

        if (isPromptMode()) {
            layout_prompt_login.visibility = View.GONE
            cancelPromptLogin()

            setLoginLayout(true,false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PrintLog.d(TAG, "onDestroy")
    }

    override fun onHttpFailure() {
        failHttp()
    }

    override fun checkPromptDevice(isRegister: Boolean) {
        PrintLog.d(TAG, "checkPromptDevice $isRegister")
        if (isRegister) {
            setLoginLayout(true, true)

//            var isDirectStart = SharedPref.getInstance().getBoolean(SharedPref.PREF_BIOMETRICS_DIRECT_PRESS_START)
//            if(isDirectStart){
//                SharedPref.getInstance().setBoolean(SharedPref.PREF_BIOMETRICS_DIRECT_PRESS_START,false)
//                Handler().postDelayed({
//                    layout_prompt_login.visibility = View.VISIBLE
//                    layout_login.visibility = View.GONE
//                    initPromptLayout()
//                    doPromptLogin()
//                }, 1)
//            }
        }
    }

    override fun successLogin(loginInfo: LoginRequest, isPromptLogin: Boolean) {
        PrintLog.d(TAG, "successLogin ${switch_use_bio.isChecked} isPromptLogin $isPromptLogin")

        SharedPref.getInstance().setString(SharedPref.PREF_ID, Crypto.AESEncode(loginInfo.userId, Crypto.getSecureKey(this)))
        SharedPref.getInstance().setString(SharedPref.PREF_PW, Crypto.AESEncode(loginInfo.password, Crypto.getSecureKey(this)))

        SharedPref.getInstance().setBoolean(SharedPref.PREF_AUTO_LOGIN, check_auto_login.isChecked)
        SharedPref.getInstance().setBoolean(SharedPref.PREF_SAVE_ID, check_save_id.isChecked)

        if (switch_use_bio.isChecked && !isPromptLogin) {
            setLoginLayout(true,true)
        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun failLogin(isWrong: Boolean) {
        PrintLog.d(TAG, "failLogin")

        if (isWrong) {
            CustomAlertDialog.Builder(this@LoginActivity).apply {
                setMsg(getString(R.string.msg_wrong_login))
                setPositiveButton(positiveId = android.R.string.ok)
            }.build()
        }
    }

    override fun successPromptLogin() {
        PrintLog.d(TAG, "successPromptLogin")

        image_status.visibility=View.VISIBLE
        image_status_ani.visibility=View.GONE

        image_status.setImageResource(R.drawable.ic_prompt_match)
        text_status.setText(R.string.msg_match_fingerprint)
        text_description.visibility = View.GONE
        button_cancel.visibility = View.GONE
        button_input_login.visibility = View.GONE

        val id = Crypto.AESDecode(SharedPref.getInstance().getString(SharedPref.PREF_ID), Crypto.getSecureKey(this))?: return
        val pw = Crypto.AESDecode(SharedPref.getInstance().getString(SharedPref.PREF_PW), Crypto.getSecureKey(this))?: return

        presenter.registerPromptDevice(Utils.getAndroidId(this), id)
        SharedPref.getInstance().setBoolean(SharedPref.PREF_IS_SUCCESS_PROMPT_LOGIN, true)

        val login = LoginRequest(id, pw, "1.0.0", "1.0.1")

        presenter.doLogin(login, true)
    }

    override fun helpPromptLogin(id: Int) {
        PrintLog.d(TAG, "helpPromptLogin")

        // 20191028 psg 취소 아이콘이 있으니 메시지로만 한다. 자꾸 취소를 누르게 됨.
//        image_status.visibility=View.VISIBLE
//        image_status_ani.visibility=View.GONE

        image_status.setImageResource(R.drawable.ic_prompt_nomatch)
        text_status.setText(getPromptMessageId(id))
        button_input_login.visibility = View.GONE
        button_cancel.visibility = View.VISIBLE

    }

    override fun failPromptLogin(id: Int) {
        PrintLog.d(TAG, "failPromptLogin")
//        image_status.visibility=View.VISIBLE
//        image_status_ani.visibility=View.GONE

        image_status.setImageResource(R.drawable.ic_prompt_nomatch)
        button_input_login.visibility = View.GONE
        button_cancel.visibility = View.VISIBLE

        when (id) {
            Constants.PROMPT_ERROR_TEMPOLARYLOCK -> text_status.setText(R.string.msg_not_match_many_times)
            Constants.PROMPT_ERROR_PERMANENTLOCK -> text_status.setText(R.string.msg_not_match_permanent_lock)
            else -> text_status.setText(R.string.msg_not_match_fingerprint)
        }
    }

    fun onClick(view: View) {
        when(view.id) {
            R.id.button_login -> {
                val id = edit_id?.text.toString()
                val pw = edit_password?.text.toString()

                if (TextUtils.isEmpty(id) || TextUtils.isEmpty(pw)) {
                    val msg = if (TextUtils.isEmpty(id)) getString(R.string.msg_empty_id) else getString(R.string.msg_empty_pw)

                    CustomAlertDialog.Builder(this@LoginActivity).apply {
                        setMsg(msg)
                        setPositiveButton(positiveId = android.R.string.ok)
                    }.build()
                    return
                }

                val login = LoginRequest(id, pw, "1.0.0", "1.0.1")
                presenter.doLogin(login)
            }

            R.id.image_prompt_ic , R.id.text_prompt-> {
                layout_prompt_login.visibility = View.VISIBLE
                layout_login.visibility = View.GONE
                initPromptLayout()
                doPromptLogin()
            }

            R.id.button_cancel, R.id.button_input_login -> {
                cancelPromptLogin()
                //20191023 psg : goto bio initial
                setLoginLayout(true,false)
                //layout_prompt_login.visibility = View.GONE
            }

            R.id.text_guide -> {
                CustomAlertDialog.Builder(this). apply {
                    setTitle(getString(R.string.title_sign_up))
                    setMsg(getString(R.string.msg_sing_up))
                    setPositiveButton(positiveId = android.R.string.ok)
                }.build()
            }
            R.id.text_contact_us -> {
                CustomAlertDialog.Builder(this). apply {
                    setTitle(getString(R.string.contact_us))
                    setMsg(HtmlCompat.fromHtml(getString(R.string.html_contact_us), HtmlCompat.FROM_HTML_MODE_LEGACY).toString())
                    setPositiveButton(positiveId = android.R.string.ok)
                }.build()
            }
            R.id.text_find_id -> {
                supportFragmentManager.findFragmentById(R.id.layout_frame)
                        as PopupFragment? ?: PopupFragment.newInstance(Constants.getFindIdUrl()).also {
                    replaceFragmentInActivity(it, R.id.layout_frame)
                }
            }
            R.id.text_find_pw -> {
                supportFragmentManager.findFragmentById(R.id.layout_frame)
                        as PopupFragment? ?: PopupFragment.newInstance(Constants.getFindPwUrl()).also {
                    replaceFragmentInActivity(it, R.id.layout_frame)
                }
            }
        }
    }

    private fun setLoginLayout(isChecked: Boolean ,bStartDirect : Boolean) {
        if (isChecked) {
            Utils.hideKeyboard(this@LoginActivity)
        } else {
            layout_prompt_login.visibility = View.GONE
            cancelPromptLogin()
        }

        layout_login.visibility = View.VISIBLE

        group_login.visibility = if (isChecked) View.GONE else View.VISIBLE
        group_prompt_login.visibility = if (isChecked) View.VISIBLE else View.GONE
        //20191025 psg
        text_find_id.visibility = if (isChecked) View.GONE else View.VISIBLE
        text_find_pw.visibility = if (isChecked) View.GONE else View.VISIBLE
        text_contact_us.visibility = if (isChecked) View.GONE else View.VISIBLE
        text_copyright.visibility = if (isChecked) View.GONE else View.VISIBLE
        text_guide.visibility = if (isChecked) View.INVISIBLE else View.VISIBLE

        //20191104 psg
        if (isChecked) {
            if(bStartDirect){
                Handler().postDelayed({
                    layout_prompt_login.visibility = View.VISIBLE
                    layout_login.visibility = View.GONE
                    initPromptLayout()
                    doPromptLogin()
                }, 1)
            }
            else{
                layout_prompt_login.visibility = View.GONE
            }
        }

    }

    private fun confirmPromptDevice() {
        if (!SharedPref.getInstance().getBoolean(SharedPref.PREF_IS_SUCCESS_PROMPT_LOGIN)) {
            return
        }

        val id = Crypto.AESDecode(SharedPref.getInstance().getString(SharedPref.PREF_ID), Crypto.getSecureKey(this))?: return
        presenter.confirmPromptDevice(Utils.getAndroidId(this@LoginActivity), id)
    }

    private fun doPromptLogin() {
        if (!PromptUtils.isFingerprintAvailable(this)) {
            CustomAlertDialog.Builder(this@LoginActivity).apply {
                setMsg(getString(R.string.msg_unavailable_prompt_login))
                setPositiveButton(positiveId = android.R.string.ok)
            }.build()
            return
        }

        presenter.doPromptLogin()
    }

    private fun cancelPromptLogin() {
        presenter.cancelPromptLogin()
    }

    private fun getPromptMessageId(messageId: Int) =
        when (messageId) {
            Constants.HELP_PARTIAL -> R.string.msg_bio_help_partial
            Constants.HELP_INSUFFICIENT -> R.string.msg_bio_help_partial
            Constants.HELP_DIRTY -> R.string.msg_bio_help_dirty
            Constants.HELP_SLOW -> R.string.msg_bio_help_too_slow
            Constants.HELP_FAST -> R.string.msg_bio_help_too_fast
            else -> MSG_NOT_DEFINE
        }

    private fun initPromptLayout() {
        //image_status.setImageResource(R.drawable.ic_prompt_fingerprint)
        image_status.visibility=View.GONE
        image_status_ani.visibility=View.VISIBLE

        //20191025 psg
        //text_status.setText(R.string.msg_fingerprint)
        text_status.setText("")
        text_description.setText(R.string.touch_fingerprint)
    }

    private fun isPromptMode(): Boolean {
        return switch_use_bio.isChecked && group_login.visibility != View.VISIBLE
    }

    private fun isSuccededLogin(): Boolean {
        val id = Crypto.AESDecode(SharedPref.getInstance().getString(SharedPref.PREF_ID), Crypto.getSecureKey(this))?: return false
        val pw = Crypto.AESDecode(SharedPref.getInstance().getString(SharedPref.PREF_PW), Crypto.getSecureKey(this))?: return false

        return !TextUtils.isEmpty(id) && !TextUtils.isEmpty(pw)
    }

    //20191018 psg clear pro
    private fun clearPromptInfo() {
        //remove prefs
        SharedPref.getInstance().setBoolean(SharedPref.PREF_IS_SUCCESS_PROMPT_LOGIN, false)
        //id pwd 도 지워주어야 한다.
        SharedPref.getInstance().setString(SharedPref.PREF_ID, "")
        SharedPref.getInstance().setString(SharedPref.PREF_PW, "")

        //clrear server udid
        presenter.removePromptDevice(Utils.getAndroidId(this))
    }

}