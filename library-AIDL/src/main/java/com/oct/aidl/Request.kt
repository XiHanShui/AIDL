package com.oct.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Request(
    val requestId: String=UUID.randomUUID().toString(),
    val params: String):Parcelable{
    override fun toString(): String {
        return "Request(requestId='$requestId', params='$params')"
    }
}
