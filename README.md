# 说明文档

## 说明
 Android日志库，官方Log 高替版。允许同时把日志 **输出到Logcat **和 **保存到本地**。因其内部实现采用策略的设计模式，所以使用者可以根据自己的需求轻松定制该库的每个模块，包括日志输出格式、日志保存方式、捕获异常日志等。

## 使用说明

#### 添加依赖

在 root/build.gradle 中添加

```groovy
allprojects {
          repositories {
		...
		maven { url 'https://jitpack.io' }
          }
}
```

在项目中添加依赖：release 见最新版本号

```groovy
 //eg:implementation 'com.github.oi-october:OTLogger:1.0.3'
 implementation 'com.github.oi-october:OTLogger:release'
```

#### 初始化Logger

```kotlin
val logger = Logger.Builder()
    .setLogcatPrinter(LogcatDefaultPrinter())  //设置 logcat printer
    .setLogTxtPrinter(LogTxtDefaultPrinter()) //设置 LogTxt printer
    .setCrashStrategy(DefaultCrashStrategyImpl()) //设置 crash ,捕获异常日志
    .build()
LogUtils.setLogger(logger)
```

我们推荐您在使用`LogUtils`之前，先进行上诉初始化。它会给你带来如下的效果：

- 默认Logger 会把日志打印到 Logcat 中，同时会保存日志到 storage/emulated/0/Android/data/packageName/files/log 下；

- 默认Logger 会按与Log一致的格式输出到控制台和日志文件，并且日志文件仅仅保留7天，超过时间的日志文件自动删除。
  如果希望定制日志输出格式、日志输出级别、日志保存文件夹地址，见**高级功能**。
  <br/>

#### 使用 QLlogger

```kotlin
LogUtils.v(TAG, "V级别 日志")  
LogUtils.d(TAG, "D级别 日志") 
LogUtils.i(TAG, "I级别 日志")  
LogUtils.w(TAG, "W级别 日志")  
LogUtils.e(TAG, "E级别 日志")  
```

<br><br>

## 高级功能

### 了解OTLogger 设计

OTLogger 内部有两个打印机：`LogcatDefaultPrinter` 和 `LogTxtDefaultPrinter` 这两个打印机都继承于`BaseLogcatPrinter`。默认Logger 只设置了` LogcatDefaultPrinter `而没有设置` LogTxtDefaultPrinter`。

你可以通过定制 `LogcatDefaultPrinter `以便控制打印到 Logcat 的日志，你也可以通过定制 `LogTxtDefaultPrinter`以便控制打印到 disk 的日志。

```kotlin
/**
 * 默认Loacat 打印机
 * @property printable  是否打印日志到 logcat
 * @property minLevel   最小日志输出级别
 * @property formatStrategy 日志格式策略
 */
open class LogcatDefaultPrinter(
    val printable: Boolean = true,
    val minLevel: LogLevel = LogLevel.V,
    val formatStrategy: LogcatDefaultFormatStrategy = LogcatDefaultFormatStrategy()
) : BaseLogcatPrinter() 

/**
 * 默认日志文件打印机
 * @property printable 是否写入到文件
 * @property minLevel  最低输出日志
 * @property formatStrategy 日志格式策略
 * @property diskStrategy 文件管理策略
 */
open class LogTxtDefaultPrinter(
    val printable: Boolean = true,
    val minLevel: LogLevel = LogLevel.V,
    val formatStrategy: LogTxtDefaultFormatStrategy = LogTxtDefaultFormatStrategy(),
    val diskStrategy: BaseLogDiskStrategy = TimeLogDiskStrategyImpl()
) : BaseLogTxtPrinter()
```



### 1. 设置日志格式

OTLogger已经实现两种日志格式，`DefaultFormatStragety` 和 `PrettyFormatStrategy` 。

#### 使用`DefaultFormatStragety` 格式

该日志格式和官方默认Log输出格式（旧版）保持一致，是**默认日志格式策略**：

```kotlin
val logger = Logger.Builder()
                    //指定 Logcat 使用日志格式：LogcatDefaultFormatStrategy
                   .setLogcatPrinter(LogcatDefaultPrinter(formatStrategy = LogcatDefaultFormatStrategy()))
                    //指定 Logtxt 使用日志格式：LogTxtDefaultFormatStrategy
                   .setLogTxtPrinter(LogTxtDefaultPrinter(formatStrategy = LogTxtDefaultFormatStrategy()))
                   .build()
LogUtils.setLogger(logger)                   
```

日志格式输出如下：

![DefaultFormatStragety](rmRes/ic_logcat_default_format.png)

#### 使用 `PrettyFormatStrategy` 格式

```
val logger = Logger.Builder()
                    //指定 Logcat 使用日志格式：LogcatPrettyFormatStrategy
                   .setLogcatPrinter(LogcatDefaultPrinter(formatStrategy = LogcatPrettyFormatStrategy()))
                    //指定 Logtxt 使用日志格式：LogTxtPrettyFormatStrategy
                   .setLogTxtPrinter(LogTxtDefaultPrinter(formatStrategy = LogTxtPrettyFormatStrategy()))
                   .build()
LogUtils.setLogger(logger)  
```

输出日志格式如下：

![DefaultFormatStragety](rmRes/ic_pretty_format.png)



### 2. 设置磁盘管理模式

OTLogger 默认有三种磁盘管理模式

- `TimeLogDiskStrategyImpl` :按时间管理日志（**默认磁盘管理策略**）

  - 默认每个日志文件保存七天，
  - 默认按照小时创建日志文件
  - 默认文件名 log_年_月_日_时间段.log 。 eg：otLog_2023_02_12_15_16.log ，这里的 15_16 表示该文件储存 15点到 16点的日志。

  其构造方法如下：

  ```kotlin
  /**
   * @param logDirectory 日志文件夹
   * @param logKeepOfDay 日志保存天数
   * @param logSegment 创建日志文件间隔，默认每个小时创建一份新的日志文件
   */
  open class TimeLogDiskStrategyImpl(
      val logDirectory: String = defaultLogDir,
      val logKeepOfDay: Int = 7,
      val logSegment:LogTimeSegment= LogTimeSegment.ONE_HOUR
  ) : BaseLogDiskStrategy() 
  ```

- `FileLogDiskStrategyImpl`:按照文件大小管理磁盘日志

  - 默认每个日志文件5MB，参考[logFileStoreSizeOfMB]
  - 默认日志文件夹最大可容纳 100M日志，超过[logDirectoryMaxStoreSizeOfMB]会按照时间顺序删除旧的日志，直到低于预定值
  -  默认文件名 otLog\_年\_月\_日\_时\_分\_秒.log 。 eg: otLog_2023_02_12_16_28_56.log
  - 每个日志写满了会创建一个新的日志文件
  - 为了保护系统，以上都要当系统可用空闲空间大于最低限制的空闲空间[minFreeStoreOfMB]时，才会创建新的日志文件。

  其构造方法如下：

  ```kotlin
  /**
   * @param logDirectory 日志文件夹
   * @param minFreeStoreOfMB 最小空闲空间（单位MB），当系统最小空闲存储空间低于该值时，不再创建新的日志文件
   * @param logDirectoryMaxStoreSizeOfMB 日志文件夹最大的存储容量（单位MB），所有的日志文件加起来的大小不得操过该值
   * @param logFileStoreSizeOfMB 每个日志文件容量（单位MB），只有上一个日志文件操过容量，才会创建下一个日志文件
   */
  open class FileLogDiskStrategyImpl(
      val logDirectory: String = defaultLogDir,
      val minFreeStoreOfMB: Int = 200,
      val logDirectoryMaxStoreSizeOfMB: Int = 100,
      val logFileStoreSizeOfMB:Int = 5
  ) : BaseLogDiskStrategy()
  ```

  - `TimeLogDiskStrategyImpl`: 文件+时间管理策略，同时具备[FileLogDiskStrategyImpl] 和 [TimeLogDiskStrategyImpl] 的部分特性

    - 默认日志文件夹最大可容纳 100M日志，超过[logDirectoryMaxStoreSizeOfMB]会按照时间顺序删除旧的日志，直到低于预定值；
    - 默认文件名 默认文件名 otlog\_年\_月\_日\_时间段\_时间戳.log 。 eg: otLog_2023_02_12_16_17_11123223423423.log ；
    - 每个日志写满了会创建一个新的日志文件，超过日志时间片段，会创建一个新的日志文件进行存储；
    - 为了保护系统，以上都要当系统可用空闲空间大于最低限制的空闲空间[minFreeStoreOfMB]时，才会创建新的日志文件。

    其构造函数如下：

    ```kotlin
    /**
     * @param logDirectory 日志文件夹
     * @param minFreeStoreOfMB 最小空闲空间（单位MB），当系统最小空闲存储空间低于该值时，不再创建新的日志文件
     * @param logDirectoryMaxStoreSizeOfMB 日志文件夹最大的存储容量（单位MB），所有的日志文件加起来的大小不得操过该值
     * @param logFileStoreSizeOfMB 每个日志文件容量（单位MB），只有上一个日志文件操过容量，才会创建下一个日志文件
     * @param segment 创建日志文件间隔，默认每个小时创建一份新的日志文件
     *
     */
    open class FileAndTimeDiskStrategyImpl(
        val logDirectory: String = defaultLogDir,
        val minFreeStoreOfMB: Int = 200,
        val logDirectoryMaxStoreSizeOfMB: Int = 100,
        val logFileStoreSizeOfMB: Int = 5,
        val segment: LogTimeSegment = LogTimeSegment.ONE_HOUR
    ) : BaseLogDiskStrategy()
    ```

  #### 设置磁盘管理策略

  ```kotlin
   val logger = Logger.Builder()
                  .setLogTxtPrinter(
                       LogTxtDefaultPrinter(diskStrategy = TimeLogDiskStrategyImpl()) //time
                       // LogTxtDefaultPrinter(diskStrategy = FileLogDiskStrategyImpl())  //file
                       // LogTxtDefaultPrinter(diskStrategy = FileAndTimeDiskStrategyImpl()) // file & time
                   )
                  .setIsDebug(true)
                  .build()
  LogUtils.setLogger(logger)
  ```

### 3. 设置压缩策略

OTLogger 打印到disk的日志文件默认是不压缩的。在`v1.2.0`版本以后，添加了日志压缩策略，其压缩规则是：

- 每当创建一个新的日志文件，遍历所有旧的日志文件进行压缩；

- 当前正在写入的日志文件不压缩；

当前已经实现的压缩策略：

```kotlin
/**
 * 压缩日志文件成为zip包
 */
class ZipLogCompressStrategy : BaseLogCompressStrategy() 
```

所有的压缩策略有必须继承于 `BaseLogCompressStrategy`。如果有需要，你可以实现自己的压缩策略。

使用压缩策略：

压缩策略必须结合磁盘管理策略一起使用。比如：

```kotlin
//案例1：
val diskStrategy = FileAndTimeDiskStrategyImpl().also {
                			it.setLogCompressStrategy(logCompressStrategy) //设置压缩策略
                   }
//案例2：
val diskStrategy= TimeLogDiskStrategyImpl().also {
                			it.setLogCompressStrategy(logCompressStrategy) //设置压缩策略
            			}
```



### 4. 是否输出日志和日志级别

```kotlin
val logger = Logger.Builder()
                 //不输出日志到logcat
                .setLogcatPrinter(LogcatDefaultPrinter(printable = false))  
                //输出日志到disk
                .setLogTxtPrinter(LogTxtDefaultPrinter(printable = true)) 
                .build()
LogUtils.setLogger(logger)
```

```kotlin
val logger = Logger.Builder()
                 //输出日志到logcat，最低打印日志级别是 VERBOSE
                 .setLogcatPrinter(LogcatDefaultPrinter(printable = true, minLevel = LogLevel.V))  
                 //输出日志到disk,最低打印日志级别是 INFO
                .setLogTxtPrinter(LogTxtDefaultPrinter(printable = true, minLevel = LogLevel.I))
                .build()
LogUtils.setLogger(logger)
```



### 5. 捕获异常并输出crash 日志

```kotlin
val logger = Logger.Builder()
                .setCrashStrategy(DefaultCrashStrategyImpl()) //设置 crash ,捕获异常日志
                .build()
LogUtils.setLogger(logger)
```

通过上面配置，可以捕获app异常，并且把异常信息打印到Logcat 或者 Disk



### 6. 添加自定义打印机`IPrinter`

​	OTLogger 在 `v1.2.0` 版本以后不再局限于只有 `LogcatPrinter`和 `LogTxtPrinter` 两种日志打印机。大家可以根据自己的需求自定义打印机，以便可以把日志打印到喜欢的位置。

自定义打印机流程：

- 继承 `IPrinter`实现打印机，具体实现流程可参考`LogcatDefaultPrinter；

  例如下面案例：上传特定异常日志到云端

  ```kotlin
  class LogcatCloudPrinter:IPrinter{
    //日志格式
    private val formatStrategy =  LogcatDefaultFormatStrategy()
    override fun print(logLevel: LogLevel,tag: String?,msg: String?,thr: Throwable?,param: Any?) {
        if(logLevel == LogLevel.E){
          //日志信息
          val logInfo = formatStrategy.format(logLevel, tag, msg, thr, param)
          // todo 上传 ${logInfo} 到云端
          // ...
        }
    }
  }
  ```

- 添加自定义打印机到 Logger

  ```kotlin
  val logger = Logger.Builder()
                     .addExtraPrinter(LogcatCloudPrinter()) //添加自定义打印机
                     .build()
  LogUtils.setLogger(logger)
  ```



## 拓展

更多文章：[OTLogger 日志库：轻松定制属于你的日志](https://juejin.cn/post/7264780458738106409)







