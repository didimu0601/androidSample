package kr.co.didimu.ulotto.util

import android.content.Context
import android.text.TextUtils
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Crypto {
    companion object {
        fun getSecureKey(context: Context): SecretKey? {
            val secureKey = SharedPref.getInstance().getString(SharedPref.PREF_SECURE_KEY)
            try {
                if (TextUtils.isEmpty(secureKey)) {
                    val generator = KeyGenerator.getInstance("AES")
                    val random = SecureRandom()
                    generator.init(256, random)
                    val generateKey = generator.generateKey()
                    SharedPref.getInstance().setString(
                        SharedPref.PREF_SECURE_KEY,
                        Base64.encodeToString(generateKey.encoded, Base64.DEFAULT)
                    )
                    return generateKey
                }

                val decodedKey = Base64.decode(secureKey, Base64.DEFAULT)
                return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        //AES256 pass ivData null
        fun AESEncode(str: String, key: SecretKey?, ivData: ByteArray? = null): String? {
            try {
                val textBytes = str.toByteArray(charset("UTF-8"))
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    IvParameterSpec(ivData ?: ByteArray(cipher.blockSize))
                )
                return Base64.encodeToString(cipher.doFinal(textBytes), Base64.DEFAULT).trim()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun AESDecode(str: String, key: SecretKey?, ivData: ByteArray? = null): String? {
            if (TextUtils.isEmpty(str)) {
                return null
            }

            try {
                val textBytes = Base64.decode(str, Base64.DEFAULT)
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    IvParameterSpec(ivData ?: ByteArray(cipher.blockSize))
                )
                return String(cipher.doFinal(textBytes), Charsets.UTF_8).trim()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun SHAEncode(str: String): String? {
            if (TextUtils.isEmpty(str)) {
                return null
            }

            try {
                val bytes = str.toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                return digest.fold("", { datum, it -> datum + "%02x".format(it) })
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}