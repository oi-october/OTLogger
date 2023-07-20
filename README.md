# 说明文档

## 说明
 Android日志库，官方Log 高替版。允许同时把日志 **输出到Logcat **和 **保存到本地**。因其内部实现采用策略的设计模式，所以使用者可以根据自己的需求轻松定制该库的每个模块，包括日志输出格式、日志保存方式、异常捕获方式等。

## 使用说明

#### 添加依赖

在 root/build.gradle 中添加

```
repositories {
        maven { url 'https://jitpack.io' }
}
```

在项目中添加依赖

```
implementation ''
```

#### 使用 QLlogger

```
LogUtils.v(TAG, "V级别 日志")  
LogUtils.d(TAG, "D级别 日志") 
LogUtils.i(TAG, "I级别 日志")  
LogUtils.w(TAG, "W级别 日志")  
LogUtils.e(TAG, "E级别 日志")  
```

默认Logger 打印日志到 Logcat的同时会保存日志到 storage/emulated/0/Android/data/packageName/files/log 下。默认Logger会按与Log一致的格式输出到控制台和日志文件，并且日志文件仅仅保留7天，超过时间的日志文件自动删除。

如果希望定制日志输出格式、日志输出级别、日志保存文件夹地址，见**高级功能**。



## lib_logger 设计模型



## 高级功能

如果不想用默认的方式，可以自定义输出到Logcat 和 日志文件的策略，如下:

```kotlin
val logger = Logger.Builder()
         //配置输出到logcat的 Printer
         .setLogcatPrinter(LogcatCustomPrinter(true, LogLevel.D, LogcatDefaultFormatStrategy()))
         //配置输出到日志文件的 Printer
         .setLogTxtPrinter(LogTxtCustomPrinter( true,LogLevel.I,LogTxtDefaultFormatStrategy(),TimeLogDiskStrategyImpl()))
         //配置崩溃策略
         .setCrashStrategy(DefaultCrashStrategyImpl(app)) 
         //构建一个Logger
         .build() 
LogUtils.setLogger(logger)
```

#### 配置输出到Logcat 的 Printer

#### 当前库中提供三种老化机制的实现类，只需要在初始化时设置即可，如下：

+ *FileLogDiskStrategyImpl*，默认每个日志文件5MB，默认日志文件夹最大可容纳100M日志，超过设定值会按照时间顺序删除旧的日志，直到低于预定值
+ *TimeLogDiskStrategyImpl*，  默认按照小时创建日志文件，每个日志文件保存七天
+ *FileAndTimeDiskStrategyImpl*，同时具备*FileLogDiskStrategyImpl*和*TimeLogDiskStrategyImpl*的特性

#####输出的样式

