package kr.co.didimu.ulotto.ui.login.prompt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat

class PromptUtils {
    companion object {
        fun isSupportedBio(context: Context): Boolean {
            return isSdkVersionSupported() && isHardwareSupported(context)
        }

        fun isFingerprintAvailable(context: Context): Boolean {
            if (!isSupportedBio(context)) {
                return false
            }

            val fingerprintManager = FingerprintManagerCompat.from(context)
            return fingerprintManager.hasEnrolledFingerprints()
        }

        fun isPermissionGranted(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.USE_FINGERPRINT
            ) == PackageManager.PERMISSION_GRANTED
        }

        private fun isSdkVersionSupported(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        }

        private fun isHardwareSupported(context: Context): Boolean {
            val fingerprintManager = FingerprintManagerCompat.from(context)
            return fingerprintManager.isHardwareDetected
        }

    }
}