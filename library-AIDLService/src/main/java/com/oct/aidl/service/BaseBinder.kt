package com.oct.aidl.service

import android.util.Log
import com.oct.aidl.IRequestCallback
import com.oct.aidl.Response
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class BaseBinder : IBinder() {

    private val callbackCache = HashMap<String, IRequestCallback>()

    private val lock = ReentrantLock(true)


    override fun registerClient(clientId: String, pkg: String, callback: IRequestCallback): Boolean {
        Log.e("BaseBinder", "clientId:${clientId}")
        lock.withLock {
            callbackCache[clientId] = callback
        }
        return true
    }

    override fun unregisterClient(clientId: String): Boolean {
        lock.withLock { callbackCache.remove(clientId) }
        return true
    }


    /**
     * 判断是否准备好了
     */
    override fun isResultReady(clientId: String?): Boolean {
        return lock.withLock { callbackCache.containsKey(clientId) }
    }


    /**
     * 发送结果
     */
    fun sendResult(clientId: String, response: Response) {
        val cb = lock.withLock { callbackCache[clientId] }
        try {
            cb?.onResult(response)
        } catch (e: Exception) {
            e.printStackTrace()
            lock.withLock { callbackCache.remove(clientId) }
        }
    }

    open fun coverFile(path:String,){}




}