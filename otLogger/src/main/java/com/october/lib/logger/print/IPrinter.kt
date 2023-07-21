package com.october.lib.logger.print

import com.october.lib.logger.LogLevel

interface IPrinter {
    open fun print(logLevel: LogLevel, tag:String?, msg:String?, thr: Throwable?, param:Any?)
}