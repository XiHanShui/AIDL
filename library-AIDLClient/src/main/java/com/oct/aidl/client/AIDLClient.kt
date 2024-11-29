package com.oct.aidl.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.oct.aidl.IAIDLService
import com.oct.aidl.IResultCallback
import com.oct.aidl.Request
import com.oct.aidl.Response
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class AIDLClient(
    private val context: Context,
    private val pkg: String,
    private val action: String,

) {


    private var latch: CountDownLatch? = null

    @Volatile
    private var stub: IAIDLService? = null

    private val clientId by lazy { UUID.randomUUID().toString() }

    private val lock = ReentrantLock(true)

    private val requestCallback = hashMapOf<String, IResponseCallback>()

    @Volatile
    private var isRegister = false


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
            stub = IAIDLService.Stub.asInterface(binder)
            latch?.countDown()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            stub = null
            isRegister = false
        }
    }

    private val callback = object : IResultCallback.Stub() {

        override fun onResult(response: Response) {
            val cb = lock.withLock {
                val cb = requestCallback[response.requestId]
                requestCallback.remove(response.requestId)
                cb
            }
            cb?.callback(response)
        }

        override fun notifyResponseReady(clientId: String?) {
            TODO("Not yet implemented")
        }
    }


    @Synchronized
    private fun bindService(): Boolean {
        if (stub != null) {
            return true
        }
        if (latch?.count != 0L) {
            latch = CountDownLatch(1)
        }
        isRegister = false
        val intent = Intent(action)
        intent.`package` = pkg
        Log.e("AIDLClient", "connectService")
        var result = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        try {
            latch?.await(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        }
        return result && stub != null
    }


    fun disconnectService() {
        if (stub != null) {
            context.unbindService(connection)
        }
    }

    fun request(request: Request, cb: IResponseCallback) {
        val response = checkConnectState(request)
        if (response != null) {
            cb.callback(response)
            return
        }
        lock.withLock { requestCallback[request.requestId] = cb }
        Log.e("AIDLClient", "clientId:${clientId}")
        val l = System.currentTimeMillis()
        val result: Boolean = stub!!.asyncRequest(clientId, request)
        Log.e("AIDLClient", "asyncRequest:${System.currentTimeMillis() - l}")
        if (!result) {
            lock.withLock { requestCallback.remove(request.requestId) }
            cb.callback(Response(-1, "发送请求失败,可能是服务端未实现代码", request.requestId))
        }
    }

    fun request(request: Request): Response {
        val response = checkConnectState(request)
        if (response != null) {
            return response
        }
        return stub!!.request(request)
    }




    private fun checkConnectState(request: Request): Response? {
        var result = true
        if (stub == null) {
            val currentTimeMillis = System.currentTimeMillis()
            result = bindService()
            Log.e("AIDLClient", "bindService:${System.currentTimeMillis() - currentTimeMillis}")
        }
        if (!result) {
            return Response(-1, "连接服务失败", request.requestId)

        }
        if (!isRegister) {
            isRegister =
                stub?.registerClient(clientId, pkg, callback) ?: false
        }
        if (!isRegister) {
            return Response(-1, "注册回调失败", request.requestId)
        }
        return null
    }



}