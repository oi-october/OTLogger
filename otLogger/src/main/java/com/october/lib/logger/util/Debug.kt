package com.october.lib.logger.util

internal fun debugLog(msg:String){
    System.out.println("[OTLogger][Debug]：${msg}")
}

internal fun errorLog(msg:String){
    System.out.println("[OTLogger][Error]：${msg}")
}
