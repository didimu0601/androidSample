package kr.co.didimu.ulotto.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import kr.co.didimu.ulotto.R
import kotlinx.android.synthetic.main.dialog_terms_info.view.*
import kotlinx.android.synthetic.main.layout_permission_info.view.*
import kotlinx.android.synthetic.main.layout_privacy_info.view.*

class TermsInfoDialog : DialogFragment() {
    private var listener: DialogSelectListener? = null

    companion object {
        fun newInstance(listener: DialogSelectListener): TermsInfoDialog {
            val fragment = TermsInfoDialog()
            fragment.setOnDateSelectListener(listener)
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_terms_info, container, false)

        isCancelable = false

        rootView.button_next.setOnClickListener {
            setLayout(rootView, false)
        }

        rootView.button_agree.setOnClickListener {
            listener?.select()
            dismissAllowingStateLoss()
        }

        rootView.button_disagree.setOnClickListener {
            listener?.select(false)
        }

        setLayout(rootView, true)

        return rootView
    }

    private fun setLayout(view: View, isPermissionMode: Boolean) {
        if (isPermissionMode) {
            view.layout_permission.visibility = View.VISIBLE
            view.layout_privacy.visibility = View.GONE
            view.text_permission_info.text = HtmlCompat.fromHtml(getString(R.string.html_permission_info), HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            view.layout_permission.visibility = View.GONE
            view.layout_privacy.visibility = View.VISIBLE
            view.text_privacy_info.text = HtmlCompat.fromHtml(getString(R.string.html_privacy_info), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }

    fun setOnDateSelectListener(listener: DialogSelectListener) {
        this.listener = listener
    }
}