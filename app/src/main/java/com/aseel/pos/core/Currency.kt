package com.aseel.pos.core

enum class Currency(val code: String, val symbol: String, val decimals: Int) {
    YER("YER", "ر.ي", 0),
    USD("USD", "$", 2),
    SAR("SAR", "ر.س", 2);

    companion object {
        fun fromCode(code: String): Currency = entries.find { it.code == code } ?: YER
    }
}
