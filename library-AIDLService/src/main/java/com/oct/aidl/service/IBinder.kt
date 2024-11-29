package com.oct.aidl.service

import com.oct.aidl.IAIDLService

abstract class IBinder : IAIDLService.Stub() {

    abstract fun action(): String

}