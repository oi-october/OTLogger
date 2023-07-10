package com.xieql.lib.logger.print

import com.xieql.lib.logger.LogLevel

interface IPrinter {
    open fun print(logLevel: LogLevel, tag:String?, msg:String?, thr: Throwable?, param:Any?)
}