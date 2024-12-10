package com.oct.aidl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface ResponseCode {

    int SUCCESS = 0;

    /**
     * 服务端未安装,或者服务端在启动时就崩溃了
     */
    int SERVER_NO_FOND = 100;

    int REGISTER_ERROR = 101;

    int SERVER_DISCONNECTED= 102;


}

