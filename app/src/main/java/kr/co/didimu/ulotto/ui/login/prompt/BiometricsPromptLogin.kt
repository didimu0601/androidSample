package kr.co.didimu.ulotto.ui.login.prompt

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kr.co.didimu.ulotto.R
import kr.co.didimu.ulotto.util.Constants
import kr.co.didimu.ulotto.util.PrintLog
import java.util.concurrent.Executors

class BiometricsPromptLogin constructor(val context: Context) : PromptLogin {
    private val TAG by lazy {  BiometricsPromptLogin::class.java.simpleName }

    private var biometricPrompt: BiometricPrompt? = null

    override fun authenticate(callback: PromptCallback) {
        val executor = Executors.newSingleThreadExecutor()

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.title_login))
            .setSubtitle(context.getString(R.string.msg_fingerprint))
            .setDescription(context.getString(R.string.touch_fingerprint))
            .setNegativeButtonText(context.getString(R.string.action_cancel))
            .build()

        biometricPrompt = BiometricPrompt(context as FragmentActivity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                PrintLog.d(TAG, "onAuthenticationError $errorCode $errString")
                callback.onFail(errorCode)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                PrintLog.d(TAG, "onAuthenticationSucceeded")
                callback.onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                PrintLog.d(TAG, "onAuthenticationFailed")
                callback.onFail(Constants.NO_ERRORCODE)
            }
        })
        biometricPrompt?.authenticate(promptInfo)
    }

    override fun cancel() {
        biometricPrompt?.cancelAuthentication()
        biometricPrompt = null
    }
}