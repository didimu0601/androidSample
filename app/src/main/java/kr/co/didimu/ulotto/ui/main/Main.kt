package kr.co.didimu.ulotto.ui.main

import kr.co.didimu.ulotto.BasePresenter
import kr.co.didimu.ulotto.BaseView

interface Main {
    interface View : BaseView<Presenter> {
        val isActive: Boolean
        fun checkVersion(isForceUpdate: Boolean = false, isUpdate: Boolean = false)
        fun checkTermsAgree()
    }

    interface Presenter : BasePresenter<View> {
        fun requestVersion(version: String)
        fun doAutoLogin(id:String, pw:String)
    }
}