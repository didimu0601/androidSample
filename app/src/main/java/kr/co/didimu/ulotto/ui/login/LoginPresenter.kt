package kr.co.didimu.ulotto.ui.login

import android.text.TextUtils
import kr.co.didimu.ulotto.data.dto.DeviceRequest
import kr.co.didimu.ulotto.data.dto.LoginRequest
import kr.co.didimu.ulotto.data.repository.ServiceRepository
import kr.co.didimu.ulotto.ui.login.prompt.PromptCallback
import kr.co.didimu.ulotto.ui.login.prompt.PromptLogin
import kr.co.didimu.ulotto.ui.login.prompt.PromptType
import kr.co.didimu.ulotto.util.PrintLog
import javax.inject.Inject

class LoginPresenter @Inject constructor(val repository: ServiceRepository,
                                         @PromptType("Finger")val promptLogin: PromptLogin) : Login.Presenter {

    private val TAG by lazy {  LoginPresenter::class.java.simpleName }

    private lateinit var loginView: Login.View

    override fun start(view: Login.View) {
        this.loginView = view
    }

    override fun confirmPromptDevice(deviceId: String, id: String) {
        repository.confirmPromptDevice(deviceId, id) {
            onResponse = {
                PrintLog.d(TAG, "confirmPromptDevice onResponse $it")
                loginView.checkPromptDevice(!TextUtils.isEmpty(it.body()?.resultContent?.regDate))
            }
            onFailure = {
                PrintLog.d(TAG, "confirmPromptDevice onFailure $it")
                loginView.onHttpFailure()
            }
        }
    }

    override fun registerPromptDevice(deviceId: String, id: String) {
        val device = DeviceRequest(deviceId, id)

        repository.registerPromptDevice(device.toJson()) {
            onResponse = {
                PrintLog.d(TAG, "registerPromptDevice onResponse $it")
            }
            onFailure = {
                PrintLog.d(TAG, "registerPromptDevice onFailure $it")
                loginView.onHttpFailure()
            }
        }
    }

    //20191018 psg
    override fun removePromptDevice(deviceId: String) {
        val device = DeviceRequest(deviceId,"")

        repository.removePromptDevice(device.toJson()) {
            onResponse = {
                PrintLog.d(TAG, "removePromptDevice onResponse $it")
            }
            onFailure = {
                PrintLog.d(TAG, "removePromptDevice onFailure $it")
                loginView.onHttpFailure()
            }
        }
    }

    override fun doLogin(loginInfo: LoginRequest, isPromptLogin: Boolean) {

        repository.doLogin(loginInfo.toJson()) {
            onResponse = {
                if (it.body()?.result == 0) {
                    loginView.successLogin(loginInfo, isPromptLogin) //khm TODO confirm
                } else {
                    loginView.failLogin(true)
                }
            }
            onFailure = {
                loginView.onHttpFailure()
            }
        }
    }

    override fun doPromptLogin() {
        promptLogin.authenticate(object : PromptCallback {
            override fun onSuccess() {
                loginView.successPromptLogin()
            }

            override fun onHelp(id: Int) {
                loginView.helpPromptLogin(id)
            }

            override fun onFail(id: Int) {
                loginView.failPromptLogin(id)
            }

        })

    }

    override fun cancelPromptLogin() {
        promptLogin.cancel()
    }

}