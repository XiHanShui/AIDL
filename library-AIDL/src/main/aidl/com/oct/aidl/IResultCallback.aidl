// IResultCallback.aidl
package com.oct.aidl;
import com.oct.aidl.Response;

// Declare any non-default types here with import statements

interface IResultCallback {


   void onResult(in Response response);

    void notifyResponseReady(in String clientId);


}