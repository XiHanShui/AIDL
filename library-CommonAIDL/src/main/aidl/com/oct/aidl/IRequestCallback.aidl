// IRequestCallback.aidl
package com.oct.aidl;
import com.oct.aidl.Response;

// Declare any non-default types here with import statements

interface IRequestCallback {


   void onResult(in Response response);




}