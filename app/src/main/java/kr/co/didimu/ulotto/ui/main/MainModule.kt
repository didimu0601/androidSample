package kr.co.didimu.ulotto.ui.main

import kr.co.didimu.ulotto.di.ActivityScoped
import dagger.Binds
import dagger.Module

@Module
abstract class MainModule {
    @Binds
    @ActivityScoped
    abstract fun mainPresenter(presenter: MainPresenter): Main.Presenter //Binds의 경우 하나의 파라메터만 받으며 리턴타입은 파라메터의 상위 타입
                                                                        //@provides를 통한 객체 생성 없이 인터페이스(추상클래스)의 구현체를 윗줄과 같이 추론하여 제공
}
