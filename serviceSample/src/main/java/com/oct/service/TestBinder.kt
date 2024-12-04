package com.oct.service

import android.os.ParcelFileDescriptor
import android.os.SharedMemory
import com.oct.aidl.IRequestCallback
import com.oct.aidl.IService2Client

import com.oct.aidl.Request
import com.oct.aidl.Response
import com.oct.aidl.service.BaseBinder

class TestBinder : BaseBinder() {


    override fun action(): String {
      return "com.oct.test"
    }

    override fun registerService2Client(
        clientId: String?,
        pkg: String?,
        callback: IService2Client?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun unregisterService2Client(clientId: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun registerSharedMenory(
        clientId: String?,
        pkg: String?,
        callback: IRequestCallback?,
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

    override fun transferringFile(pfd: ParcelFileDescriptor) {
        TODO("Not yet implemented")
    }

    override fun transferringBigData(clientId: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun transferringImg(clientId: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun notifyRequestReady(clientId: String?) {
        TODO("Not yet implemented")
    }


}