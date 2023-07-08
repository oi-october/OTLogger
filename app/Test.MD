# 测试文件
测试日志库


### 功能测试和压力测试
主要测试类：[TestLoggerHelper]


### 测试日志存储策略：TimeLogDiskStrategyImpl
针对[TimeLogDiskStrategyImpl]磁盘管理策略进行测试。

主要测试类：[TestLoggerTimeDiskHelper]

测试方法：
 1. 主动更改系统时间：
```
adb shell
su        
date -s "YYYYMMDD.HHMMSS" 
```
通过上面命令主动修改系统时间，测试日志文件变化




