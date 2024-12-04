package com.oct.aidl.client

import com.oct.aidl.Response
import java.util.UUID

abstract class IResponseCallback {

    lateinit var requestId: String

    /**
     * 请求成功
     */
    abstract fun callback(result: Response)


}