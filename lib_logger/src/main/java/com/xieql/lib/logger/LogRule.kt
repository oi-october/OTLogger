/*
 * Copyright 2015-2021 Hive Box.
 */

package com.xieql.lib.logger

internal object LogRule {

    object Log{
        const val WITH_MAG = "%s:%s" // tag:msg
        const val WITH_MSG_TR = "%s:%s:%s" //tag:msg:Throwable
    }

    object LogTag{
        const val WITH_ENCRYPT = "[加密]"
        const val WITH_ENCRYPT_DEBUG = "[加密·DEBUG]"
    }





}
