package com.oct.aidl.client

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class CommonServiceConnection(private val binderCallback: (IBinder?) -> Unit) : ServiceConnection {

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder) {
        binderCallback.invoke(p1)
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        binderCallback.invoke(null)
    }
}