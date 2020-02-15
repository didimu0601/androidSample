package kr.co.didimu.ulotto.ui.login.prompt

interface PromptLogin {
    fun authenticate(callback: PromptCallback)
    fun cancel()
}