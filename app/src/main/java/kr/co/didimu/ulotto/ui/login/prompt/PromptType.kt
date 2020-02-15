package kr.co.didimu.ulotto.ui.login.prompt

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
annotation class PromptType(val value: String = "")