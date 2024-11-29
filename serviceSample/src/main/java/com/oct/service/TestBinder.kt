package com.oct.service

import android.os.SharedMemory
import com.oct.aidl.IResultCallback
import com.oct.aidl.Request
import com.oct.aidl.Response
import com.oct.aidl.service.BaseBinder

class TestBinder : BaseBinder() {


    override fun action(): String {
      return "com.oct.test"
    }

    override fun registerSharedMenory(
        clientId: String?,
        pkg: String?,
        callback: IResultCallback?,
        sharedMemory: SharedMemory?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun request(request: Request?): Response {
       return  Response(1,"success","")
    }

    override fun asyncRequest(clientId: String, request: Request): Boolean {
        sendResult(clientId, Response(1,"success",request.requestId))
        return true
    }

    override fun notifyRequestReady(clientId: String?) {
        TODO("Not yet implemented")
    }


}