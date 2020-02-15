package kr.co.didimu.ulotto.ui.login

import dagger.Binds
import dagger.Module

@Module
abstract class LoginModule {
    @Binds
    abstract fun loginPresenter(presenter: LoginPresenter): Login.Presenter //Binds의 경우 하나의 파라메터만 받으며 리턴타입은 파라메터의 상위 타입
                                                                            //@provides를 통한 객체 생성 없이(new) 인터페이스(추상클래스)의 구현체를 윗줄과 같이 추론하여 제공(@Inject constructor()
//    @Module
//    companion object { //khm why?
//        @JvmStatic
//        @Provides //@Provides는 모듈에서만 사용 가능
//        internal fun provideContext(activity: LoginActivity): Context {
//            return activity
//        }
//    }

//    @Module
//    companion object { //khm why?
//        @JvmStatic
//        @Provides //@Provides는 모듈에서만 사용 가능
//        internal fun provideLoginPresenter(): LoginPresenter {
//            return LoginPresenter()
//        }
//    }

    //@Binds와 @Provides는 본래 같은 모듈에 정의 할 수가 없어서 위와 같은 방식으로 사용해야 한다. khm why?방법에 대한 이유
}