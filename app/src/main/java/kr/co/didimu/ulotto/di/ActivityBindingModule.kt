package kr.co.didimu.ulotto.di

import kr.co.didimu.ulotto.ui.login.LoginActivity
import kr.co.didimu.ulotto.ui.login.LoginModule
import kr.co.didimu.ulotto.ui.login.prompt.PromptModule
import kr.co.didimu.ulotto.ui.main.MainActivity
import kr.co.didimu.ulotto.ui.main.MainModule
import kr.co.didimu.ulotto.ui.settings.SettingsActivity
import kr.co.didimu.ulotto.ui.settings.SettingsModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = [MainModule::class]) //ContributesAndroidInjector는 Module(여기선 ActivityBindingModule)에서 각 Activity별 subComponent생성
    abstract fun mainActivity(): MainActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [LoginModule::class, PromptModule::class]) //modules는 생성된 subComponent와 연결할 모듈을 정의
    abstract fun loginActivity(): LoginActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [SettingsModule::class])
    abstract fun settingsActivity(): SettingsActivity
}