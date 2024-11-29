package com.oct.aidl.client

import com.oct.aidl.Response
import java.util.UUID

fun interface IResponseCallback {

    /**
     * 请求成功
     */
    fun callback(result: Response)


}