package com.example.bulletinboard.model

/**
 * Класс с типом данных для счетчика обяъвлений
 * С помощью этого класса запишу новый узел в базу
 */
data class InfoItem(
    val viewsCounter: String? = null,
    val emailsCounter: String? = null,
    val callsCounter: String? = null
)
