package com.xieql.lib.logger

import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.util.Log
import androidx.annotation.StringRes
import com.xieql.lib.logger.core.*
import com.xieql.lib.logger.utils.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 日志打印器，逻辑基本照搬DefaultPrinter，修改日志输出格式
 **/

internal class LogPrinter() : Printer {

    init {
        FreeSpaceManager.toStart()
    }


    override fun printToConsole(
        logLevel: LogLevel,
        tag: String,
        message: String,
        element: StackTraceElement
    ) {
        if(isDebug()){
            //控制台输出
            val tag = FormatHelper.getConsoleFormatTag(tag,message,element)
            val msg = FormatHelper.getConsoleFormatMsg(tag,message,element)
            PrintHelper.log2Console(logLevel,tag,msg)
        }
    }

    override fun printToFile(
        storeInSdCard: Boolean,
        logLevel: LogLevel,
        message: String,
        element: StackTraceElement,
        logDir: String,
        logPrefix: String,
        logSegment: LogSegment
    ) {
        //本地文件输出
        PrintHelper.log2File(storeInSdCard,logLevel,message,element,logDir,logPrefix,logSegment)
    }
}


//打印帮助类
internal object PrintHelper{
    //打印到控制台
    fun log2Console(logLevel: LogLevel, tag: String, message: String) {
        debugLog("输出到控制台")
        when (logLevel) {
            LogLevel.V -> Log.v(tag, message)
            LogLevel.D -> Log.d(tag, message)
            LogLevel.I -> Log.i(tag, message)
            LogLevel.W -> Log.w(tag, message)
            LogLevel.E -> Log.e(tag, message)
            LogLevel.WTF -> Log.wtf(tag, message)
        }
    }


    //打印到本地
    fun log2File(storeInSdCard: Boolean,
                 logLevel: LogLevel,
                 message: String,
                 element: StackTraceElement,
                 logDir: String,
                 logPrefix: String,
                 logSegment: LogSegment){
        var message= message
        if(!FreeSpaceManager.isFreeSpace){
            //空间不足
            if(logLevel == LogLevel.WTF){
                // WTF级别日志可以输出
                LogUtils.v("print","空间不足，输出WTF日志")
            }else{
                //其他日志不可以输出
                LogUtils.v("print","空间不足，不输出日志")
                return
            }
        }
        debugLog("写入输入日志到文件")

        executePool()
        val msg = FormatHelper.getFileFormatMsg(logLevel,message,element)
        PrinterExecutor.cacheQueue.offer(
            LogToFileInfo(
                storeInSdCard,
                logDir,
                logPrefix,
                logSegment,
                msg
            )
        )
    }

    private fun executePool() {
        PrinterExecutor.look.lock()
        try {
            if (PrinterExecutor.threadPool.activeCount < PrinterExecutor.MAX_THREAD) {
                PrinterExecutor.threadPool.execute(WriteLogToFileRunnable())
            }
        } catch (ignore: Exception) {
            // ignore
        } finally {
            PrinterExecutor.look.unlock()
        }
    }

    //写入数据
    internal class WriteLogToFileRunnable() : Runnable {
        companion object{
            private const val TAG = "WriteLogToFileRunnable"
        }

        override fun run() {
            try {
                while (!Thread.interrupted()) {
                    val info = PrinterExecutor.cacheQueue.poll(
                        PrinterExecutor.QUEUE_WAIT_TIME,
                        TimeUnit.SECONDS
                    )
                        ?: return
                    PrinterExecutor.look.lock()
                    try {
                        val dirPath = LogConfig.genDirPath(info.storeInSdCard, info.logDir)
                        if (dirPath != null) {
                            val fileName = genFileName(info.logPrefix, info.logSegment)
                            val filePath = dirPath + File.separator + fileName
                            write(info,filePath, info.message)
                        }
                    } catch (ignore: Exception) {
                        // ignore
                    } finally {
                        PrinterExecutor.look.unlock()
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }

        private fun write(info: LogToFileInfo,filePath: String, content: String) {
            val file = File(filePath)
            var outContent: String = content
            if (!file.isExist()) {
                outContent = FormatHelper.getDeviceInfo() + content
                file.create()

                deleteLessFile(info)
            }
            file.appendText(outContent)
        }

        //文件过多，删除最久的文件
        private fun deleteLessFile(info:LogToFileInfo){
            try {
                val dirPath = LogConfig.genDirPath(info.storeInSdCard, info.logDir)
                val logDir = File(dirPath)
                val logFiles = logDir.logFiles()
                if(logFiles != null && logFiles.isNotEmpty()){
                    val filterLogFiles = logFiles.filterLogFiles(info.logPrefix, info.logSegment)
                    LogUtils.d(TAG,"日志数量：${filterLogFiles.size}")
                    if(filterLogFiles.size>0 && filterLogFiles.size>LogUtils.getHelper().getMaxLogNum()){
                        //删除最旧的数据
                        var oldFile = filterLogFiles[0]
                        for (index in 0 until filterLogFiles.size){
                            if(oldFile.name > filterLogFiles[index].name ){
                                oldFile = filterLogFiles[index]
                            }
                        }
                        if(oldFile.exists()){
                            LogUtils.d(TAG,"删除多余日志文件：${oldFile.name}")
                            oldFile.delete()
                        }

                    }
                }
            }catch (e:Exception){
                LogUtils.w(TAG,"删除文件失败",e)
            }
        }

    }

}

//格式帮助类
internal object FormatHelper{

    fun getConsoleFormatTag(tag:String,msg:String,element: StackTraceElement):String{
        return "(${element.fileName}:${element.lineNumber})"
    }

    //控制台数据格式
    fun getConsoleFormatMsg(tag:String,msg:String,element: StackTraceElement):String{
        return "#${element.methodName} Thread:${Thread.currentThread().name} \n" +
            "$msg\n \n"
    }

    //设备信息
    fun getDeviceInfo():String{
        fun appStr(@StringRes stringResId: Int):String{
            return appCtx.resources.getString(stringResId)
        }
        return "${appStr(R.string.log_app_version_name)}: ${appVerName()}\n" +
            "${appStr(R.string.log_app_version_code)}: ${appVerCode()}\n" +
            "${appStr(R.string.log_os_version_name)}: $osVerName\n" +
            "${appStr(R.string.log_os_version_code)}: $osVerCode\n" +
            "${appStr(R.string.log_os_display_name)}: $osVerDisplayName\n" +
            "${appStr(R.string.log_brand_info)}: $brand\n" +
            "${appStr(R.string.log_product_info)}: $product\n" +
            "${appStr(R.string.log_model_info)}: $model\n" +
            "${appStr(R.string.log_manufacturer_info)}: $manufacturer\n" +
            "SN:${LogUtils.getHelper().getSN()}\n\n\n"
    }

    //文件日志格式
    //[时间/包名 等级][(类名:行数) #方法名称 Thread:线程名称]\n 日志信息\n
    fun getFileFormatMsg(level: LogLevel,msg: String,element: StackTraceElement):String{
        return "[$nowStr/${appCtx.packageName} $level] [${Process.myPid()}] [(${element.fileName}:${element.lineNumber})"+
            " #${element.methodName} Thread:${Thread.currentThread().name} - ${Thread.currentThread().id}]\n"+
            "$msg\n"
    }


}

//存储空间管理
internal object FreeSpaceManager{

    private const val TAG = "FreeSpaceManager"

    private var NOT_FREE_SPACE_TIP = "剩余存储空间："

    var isFreeSpace = true // sdcard 是否有充足空间


    private var isRunning = false
    private val TIME = 15*60*1000L  //间隔15min查询一次
    private val MIN_NUM = 1024 //最小剩余容量1024M = 1G ，可用空间 >1G  = 空间充足， 否则不足
    private val MAX_LOG_SIZE = 500 //最大日志容量

    //微信支付地址文件
    private val WX_PAY_FACE = Environment.getExternalStorageDirectory().absolutePath + File.separator+"wxpayface"+File.separator+"log"
    //云从日志
    private val YC_LOG = Environment.getExternalStorageDirectory().absolutePath + File.separator+"cloudwalk"
    //系统日志
    private val SYSTEM_LOG = Environment.getExternalStorageDirectory().absolutePath + File.separator+"logfile"

    @Synchronized
    fun toStart(){
       if(!isRunning){
            Thread{
                var free = 0  //空闲空间
                var logDir = LogConfig.genDirPath(true, LogUtils.getHelper().getLogDir()) //日志文件夹
                var logSize = 0.0 //日志大小
                var wxpaySize = 0.0 // 微信文件夹大小
                var ycfacesize = 0.0 // 云从文件夹大小
                var systemLogSize = 0.0 //系统文件夹

                while (true){

                    //获得日志大小
                    logDir = LogConfig.genDirPath(true, LogUtils.getHelper().getLogDir())
                    LogUtils.v(TAG,"文件文件夹：${logDir}")
                    logSize = FileSizeUtil.getFileOrFilesSize(logDir,3)

                    wxpaySize = FileSizeUtil.getFileOrFilesSize(WX_PAY_FACE,3)
                    ycfacesize = FileSizeUtil.getFileOrFilesSize(YC_LOG,3)
                    systemLogSize = FileSizeUtil.getFileOrFilesSize(SYSTEM_LOG,3)

                    //获得可用空间大小
                    free =  getSDFreeSpace()
                    LogUtils.i(TAG,"空闲空间：${free} ,  占用-> 丰巢日志:${logSize}，微信刷脸日志：${wxpaySize}，云从日志：${ycfacesize}，,系统日志：${systemLogSize}")



                    //判断空闲空间
                    isFreeSpace = free > MIN_NUM
                    if(!isFreeSpace){
                        LogUtils.wtf(TAG,"空闲空间过少：空闲：${free}, 占用-> 丰巢日志:${logSize}，微信刷脸日志：${wxpaySize}，云从日志：${ycfacesize}，,系统日志：${systemLogSize}")
                    }

                    //日志文件大于 500M 删除日志
                    if(logSize> MAX_LOG_SIZE){
                        LogUtils.wtf(TAG,"日志超过500M容量,删除日志")
                        deleteLog(logSize)
                    }


                    logSize = FileSizeUtil.getFileOrFilesSize(logDir,3)
                    //获得可用空间大小
                    free =  getSDFreeSpace()
                    //判断空闲空间
                    isFreeSpace = free > MIN_NUM
                    LogUtils.i(TAG,"删除后空闲空间：${free} , 日志占用空间：${logSize}")


                    try {
                        Thread.sleep(TIME)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }

                }

            }.start()
           isRunning = true
       }

    }

    /**
     * @param size 日志总大小
     */
    private fun deleteLog(size:Double){

        val dirPath = LogConfig.genDirPath(true,LogUtils.getHelper().getLogDir())
        val logDir = File(dirPath)
        val logFiles = logDir.logFiles()
        if(logFiles != null && logFiles.isNotEmpty()){
            val filterLogFiles = logFiles.filterLogFiles(LogUtils.getHelper().getLogPrefix(),LogUtils.getHelper().getLogSegment())
            filterLogFiles.sort()
            LogUtils.d(TAG,"日志文件：${filterLogFiles}")

            if(filterLogFiles.size>0){
                //计算需要删除文件

                val deleteList = LinkedList<File>()
                var deleteLogSize = 0.0

                for (index in 0 until filterLogFiles.size-1){
                    deleteLogSize += FileSizeUtil.getFileOrFilesSize(filterLogFiles[index].absolutePath,3)
                    deleteList.add(filterLogFiles[index])
                    LogUtils.v(TAG,"删除日志文件：${filterLogFiles[index].absolutePath},删除大小：${deleteLogSize}")

                    if((size - deleteLogSize) < (MAX_LOG_SIZE / 2)){
                        break
                    }
                }
                //指令删除
                for (file in deleteList){
                    try {
                        file.delete()
                    }catch (e:Exception){
                        LogUtils.v(TAG,"删除失败",e)
                    }
                }
            }
        }
    }


    //检查SD卡是否有足够的空间
    private fun checkFreeSpace(minimum:Long):Boolean{
        val  size = minimum * 1024 * 1024
        val free =  getSDFreeSpace()
        val isFree =  free > size
        return isFree
    }


}


inline fun getSDFreeSpace():Int{
    val sdcardDir = Environment.getExternalStorageDirectory()
    val sf = StatFs(sdcardDir.path)
    val blockSize = sf.blockSize.toLong()
    val blockCount = sf.blockCount.toLong()
    val availCount = sf.availableBlocks.toLong()
    Log.v("", "block大小:" + blockSize + ",block数目:" + blockCount + ",总大小:" + blockSize * blockCount / 1024 + "KB")
    Log.v("", "可用的block数目：:" + availCount + ",剩余空间:" + availCount * blockSize / 1024 + "KB")
    return (availCount * blockSize/1024/1024).toInt()
}
