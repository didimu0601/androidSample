package kr.co.didimu.ulotto.util

import android.os.Build
import android.os.Environment
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class RootingChecker {
    companion object {
        private val ROOT_PATH = Environment.getExternalStorageDirectory().toString() + ""
        private val RootFilesPath = listOf(
            "$ROOT_PATH/system/bin/su",
            "$ROOT_PATH/system/xbin/su",
            "$ROOT_PATH/system/app/SuperUser.apk",
            "$ROOT_PATH/data/data/com.noshufou.android.su")

        fun isRooting(): Boolean {
            var isRootingFlag: Boolean
            var process: Process? = null
            try {
                process = Runtime.getRuntime().exec("su")
                isRootingFlag = true
            } catch (var2: Exception) {
                isRootingFlag = false
            } finally {
                process?.destroy()
            }

            if (isRootingFlag) {
                return true
            }

            isRootingFlag = checkRootingFiles(createFiles(RootFilesPath))

            if (!isRootingFlag) {
                isRootingFlag = isDeviceRooted()
            }

            return isRootingFlag
        }

        private fun createFiles(pathList: List<String>): List<File> {
            val rootingFiles = mutableListOf<File>()

            for (i in 0 until pathList.size) {
                rootingFiles.add(File(pathList[i]))
            }

            return rootingFiles
        }

        private fun checkRootingFiles(fileList: List<File>): Boolean {
            var result = false
            val var3 = fileList.size

            for (var4 in 0 until var3) {
                val f = fileList[var4]
                if (f.exists() && f.isFile) {
                    result = true
                    break
                }

                result = false
            }

            return result
        }

        private fun isDeviceRooted(): Boolean {
            return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
        }

        private fun checkRootMethod1(): Boolean {
            val buildTags = Build.TAGS
            return buildTags != null && buildTags.contains("test-keys")
        }

        private fun checkRootMethod2(): Boolean {
            val paths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/SYSTEM/APP/SUPERUSER.APK",
                "/SBIN/SU",
                "/SYSTEM/BIN/SU",
                "/SYSTEM/XBIN/SU",
                "/DATA/LOCAL/XBIN/SU",
                "/DATA/LOCAL/BIN/SU",
                "/SYSTEM/SD/XBIN/SU",
                "/SYSTEM/BIN/FAILSAFE/SU",
                "/DATA/LOCAL/SU"
            )
            val var2 = paths.size

            for (var3 in 0 until var2) {
                val path = paths[var3]
                if (File(path).exists()) {
                    return true
                }
            }

            return false
        }

        private fun checkRootMethod3(): Boolean {
            var process: Process? = null

            var var2: Boolean
            try {
                process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
                val t = BufferedReader(InputStreamReader(process.inputStream))
                var2 = t.readLine() != null
                return var2
            } catch (var6: Throwable) {
                var2 = false
            } finally {
                process?.destroy()

            }

            return var2
        }
    }
}