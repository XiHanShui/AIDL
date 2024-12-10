package com.oct.aidl

import android.util.Log

/**
 * @author WJ
 * @date 2022/9/13
 * @description  日志打印
 */
object KLog {


    private var isDebug = false

    private var tag: String = "AIDL"

    private const val DEFAULT_MESSAGE = "execute"
    private const val V = 0x1
    private const val D = 0x2
    private const val I = 0x3
    private const val W = 0x4
    private const val E = 0x5
    private const val A = 0x6

    @JvmStatic
    fun init(isDebug: Boolean, tag: String = this.tag) {
        KLog.isDebug = isDebug
        this.tag = tag
    }

    @JvmStatic
    fun v(msg: Any? = DEFAULT_MESSAGE) {
        printLog(V,  msg)
    }

    @JvmStatic
    fun d(msg: Any? = DEFAULT_MESSAGE) {
        printLog(D,  msg)
    }

    @JvmStatic
    fun i(msg: Any? = DEFAULT_MESSAGE) {
        printLog(I,  msg)
    }

    @JvmStatic
    fun w(msg: Any? = DEFAULT_MESSAGE) {
        printLog(W,  msg)
    }

    @JvmStatic
    fun e(msg: Any? = DEFAULT_MESSAGE) {
        printLog(E,  msg)
    }

    @JvmStatic
    fun a(msg: Any? = DEFAULT_MESSAGE) {
        printLog(A,  msg)
    }


    private fun printLog(type: Int,  objectMsg: Any?) {
        if (!isDebug) {
            return
        }
        val stackTrace = Thread.currentThread().stackTrace
        val index = 5
        val className = stackTrace[index].fileName
        var methodName = stackTrace[index].methodName
        val lineNumber = stackTrace[index].lineNumber
        methodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1)
        val stringBuilder = StringBuilder()
        stringBuilder.append("[ (").append(className).append(":").append(lineNumber).append(")#")
            .append(methodName).append(" ] ")
        val msg: String = objectMsg?.toString() ?: "Log with null Object"
        stringBuilder.append(msg)
        val logStr = stringBuilder.toString()
        when (type) {
            V -> Log.v(tag, logStr)
            D -> Log.d(tag, logStr)
            I -> Log.i(tag, logStr)
            W -> Log.w(tag, logStr)
            E -> Log.e(tag, logStr)
            A -> Log.wtf(tag, logStr)
        }
    }
}