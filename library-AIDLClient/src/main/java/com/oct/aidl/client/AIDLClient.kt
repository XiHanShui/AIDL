package com.oct.aidl.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import com.oct.aidl.ICommonService
import com.oct.aidl.IRequestCallback
import com.oct.aidl.IService2Client
import com.oct.aidl.Request
import com.oct.aidl.Response
import java.io.File
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class AIDLClient(private val context: Context, private val pkg: String, private val action: String) {


    private var latch: CountDownLatch? = null

    @Volatile
    private var stub: ICommonService? = null

    private val clientId by lazy { UUID.randomUUID().toString() }

    private val lock = ReentrantLock(true)

    private val requestCallbackCache = hashMapOf<String, IResponseCallback>()

    @Volatile
    private var service2Client: IServiceCallback? = null

    @Volatile
    private var isRegister = false

    @Volatile
    private var isRegisterServiceCallback = false


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
            stub = ICommonService.Stub.asInterface(binder)
            latch?.countDown()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            stub = null
            isRegister = false
            lock.withLock {
                for (cb in requestCallbackCache.values) {
                    cb.callback(Response(-1, "service disconnected", cb.requestId))
                }
                requestCallbackCache.clear()
            }
        }
    }

    private val requestCb by lazy {
        object : IRequestCallback.Stub() {
            override fun onResult(response: Response) {
                val cb = lock.withLock {
                    val cb = requestCallbackCache[response.requestId]
                    requestCallbackCache.remove(response.requestId)
                    cb
                }
                cb?.callback(response)
            }
        }
    }

    private val service2ClientCb by lazy {
        object : IService2Client.Stub() {
            override fun service2Client(action: String, data: String?) {
                service2Client?.service2Client(action, data)
            }

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
            latch?.await(12, TimeUnit.SECONDS)
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

    fun registerService2ClientCallback(callback: IServiceCallback): Response {
        this.service2Client = null
        this.service2Client = callback
        return if (!isRegisterServiceCallback) {
            val request = Request(params = "")
            val response = checkConnectState(request)
            val result = if (response.code == 0) {
                stub?.registerService2Client(clientId, pkg, service2ClientCb) ?: false
            } else {
                return response
            }
            isRegisterServiceCallback=result
            if (result) {
                Response(0, "连接成功", request.requestId)
            } else {
                Response(-1, "连接失败", request.requestId)
            }
        } else {
            Response(0, "已连接", Request(params = "").requestId)
        }
    }


    fun request(request: Request, cb: IResponseCallback) {
        val response = checkConnectState(request)
        cb.requestId = request.requestId
        if (response.code != 0) {
            cb.callback(response)
            return
        }
        lock.withLock { requestCallbackCache[request.requestId] = cb }
        Log.e("AIDLClient", "clientId:${clientId}")
        val l = System.currentTimeMillis()
        val result = stub?.asyncRequest(clientId, request)
        Log.e("AIDLClient", "asyncRequest:${System.currentTimeMillis() - l}")
        if (result != true) {
            val callback = lock.withLock { requestCallbackCache.remove(request.requestId) }
            callback?.callback(
                Response(
                    -1,
                    "The request failed to be sent, maybe the server did not implement the code",
                    request.requestId
                )
            )
        }
    }

    /**
     * 发送参数请求
     */
    fun request(request: Request): Response {
        val response = checkConnectState(request)
        if (response.code != 0) {
            return response
        }
        return stub?.request(request) ?: Response(
            -1,
            "The request failed to be sent, maybe the server did not implement the code",
            request.requestId
        )
    }

    /**
     * 校验连接状态
     *
     */
    private fun checkConnectState(request: Request, isCheckRequest: Boolean = false): Response {
        var result = true
        if (stub == null) {
            val currentTimeMillis = System.currentTimeMillis()
            result = bindService()
            Log.e("AIDLClient", "bindService:${System.currentTimeMillis() - currentTimeMillis}")
        }
        if (!result) {
            return Response(-1, "Server connection failed", request.requestId)
        }
        if (!isRegister && isCheckRequest) {
            isRegister = stub?.registerClient(clientId, pkg, requestCb) ?: false
        }
        if (!isRegister && isCheckRequest) {
            return Response(-1, "Callback function registration failed", request.requestId)
        }
        return Response(0, "success", request.requestId)
    }


    private fun transferringFile(path: String): Response {
        val response = checkConnectState(Request(params = "transferringFile"))
        if (response.code != 0) {
            return response
        }
        var pfd: ParcelFileDescriptor? = null
        try {
            pfd = ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
            stub!!.transferringFile(pfd)
            return Response(1, "", "")
        } catch (e: Exception) {
            e.printStackTrace()
            return Response(-1, "File transfer failed", "")
        } finally {
            pfd?.close()
        }
    }

    fun isRegisterService2ClientCallback(): Boolean {
        return service2Client != null && isRegisterServiceCallback
    }


}