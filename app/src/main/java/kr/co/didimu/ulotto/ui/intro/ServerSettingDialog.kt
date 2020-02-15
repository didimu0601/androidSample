package kr.co.didimu.ulotto.ui.intro

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import kr.co.didimu.ulotto.R
import kr.co.didimu.ulotto.util.ServerType
import kr.co.didimu.ulotto.util.SharedPref
import kotlinx.android.synthetic.main.dialog_server_setting.*

abstract class ServerSettingDialog(context: Context) : Dialog(context) {
    init {
        setLayout()
    }

    private fun setLayout() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.dialog_server_setting)
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.CENTER)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        text_rel.text = ServerType.REL.getApiUrl()
        text_dev.text = ServerType.DEV.getApiUrl()
        text_qa.text = ServerType.QA.getApiUrl()
        text_ssl_rel.text = ServerType.SSLREL.getApiUrl()
        text_ssl_dev.text = ServerType.SSLDEV.getApiUrl()
        text_ssl_qa.text = ServerType.SSLQA.getApiUrl()

        when(ServerType.from(SharedPref.getInstance().getInt(SharedPref.PREF_SERVER_MODE))) {
            ServerType.REL -> text_server_type.text = "REL"
            ServerType.DEV -> text_server_type.text = "DEV"
            ServerType.QA -> text_server_type.text = "QA"
            ServerType.SSLREL -> text_server_type.text = "SSLREL"
            ServerType.SSLDEV -> text_server_type.text = "SSLDEV"
            ServerType.SSLQA -> text_server_type.text = "SSLQA"
        }

        button_rel.setOnClickListener {
            selectRelServer()
        }

        button_dev.setOnClickListener {
           selectDevServer()
        }

        button_qa.setOnClickListener {
            selectQaServer()
        }

        button_ssl_rel.setOnClickListener {
            selectSslRelServer()
        }

        button_ssl_dev.setOnClickListener {
            selectSslDevServer()
        }

        button_ssl_qa.setOnClickListener {
            selectSslQaServer()
        }
    }

    abstract fun selectRelServer()
    abstract fun selectDevServer()
    abstract fun selectQaServer()
    abstract fun selectSslRelServer()
    abstract fun selectSslDevServer()
    abstract fun selectSslQaServer()

}

