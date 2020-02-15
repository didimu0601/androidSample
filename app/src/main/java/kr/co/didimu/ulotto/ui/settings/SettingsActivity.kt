package kr.co.didimu.ulotto.ui.settings

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import kr.co.didimu.ulotto.BaseActivity
import kr.co.didimu.ulotto.R
import kr.co.didimu.ulotto.ui.dialog.CustomAlertDialog
import kr.co.didimu.ulotto.ui.intro.ServerSettingDialog
import kr.co.didimu.ulotto.ui.login.prompt.PromptUtils
import kr.co.didimu.ulotto.util.*
import kr.co.didimu.ulotto.web.Scheme
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity(), Settings.View {
    private val TAG by lazy { SettingsActivity::class.java.simpleName }

    override lateinit var presenter: Settings.Presenter //khm currently not used, but for later

    var verPressed : Int = 0

    override val isActive: Boolean
        get() = !isFinishing && !isDestroyed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PrintLog.d(TAG, "onCreate")
        setContentView(R.layout.activity_settings)

        switch_use_bio.isChecked = SharedPref.getInstance().getBoolean(SharedPref.PREF_USE_PROMPT_LOGIN)
        if (!PromptUtils.isSupportedBio(this)) {
            switch_use_bio.isChecked = false
            group_use_bio.visibility = View.GONE
        }

        switch_use_bio.setOnCheckedChangeListener { buttonView, isChecked ->
            SharedPref.getInstance().setBoolean(SharedPref.PREF_USE_PROMPT_LOGIN, isChecked)

            if (!buttonView.isPressed) {
                return@setOnCheckedChangeListener
            }

            if (isChecked && !PromptUtils.isFingerprintAvailable(this)) {
                CustomAlertDialog.Builder(this@SettingsActivity).apply {
                    setMsg(getString(R.string.msg_unavailable_prompt_login))
                    setPositiveButton(positiveId = android.R.string.ok)
                }.build()
            }
        }

        text_version.text = getString(R.string.title_version, Utils.getAppVersion(this@SettingsActivity))
        showUpdateVersion()

//        if (Constants.IS_DEBUG_MODE) {
//            val shared = SharedPref(this)
////            ServerType.baseUrl = ServerType.from(shared.getInt(SharedPref.PREF_SERVER_MODE)).getBaseUrl()
//            layout_test_setting.setOnClickListener {
//                if (!this.isActive) {
//                    return@setOnClickListener
//                }

//            }
//        }
    }

    override fun onHttpFailure() {
        PrintLog.d(TAG, "onHttpFailure")
        failHttp()
    }

    fun onClick(view: View) {
        PrintLog.d(TAG, "onClick")
        when(view.id) {
            R.id.text_finish -> {
                finish()
            }

            R.id.button_update -> {
                val uri = Uri.parse(Constants.getDownloadUrl())
                Scheme.resolveUrl(this@SettingsActivity, uri)
            }

            //20191028 psg : 릴리즈 에서도 서버 설정을 변경할 수 있도록 함.
            R.id.text_version -> {
                verPressed ++
//                if(verPressed % 6 >= 4){
//                    val i = this.packageManager.getPackageInfo(this.packageName, 0)
//                    val versioncode = i.longVersionCode
//                    val appver =  Utils.getAppVersion(this@SettingsActivity)
//                    var strApp = appver + "(" + versioncode + ")"
//                    text_version.text = getString(R.string.title_version,strApp)
//                }

                if(verPressed % 6 == 5){
                    verPressed = 0
                    showServerDialog()
                }
            }
        }
    }

    //20191028 psg : 릴리즈 에서도 서버 설정을 변경할 수 있도록 함.
    private fun showServerDialog() {
        val shared = SharedPref(this)
        var dialog = object : ServerSettingDialog(this) {
            override fun selectRelServer() {
                ServerType.apiUrl = ServerType.REL.getApiUrl()
                shared.setInt(SharedPref.PREF_SERVER_MODE, ServerType.REL.code)
                Toast.makeText(this.context, "Rel Server Setting...", Toast.LENGTH_SHORT).show()
                dismiss()
                Handler().postDelayed({
                    Utils.finishApplication(this@SettingsActivity, true)
                }, 500)
            }

            override fun selectDevServer() {
                ServerType.apiUrl = ServerType.DEV.getApiUrl()
                shared.setInt(SharedPref.PREF_SERVER_MODE, ServerType.DEV.code)
                Toast.makeText(this.context, "Dev Server Setting...", Toast.LENGTH_SHORT).show()
                dismiss()
                Handler().postDelayed({
                    Utils.finishApplication(this@SettingsActivity, true)
                }, 500)
            }

            override fun selectQaServer() {
                ServerType.apiUrl = ServerType.QA.getApiUrl()
                shared.setInt(SharedPref.PREF_SERVER_MODE, ServerType.QA.code)
                Toast.makeText(this.context, "Qa Server Setting...", Toast.LENGTH_SHORT).show()
                dismiss()
                Handler().postDelayed({
                    Utils.finishApplication(this@SettingsActivity, true)
                }, 500)
            }

            override fun selectSslRelServer() {
                ServerType.apiUrl = ServerType.REL.getApiUrl()
                shared.setInt(SharedPref.PREF_SERVER_MODE, ServerType.SSLREL.code)
                Toast.makeText(this.context, "SslRel Server Setting...", Toast.LENGTH_SHORT).show()
                dismiss()
                Handler().postDelayed({
                    Utils.finishApplication(this@SettingsActivity, true)
                }, 500)
            }

            override fun selectSslDevServer() {
                ServerType.apiUrl = ServerType.DEV.getApiUrl()
                shared.setInt(SharedPref.PREF_SERVER_MODE, ServerType.SSLDEV.code)
                Toast.makeText(this.context, "SslDev Server Setting...", Toast.LENGTH_SHORT).show()
                dismiss()
                Handler().postDelayed({
                    Utils.finishApplication(this@SettingsActivity, true)
                }, 500)
            }

            override fun selectSslQaServer() {
                ServerType.apiUrl = ServerType.QA.getApiUrl()
                shared.setInt(SharedPref.PREF_SERVER_MODE, ServerType.SSLQA.code)
                Toast.makeText(this.context, "SslQa Server Setting...", Toast.LENGTH_SHORT).show()
                dismiss()
                Handler().postDelayed({
                    Utils.finishApplication(this@SettingsActivity, true)
                }, 500)
            }
        }
        dialog.show()
    }

    private fun showUpdateVersion() {
        if (SharedPref.getInstance().getBoolean(SharedPref.PREF_IS_SHOW_UPDATE_BUTTON)) {
            text_new_version.visibility = View.GONE
            button_update.visibility = View.VISIBLE
        } else {
            text_new_version.visibility = View.VISIBLE
            button_update.visibility = View.GONE
        }
    }
}