package com.oct.aidl.service


import com.oct.aidl.ICommonService

abstract class IBinder : ICommonService.Stub() {

    abstract fun action(): String

}