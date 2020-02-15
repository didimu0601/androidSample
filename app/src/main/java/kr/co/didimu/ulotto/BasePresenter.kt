package kr.co.didimu.ulotto

interface BasePresenter<T> {
    fun start(view: T)
}