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
import com.oct.aidl.KLog
import com.oct.aidl.Request
import com.oct.aidl.Response
import com.oct.aidl.ResponseCode
import java.io.File
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class AIDLClient(val action: String, private val serverPkg: String) {


    private var latch: CountDownLatch? = null

    @Volatile
    private var stub: ICommonService? = null

    private val clientId by lazy { UUID.randomUUID().toString() }

    private val lock = ReentrantLock(true)

    private val requestCallbackCache = hashMapOf<String, IResponseCallback>()

    @Volatile
    private var service2Client: IServiceCallback? = null

    /**
     * 是否已注册 请求回调
     */
    @Volatile
    private var isRegisterClientListener = false

    /**
     * 是否已注册 服务端主动给到客户端监听
     */
    @Volatile
    private var isRegisterServiceListener = false

    private val pkg by lazy { ClientManager.instance.context.packageName }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
            stub = ICommonService.Stub.asInterface(binder)
            latch?.countDown()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isRegisterClientListener = false
            isRegisterServiceListener = false
            stub = null
            KLog.w(
                """
                service disconnected
                clientId: $clientId
                action: $action
                serverPkg :$serverPkg
            """.trimIndent()
            )
            lock.withLock {
                for (cb in requestCallbackCache.values) {
                    cb.callback(Response(ResponseCode.SERVER_DISCONNECTED, "", cb.requestId))
                }
                requestCallbackCache.clear()
            }
        }
    }

    private val requestListener by lazy {
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

    private val serviceListener by lazy {
        object : IService2Client.Stub() {
            override fun service2Client(action: String, data: String?) {
                service2Client?.service2Client(action, data)
            }
        }
    }


    private fun bindService(): Boolean {
        if (stub != null) {
            return true
        }
        if (latch?.count != 0L) {
            latch = CountDownLatch(1)
        }
        isRegisterClientListener = false
        var result: Boolean
        try {
            KLog.d(
                """        
            bindService
            action: $action
            serverPackage: $serverPkg
        """.trimIndent()
            )
            // 绑定服务
            result = ClientManager.instance.context.bindService(Intent(action).apply {
                `package` = serverPkg
            }, connection, Context.BIND_AUTO_CREATE)
            // 如果12s 内没有绑定成功,则返回false
            latch?.await(12, TimeUnit.SECONDS)
        } catch (e: Exception) {
            KLog.e("bindService error---------")
            e.printStackTrace()
            result = false
        }
        return result && stub != null
    }


    /**
     *  解绑服务
     */
    fun unbindService() {
        KLog.d(
            """
                unbindService
                action: $action
                serverPackage: $serverPkg
            """.trimIndent()
        )
        if (stub != null) {
            ClientManager.instance.context.unbindService(connection)
        }
    }

    /**
     * 注册服务监听
     */
    fun registerService2ClientListener(callback: IServiceCallback): Response {
        KLog.d("registerService2ClientCallback")
        this.service2Client = null
        this.service2Client = callback
        return if (!isRegisterServiceListener) {
            val request = Request(params = "")
            val response = checkServiceBindingStatus()
            val result = if (response == ResponseCode.SUCCESS) {
                stub?.registerService2Client(clientId, pkg, serviceListener) ?: false
            } else {
                return Response(response, "", request.requestId)
            }
            isRegisterServiceListener = result
            if (result) {
                Response(ResponseCode.SUCCESS, "", request.requestId)
            } else {
                Response(-1, "连接失败", request.requestId)
            }
        } else {
            Response(0, "", Request(params = "").requestId)
        }
    }


    fun request(request: Request, cb: IResponseCallback) {
        // 检查服务端连接状态
        val response = checkServiceBindingStatus()
        if (response != ResponseCode.SUCCESS) {
            cb.callback(Response(response, "", request.requestId))
            return
        }
        KLog.d(
            """
            startRequest :${request}
            action: $action
            serverPackage: $serverPkg
        """.trimIndent()
        )

        cb.requestId = request.requestId
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
        val response = checkServiceBindingStatus()
        if (response != ResponseCode.SUCCESS) {
            return Response(response, "", request.requestId)
        }

        KLog.d(
            """
            startRequest :${request}
            action: $action
            serverPackage: $serverPkg
        """.trimIndent()
        )

        val result = stub?.request(request) ?: Response(ResponseCode.SERVER_DISCONNECTED, "", request.requestId)
        KLog.d("endRequest :$result")
        return result

    }

    /**
     * 检查服务绑定状态
     */
    @Synchronized
    private fun checkServiceBindingStatus(isCheckRequestListener: Boolean = false): Int {
        val result = if (stub == null) {
            val currentTimeMillis = System.currentTimeMillis()

            val bindResult = bindService()
            KLog.d(
                """
                bindServiceResult:$bindResult
                bindServiceDuration:${System.currentTimeMillis() - currentTimeMillis}ms
                action: $action
                serverPackage: $serverPkg
            """.trimIndent()
            )
            bindResult
        } else {
            false
        }
        return if (result) {
            // 检查是否已经注册或者是否需要注册
            if (!isRegisterClientListener && isCheckRequestListener) {
                isRegisterClientListener =
                    stub?.registerClient(clientId, pkg, requestListener) ?: false
                KLog.d(
                    """
                注册客户端监听器结果：$isRegisterClientListener
                action: $action
                serverPackage: $serverPkg    
                """.trimIndent()
                )
            }
            if (!isRegisterClientListener && isCheckRequestListener) ResponseCode.REGISTER_ERROR else ResponseCode.SUCCESS
        } else {
            ResponseCode.SERVER_NO_FOND
        }
    }


    fun transferringFile(path: String): Response {
        val result = checkServiceBindingStatus()
        if (result != ResponseCode.SUCCESS) {
            return Response(result, "", "")
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


    fun isRegisterServiceListener(): Boolean {
        return service2Client != null && isRegisterServiceListener
    }

    fun isRegisterClientListener(): Boolean {
        return stub != null && isRegisterClientListener
    }


}