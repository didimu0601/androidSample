package kr.co.didimu.ulotto.ui.main

import kr.co.didimu.ulotto.data.dto.LoginRequest
import kr.co.didimu.ulotto.data.repository.ServiceRepository
import kr.co.didimu.ulotto.util.PrintLog
import javax.inject.Inject

//khm constructor injection: the client to provide a parameter in a constructor for the dependency.
class MainPresenter @Inject constructor(val repository: ServiceRepository): Main.Presenter {

    private val TAG by lazy {  MainPresenter::class.java.simpleName }

    private lateinit var mainView: Main.View

    override fun start(view: Main.View) {
        mainView = view
    }

    override fun requestVersion(version: String) {
        repository.requestVersion(version) {
            onResponse = response@{
                PrintLog.d(TAG, "requestVersion success $it")
                PrintLog.d(TAG, "requestVersion success $it")
                val newVerStr : String  =it.body()?.appUpdateInfo?.android_new ?: ""
                val fUpdateVerStr : String  =it.body()?.appUpdateInfo?.android_fupdate ?: ""

                val tNewVer= getVersion(newVerStr)
                val tFupdateVer= getVersion(fUpdateVerStr)

                val tCurVer = getVersion(version)

                val forceUpdate : Boolean = if(tCurVer <= tFupdateVer) true else false

                val needUpdate : Boolean = if(tCurVer < tNewVer)  true else false
// 실제 버전 체크시 : 테스트는 그냥 넘어감
//                if(!forceUpdate  && !needUpdate) {
//                    mainView.checkVersion()
//                    return@response
//                }
//
//                if (forceUpdate) {
//                    mainView.checkVersion(true)
//                } else {
//                    if (!needUpdate) {
//                        mainView.checkVersion()
//                    } else {
//                        mainView.checkVersion(isUpdate = true)
//                    }
//                }
                mainView.checkVersion() // test code
            }
            onFailure = {
                PrintLog.d(TAG, "requestVersion fail $it")
                mainView.onHttpFailure()
            }
        }
    }

    override fun doAutoLogin(id: String, pw:String) {
        val login = LoginRequest(id, pw, "1.0.0", "1.0.1")

        repository.doLogin(login.toJson()) {
            onResponse = {
                PrintLog.d(TAG, "doAutoLogin success $it")
                //go main page
            }
            onFailure = {
                PrintLog.d(TAG, "doAutoLogin success $it")
                mainView.checkTermsAgree()
            }
        }
    }

    private fun getVersion(version: String): Int {
        return version.replace(".", "").toInt()
    }
}