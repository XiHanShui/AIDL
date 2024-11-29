package com.oct.aidl.service

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 使用binder池管理binder
 * @author oct
 * @date 2021/02/28
 */
object BinderPoolManager {

    private val lock = ReentrantLock(true)

    private val binderPool = HashMap<String, IBinder>()

    /**
     * 注册binder
     */
    fun registerBinder(binder: IBinder) {
        lock.withLock { binderPool[binder.action()] = binder }
    }

    /**
     * 获取binder
     */
    fun getBinder(action: String): IBinder? {
        return lock.withLock { binderPool[action] }
    }

    /**
     * 解绑
     */
    fun unRegisterBinder(binder: IBinder) {
        lock.withLock { binderPool.remove(binder.action()) }
    }

}