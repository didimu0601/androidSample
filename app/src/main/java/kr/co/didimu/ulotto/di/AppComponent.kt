package kr.co.didimu.ulotto.di

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import kr.co.didimu.ulotto.HybridApp
import kr.co.didimu.ulotto.data.repository.ServiceRepository
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        ActivityBindingModule::class]
)

interface AppComponent {

    fun getRepository(): ServiceRepository

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: HybridApp): Builder
        fun build(): AppComponent
    }

    fun inject(hybridApp: HybridApp)
}
