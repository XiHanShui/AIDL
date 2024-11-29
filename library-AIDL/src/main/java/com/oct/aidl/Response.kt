package com.oct.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Response(val code: Int, val message: String, val requestId: String) : Parcelable{

    override fun toString(): String {
        return "Response(code=$code, message='$message', requestId='$requestId')"
    }
}
