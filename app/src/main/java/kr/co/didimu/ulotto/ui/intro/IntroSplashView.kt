package kr.co.didimu.ulotto.ui.intro

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import kr.co.didimu.ulotto.R
import kr.co.didimu.ulotto.data.repository.ServiceRepository
import kr.co.didimu.ulotto.ui.main.MainActivity
import kr.co.didimu.ulotto.util.*
import kotlinx.android.synthetic.main.layout_intro.view.*
import javax.inject.Inject

class IntroSplashView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

//    private lateinit var messageHandler: MessageHandler

    @Inject
    lateinit var repository: ServiceRepository

    init {
        initSplash()
    }

    private fun initSplash() {
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = li.inflate(R.layout.layout_intro, this, false)
        addView(view)

//        Glide.with(context)
//            .load(R.raw.ic_splash)
//            .asGif()
//            .into(image_intro)

//        View.inflate(context, R.layout.layout_intro, this)
//        messageHandler = MessageHandler(context, context as Handler.Callback)

        if (Constants.IS_DEBUG_MODE) {
            val activity = context as MainActivity
            val shared = SharedPref(activity)
//            ServerType.baseUrl = ServerType.from(shared.getInt(SharedPref.PREF_SERVER_MODE)).getBaseUrl()
            layout_test.setOnClickListener {
                if (!activity.isActive) {
                    return@setOnClickListener
                }

                val dialog = object : ServerSettingDialog(activity) {
                    override fun selectRelServer() {
                        ServerType.apiUrl = ServerType.REL.getApiUrl()
                        shared.setInt(SharedPref.PREF_SERVER_MODE, ServerType.REL.code)
                        Toast.makeText(this.context, "Rel Server Setting...", Toast.LENGTH_SHORT).show()
                        dismiss()
                        Handler().postDelayed({
                            Utils.finishApplication(activity, true)
                        }, 500)
                    }

                    override fun selectDevServer() {
                        ServerType.apiUrl = ServerType.DEV.getApiUrl()
                        shared.setInt(SharedPref.PREF_SERVER_MODE, ServerType.DEV.code)
                        Toast.makeText(this.context, "Dev Server Setting...", Toast.LENGTH_SHORT).show()
                        dismiss()
                        Handler().postDelayed({
                            Utils.finishApplication(activity, true)
                        }, 500)
                    }

                    override fun selectQaServer() {
                        ServerType.apiUrl = ServerType.QA.getApiUrl()
                        shared.setInt(SharedPref.PREF_SERVER_MODE, ServerType.QA.code)
                        Toast.makeText(this.context, "Qa Server Setting...", Toast.LENGTH_SHORT).show()
                        dismiss()
                        Handler().postDelayed({
                            Utils.finishApplication(activity, true)
                        }, 500)
                    }

                    override fun selectSslRelServer() {
                        ServerType.apiUrl = ServerType.REL.getApiUrl()
                        shared.setInt(SharedPref.PREF_SERVER_MODE, ServerType.SSLREL.code)
                        Toast.makeText(this.context, "SslRel Server Setting...", Toast.LENGTH_SHORT).show()
                        dismiss()
                        Handler().postDelayed({
                            Utils.finishApplication(activity, true)
                        }, 500)
                    }

                    override fun selectSslDevServer() {
                        ServerType.apiUrl = ServerType.DEV.getApiUrl()
                        shared.setInt(SharedPref.PREF_SERVER_MODE, ServerType.SSLDEV.code)
                        Toast.makeText(this.context, "SslDev Server Setting...", Toast.LENGTH_SHORT).show()
                        dismiss()
                        Handler().postDelayed({
                            Utils.finishApplication(activity, true)
                        }, 500)
                    }

                    override fun selectSslQaServer() {
                        ServerType.apiUrl = ServerType.QA.getApiUrl()
                        shared.setInt(SharedPref.PREF_SERVER_MODE, ServerType.SSLQA.code)
                        Toast.makeText(this.context, "SslQa Server Setting...", Toast.LENGTH_SHORT).show()
                        dismiss()
                        Handler().postDelayed({
                            Utils.finishApplication(activity, true)
                        }, 500)
                    }
                }
                dialog.show()
            }
        }
    }

//    fun startSplash() {
//        val msg = Message.obtain()
//        msg.what = Constants.MSG_SPLASH_FINISHED
//        messageHandler.sendMessageDelayed(msg, Constants.INTRO_DELAY_TIME)
//    }
}