package com.october.lib.logger;


import androidx.annotation.NonNull;

/**
 * 外部使用该类中的静态方法打印日志
 */
public class LogUtils {
    //设置 Logger
    public static void setLogger(@NonNull Logger myLog){
        Logger.Companion.setLogger(myLog);
    }
    public static Logger getLogger(){
        return Logger.Companion.getLogger();
    }

    public static void v(String tag,@NonNull String msg){
        Logger.Companion.getLogger().println(LogLevel.V,tag,msg,null,null);
    }
    public static void v(String tag,@NonNull String msg,Throwable thr){
        Logger.Companion.getLogger().println(LogLevel.V,tag,msg,thr,null);
    }
    public static void v(String tag,@NonNull String msg,Throwable thr,Object param){
        Logger.Companion.getLogger().println(LogLevel.V,tag,msg,thr,param);
    }

    public static void d(String tag,String msg){
        Logger.Companion.getLogger().println(LogLevel.D,tag,msg,null,null);
    }
    public static void d(String tag,@NonNull String msg,Throwable thr){
        Logger.Companion.getLogger().println(LogLevel.D,tag,msg,thr,null);
    }
    public static void d(String tag,@NonNull String msg,Throwable thr,Object param){
        Logger.Companion.getLogger().println(LogLevel.D,tag,msg,thr,param);
    }

    public static void i(String tag,@NonNull String msg){
        Logger.Companion.getLogger().println(LogLevel.I,tag,msg,null,null);
    }
    public static void i(String tag,@NonNull String msg,Throwable thr){
        Logger.Companion.getLogger().println(LogLevel.I,tag,msg,thr,null);
    }
    public static void i(String tag,@NonNull String msg,Throwable thr,Object param){
        Logger.Companion.getLogger().println(LogLevel.I,tag,msg,thr,param);
    }

    public static void w(String tag,@NonNull String msg){
        Logger.Companion.getLogger().println(LogLevel.W,tag,msg,null,null);
    }
    public static void w(String tag,@NonNull String msg,Throwable thr){
        Logger.Companion.getLogger().println(LogLevel.W,tag,msg,thr,null);
    }
    public static void w(String tag,@NonNull String msg,Throwable thr,Object param){
        Logger.Companion.getLogger().println(LogLevel.W,tag,msg,thr,param);
    }

    public static void e(String tag,@NonNull String msg){
        Logger.Companion.getLogger().println(LogLevel.E,tag,msg,null,null);
    }
    public static void e(String tag,Throwable thr){
        Logger.Companion.getLogger().println(LogLevel.E,tag,null,thr,null);
    }
    public static void e(String tag,String msg,Throwable thr){
        Logger.Companion.getLogger().println(LogLevel.E,tag,msg,thr,null);
    }
    public static void e(String tag,@NonNull String msg,Throwable thr,Object param){
        Logger.Companion.getLogger().println(LogLevel.E,tag,msg,thr,param);
    }
    //糟糕的异常
    public static void wtf(String tag,@NonNull String msg, Throwable thr,Object param){
        Logger.Companion.getLogger().println(LogLevel.WTF,tag,msg,thr,param);
    }




}
