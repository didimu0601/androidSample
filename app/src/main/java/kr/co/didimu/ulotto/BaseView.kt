package kr.co.didimu.ulotto

interface BaseView<T> {
    var presenter: T
    fun onHttpFailure()
}