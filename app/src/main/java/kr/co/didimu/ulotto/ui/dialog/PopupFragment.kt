package kr.co.didimu.ulotto.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kr.co.didimu.ulotto.util.Constants
import kotlinx.android.synthetic.main.fragment_popup.view.*
import kr.co.didimu.ulotto.R
import kr.co.didimu.ulotto.util.PrintLog

class PopupFragment : Fragment() {
    private val TAG by lazy {  PopupFragment::class.java.simpleName }

    val url: String by lazy {
        arguments?.getString(Constants.POPUP_URL, "") ?: ""
    }

    override fun onResume() {
        super.onResume()
        PrintLog.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        PrintLog.d(TAG, "onPause")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        PrintLog.d(TAG, "onActivityCreated")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        PrintLog.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_popup, container, false)
        view.webview.loadUrl(url)
        view.text_back.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
        return view
    }

    companion object {
        fun newInstance(url: String): PopupFragment {
            val fragment = PopupFragment()

            val arg = Bundle()
            arg.putString(Constants.POPUP_URL, url)

            fragment.arguments = arg
            return fragment
        }
    }
}