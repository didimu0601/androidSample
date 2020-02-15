package kr.co.didimu.ulotto

import android.app.Activity
import android.app.Application
import android.content.Context
import com.kakao.auth.KakaoSDK
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import kr.co.didimu.ulotto.util.PrintLog
import kr.co.didimu.ulotto.util.SharedPref
import javax.inject.Inject


class HybridApp : Application(), HasActivityInjector {
    val TAG by lazy { HybridApp::class.java.simpleName }

//    private var sApplication: Application? = null
//


    companion object {
        var sApplication: Application? = null

        @JvmStatic
        fun getApplication(): Application? {
            return sApplication
        }

        @JvmStatic
        fun getContext(): Context {
            return getApplication()!!.applicationContext
        }
    }


    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity> //=>Inject시 ActivityBindingModule내에 있는 액티비티들을 바인딩

    override fun onCreate() {
        super.onCreate()
        PrintLog.d(TAG, "onCreate")
        sApplication = this

        KakaoSDK.init(KakaoSDKAdapter())

        SharedPref.initialize(applicationContext)

        kr.co.didimu.ulotto.di.DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingAndroidInjector
}