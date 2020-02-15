package kr.co.didimu.ulotto.ui.login.prompt

interface PromptCallback {
    fun onSuccess()
    fun onHelp(id: Int)
    fun onFail(id: Int)
}