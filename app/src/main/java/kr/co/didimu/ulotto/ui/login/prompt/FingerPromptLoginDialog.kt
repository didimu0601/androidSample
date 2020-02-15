package kr.co.didimu.ulotto.ui.login.prompt

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kr.co.didimu.ulotto.R
import kr.co.didimu.ulotto.util.Utils
import kotlinx.android.synthetic.main.dialog_fingerprint_login.view.*

class FingerPromptLoginDialog : DialogFragment() {

    private lateinit var textStatus: TextView

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Override
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.dialog_fingerprint_login, container, false)

        val window = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.requestFeature(Window.FEATURE_NO_TITLE)

        with(rootView) {
            textStatus = findViewById(R.id.text_login_status)
            text_cancel.setOnClickListener{ }

        }

        return rootView
    }

    override fun onResume() {
        val window = dialog?.window

        super.onResume()

        window?.let {
            val display = it.windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            val width = metrics.widthPixels - Utils.pixelFromDp(context, 15f) * 2
            val params = it.attributes
            params.width = width
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            params.gravity = Gravity.TOP
            params.dimAmount = 0.5f
            params.y = Utils.pixelFromDp(context, 93f)
            it.attributes = params
        }
    }

    companion object {
        fun newInstance() = FingerPromptLoginDialog
    }
}