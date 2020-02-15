package kr.co.didimu.ulotto.ui.login.prompt

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import kr.co.didimu.ulotto.util.Constants
import kr.co.didimu.ulotto.util.PrintLog
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

@TargetApi(Build.VERSION_CODES.M)
class FingerPromptLogin constructor(val context: Context): PromptLogin {
    private val TAG by lazy {  FingerPromptLogin::class.java.simpleName }

    private val ERROR_TEMPOLARYLOCK = 7
    private val ERROR_PERMANENTLOCK = 9

    private val keyName: String = UUID.randomUUID().toString()
    private var cancellationSignal: CancellationSignal? = null

    private lateinit var cipher: Cipher
    private var keyStore: KeyStore? = null

//    private var dialog: FingerPromptLoginDialog? = null

    override fun authenticate(callback: PromptCallback) {
        generateKey()
        PrintLog.d(TAG, "authenticate")

        if (initCipher()) {
            val cryptoObject = FingerprintManagerCompat.CryptoObject(cipher)
            cancellationSignal = CancellationSignal()

            val fingerprintManagerCompat = FingerprintManagerCompat.from(context)

            fingerprintManagerCompat.authenticate(cryptoObject, 0, cancellationSignal,
                object : FingerprintManagerCompat.AuthenticationCallback() {
                    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errMsgId, errString)
                        PrintLog.d(TAG, "onAuthenticationError $errMsgId $errString")
                        callback.onFail(errMsgId)
                    }

                    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
                        super.onAuthenticationHelp(helpMsgId, helpString)
                        PrintLog.d(TAG, "onAuthenticationHelp $helpMsgId $helpString")
                        callback.onHelp(helpMsgId)
                    }

                    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        PrintLog.d(TAG, "onAuthenticationSucceeded")
                        callback.onSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        PrintLog.d(TAG, "onAuthenticationFailed")
                        callback.onFail(Constants.NO_ERRORCODE)
                    }
                }, null)
        }
    }

    override fun cancel() {
        cancellationSignal?.cancel()
        cancellationSignal = null
    }

    private fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore?.load(null)

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(
                KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()
            )
            keyGenerator.generateKey()
        } catch (exc: KeyStoreException) {
            exc.printStackTrace()
        } catch (exc: NoSuchAlgorithmException) {
            exc.printStackTrace()
        } catch (exc: NoSuchProviderException) {
            exc.printStackTrace()
        } catch (exc: InvalidAlgorithmParameterException) {
            exc.printStackTrace()
        } catch (exc: CertificateException) {
            exc.printStackTrace()
        } catch (exc: IOException) {
            exc.printStackTrace()
        }
    }
    
    private fun initCipher(): Boolean {
        try {
            cipher = Cipher.getInstance(
        KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )
        } catch (e:NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e:NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }
        try {
            keyStore?.load(null)
            val key = keyStore?.getKey(keyName, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }
    }
}