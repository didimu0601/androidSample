package kr.co.didimu.ulotto.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import kr.co.didimu.ulotto.R
import kotlinx.android.synthetic.main.dialog_custom.*


class CustomAlertDialog(context: Context) : Dialog(context) {

    private var title: String? = null
    private var msg: String? = null
    private var positiveText: String? = null
    private var negativeText: String? = null
    private var isCancel: Boolean = true
    private var positiveClick: (() -> Unit)? = null
    private var negativeClick: (() -> Unit)? = null
    private var cancelClick: (() -> Unit)? = null


    constructor(context: Context, title: String?, msg: String?,
                positive: String?, positiveClick: (() -> Unit)?,  negative: String?, negativeClick: (()-> Unit)?, isCancel: Boolean, cancelClick: (() -> Unit)?)
            : this(context) {
        this.title = title
        this.msg = msg
        this.positiveText = positive
        this.negativeText = negative
        this.positiveClick = positiveClick
        this.negativeClick = negativeClick
        this.isCancel = isCancel
        this.cancelClick = cancelClick
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_custom)

//        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        val width = (context.resources.displayMetrics.widthPixels * 0.80).toInt()
        window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        setTitleText()
        setMessageText()
        setPositiveButton()
        setNegativeButton()

        setCancelable(isCancel)
        setOnCancelListener { cancelClick?.invoke() }
    }

    private fun setTitleText() {
        if (TextUtils.isEmpty(title)) {
            return
        }

        findViewById<TextView>(R.id.text_title).apply {
            visibility = View.VISIBLE
            text = title
        }
    }

    private fun setMessageText() {
        if (TextUtils.isEmpty(msg)) {
            return
        }

        findViewById<TextView>(R.id.text_msg).apply {
            text = msg

            //20191107 psg left gravity on help
            val tStr: String? = msg
            if(tStr != null) {
                if (tStr.contains("전효민")) {
                    gravity = Gravity.LEFT
                } else {
                    gravity = Gravity.CENTER
                }
            }
        }
    }

    private fun setPositiveButton() {
        if (TextUtils.isEmpty(positiveText)) {
            return
        }

        button_positive.visibility = View.VISIBLE
        button_positive.text = positiveText
        button_positive.setOnClickListener {
            dismiss()
            positiveClick?.invoke()
        }
    }

    private fun setNegativeButton() {
        if (TextUtils.isEmpty(negativeText)) {
            return
        }

        button_negative.visibility = View.VISIBLE
        button_negative.text = negativeText
        button_negative.setOnClickListener { //possible lambdas SAM and define in java
            dismiss()
            negativeClick?.invoke()
        }
    }

    class Builder(val context: Context) {
        private var title: String? = null
        private var msg: String? = null
        private var positiveText: String? = null
        private var negativeText: String? = null
        private var isCancel: Boolean = false
        private var positiveClick: (() -> Unit)? = null
        private var negativeClick: (() -> Unit)? = null
        private var cancelClick: (() -> Unit)? = null

        fun setTitle(title: String? = null, titleId: Int = 0) {
            this.title = title ?: context.getString(titleId)
        }

        fun setMsg(msg: String? = null, msgId: Int = 0) {
            this.msg = msg ?: context.getString(msgId)
        }

        fun setPositiveButton(positiveText: String? = null, positiveId: Int = android.R.string.ok, positiveClick: (() -> Unit)? = null) {
            this.positiveText = positiveText ?: context.getString(positiveId)
            this.positiveClick = positiveClick
        }


        fun setNegativeButton(negativeText: String? = null, negativeId: Int = android.R.string.cancel, negativeClick: (() -> Unit)? = null) {
            this.negativeText = negativeText ?: context.getString(negativeId)
            this.negativeClick = negativeClick
        }

        fun setCancelable(isCancel: Boolean, cancelClick: () -> Unit) {
            this.isCancel = isCancel
            this.cancelClick = cancelClick
        }

        fun build() {
            val dialog = CustomAlertDialog(context, title, msg, positiveText, positiveClick, negativeText, negativeClick, isCancel, cancelClick)
            dialog.show()
        }
    }

}