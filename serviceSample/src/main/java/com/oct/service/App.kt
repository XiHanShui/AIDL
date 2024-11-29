package com.oct.service

import android.app.Application
import com.oct.aidl.service.BinderPoolManager

class App :Application() {

    override fun onCreate() {
        super.onCreate()
        BinderPoolManager.registerBinder(TestBinder())
    }
}