package com.oct.aidl.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AIDLShareMemoryService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        val action = intent.action ?: return null
        return BinderPoolManager.getBinder(action)
    }
}