package com.xieql.lib.logger;

import androidx.annotation.NonNull;

/**
 * 外部使用该类中的静态方法打印日志
 */
public class LogUtils {

    //初始化默认的Logger
    static {
        // todo 初始化默认的Logger
    }
    //设置 Logger
    public static void setLogger(@NonNull Logger myLog){
        Logger.Companion.setLogger(myLog);
    }

    public static void v(String tag,String msg){
        v(tag,msg,null);
    }
    public static void v(String tag,String msg,Throwable thr){
        Logger.Companion.getLogger().println(LogLevel.V,tag,msg,thr);
    }
    public static void d(String tag,String msg){
        d(tag,msg,null);
    }
    public static void d(String tag,String msg,Throwable thr){}
    public static void i(String tag,String msg){
        i(tag,msg,null);
    }
    public static void i(String tag,String msg,Throwable thr){}
    public static void w(String tag,String msg){
        w(tag,msg,null);
    }
    public static void w(String tag,String msg,Throwable thr){}
    public static void e(String tag,String msg){
        e(tag,msg,null);
    }
    public static void e(String tag,String msg,Throwable thr){}






}
