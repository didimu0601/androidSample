package kr.co.didimu.ulotto.ui.settings

import kr.co.didimu.ulotto.BasePresenter
import kr.co.didimu.ulotto.BaseView

interface Settings {

    interface View : BaseView<Presenter> {
        val isActive: Boolean
    }

    interface Presenter : BasePresenter<View>
}
