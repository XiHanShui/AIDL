package com.oct.aidl.client

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.oct.aidl.KLog
import java.time.Duration


class ClientManager private constructor() {

    lateinit var context: Context
    var debug: Boolean = false
    var timeout: Duration = Duration.ofSeconds(10)

    private val clientCache by lazy { HashMap<String, AIDLClient>() }

    fun init(context: Context, debug: Boolean, timeout: Duration) {
        this.context = context.applicationContext
        this.debug = debug
        this.timeout = timeout
        KLog.init(debug)
    }


    fun addClient(client: AIDLClient) {
        clientCache[client.action] = client
    }

    fun  getClient(action: String): AIDLClient? {
        return clientCache[action]
    }

    fun removeClient(action: String): AIDLClient? {
        return clientCache.remove(action)
    }





    companion object {

        val instance by lazy { ClientManager() }
    }

}