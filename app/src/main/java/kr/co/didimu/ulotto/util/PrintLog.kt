package kr.co.didimu.ulotto.util

import android.util.Log

class PrintLog {
    companion object {
        fun i(tag: String, msg: String) {

                Log.i(tag, msg)

        }

        fun d(tag: String, msg: String) {

                Log.e(tag, msg)

        }

        fun e(tag: String, msg: String) {

                Log.e(tag, msg)

        }
    }
}