package com.xieql.lib.logger.core

import com.xieql.lib.logger.unexpectedValue

/**
 * 日志保存片段
 * @property value
 */
enum class LogSegment(var value: Int) {
    ONE_HOUR(1),
    TWO_HOURS(2),
    THREE_HOURS(3),
    FOUR_HOURS(4),
    SIX_HOURS(6),
    TWELVE_HOURS(12),
    TWENTY_FOUR_HOURS(24);

    companion object {
        private val map = values().associateBy { it.value }
        fun fromValue(type: Int) = map[type] ?: unexpectedValue(type)
    }
}