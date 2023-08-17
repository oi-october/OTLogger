package com.october.lib.logger.util

import com.october.lib.logger.Logger

internal fun debugLog(msg:String){
    if(!Logger.logger.isDebug())return
    System.out.println("[OTLogger][Debug]：${msg}")
}

internal fun errorLog(msg:String){
    if(!Logger.logger.isDebug())return
    System.out.println("[OTLogger][Error]：${msg}")
}
