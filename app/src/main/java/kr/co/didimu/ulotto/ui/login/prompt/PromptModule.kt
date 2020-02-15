package kr.co.didimu.ulotto.ui.login.prompt

import android.content.Context
import kr.co.didimu.ulotto.di.ActivityScoped
import kr.co.didimu.ulotto.ui.login.LoginActivity
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
internal class PromptModule {
    @Provides
    @Named("context")
    @ActivityScoped
    fun provideContext(activity: LoginActivity): Context {
        return activity
    }

    @Provides
    @ActivityScoped
    @PromptType("Biometric")
    fun provideBiometricsLogin(@Named("context")context: Context): PromptLogin {
        return BiometricsPromptLogin(context)
    }

    @Provides
    @ActivityScoped
    @PromptType("Finger")
    fun provideFingerLogin(@Named("context")context: Context): PromptLogin {
        return FingerPromptLogin(context)
    }
}