package kr.co.didimu.ulotto.util

import android.content.Context
import android.os.Handler
import android.os.Message
import java.lang.ref.WeakReference


class MessageHandler(context: Context?, callback: Callback?) : Handler() {
    private val contextRef: WeakReference<Context?> = WeakReference(context)
    private val callbackRef: WeakReference<Callback?> = WeakReference(callback)

    override fun handleMessage(msg: Message?) {
        super.handleMessage(msg)

        if (contextRef.get() != null) {
            callbackRef.get()?.handleMessage(msg)
        }
    }
}