// ICommonService.aidl
package com.oct.aidl;
import android.os.Parcelable;
import com.oct.aidl.IRequestCallback;
import com.oct.aidl.IService2Client;
import com.oct.aidl.Request;
import com.oct.aidl.Response;
import android.os.SharedMemory;




// Declare any non-default types here with import statements

interface ICommonService  {

   // 客户端注册，返回客户端专属的共享内存描述符
    boolean registerClient(in String clientId,in String pkg, in IRequestCallback callback);
    boolean unregisterClient(in String clientId);


    boolean registerService2Client(in String clientId,in String pkg, in IService2Client callback);
    boolean unregisterService2Client(in String clientId);

    boolean registerSharedMenory(in String clientId,in String pkg, in IRequestCallback callback, in SharedMemory sharedMemory);



    // 客户端发送请求
    Response request(in Request request);

    // 客户端发送请求
    boolean asyncRequest(in String clientId,in Request request);

    // 服务端通知客户端结果已准备好
    boolean isResultReady(in String clientId);

    // 传输文件
    void transferringFile(in ParcelFileDescriptor  pfd);

    //传输大数据
    boolean transferringBigData(in String clientId);

    // 传输图片
    boolean transferringImg(in String clientId);

    // 通知请求端准备好
    void notifyRequestReady(in String clientId);





}