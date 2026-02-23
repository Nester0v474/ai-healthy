package com.example.airich.utils

import kotlin.text.RegexOption

object TextUtils {
    /**
     * Удаляет markdown форматирование из текста (решётки, звёздочки, подчёркивания и т.д.)
     */
    fun removeMarkdown(text: String): String {
        return text
            // Удаляем заголовки # ## ### в начале строк (режим MULTILINE)
            .replace(Regex("^#{1,6}\\s*", RegexOption.MULTILINE), "")
            // Удаляем жирный текст **text** или __text__
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("__(.*?)__"), "$1")
            // Удаляем курсив *text* или _text_
            .replace(Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)"), "$1")
            .replace(Regex("(?<!_)_(?!_)(.*?)(?<!_)_(?!_)"), "$1")
            // Удаляем зачеркнутый текст ~~text~~
            .replace(Regex("~~(.*?)~~"), "$1")
            // Удаляем код `code`
            .replace(Regex("`(.*?)`"), "$1")
            // Удаляем ссылки [text](url)
            .replace(Regex("\\[([^\\]]+)\\]\\([^\\)]+\\)"), "$1")
            // Удаляем изображения ![alt](url)
            .replace(Regex("!\\[([^\\]]*)\\]\\([^\\)]+\\)"), "")
            // Удаляем списки - или *
            .replace(Regex("^[\\s]*[-*+]\\s+", RegexOption.MULTILINE), "")
            // Удаляем нумерованные списки 1. 2. и т.д.
            .replace(Regex("^[\\s]*\\d+\\.\\s+", RegexOption.MULTILINE), "")
            // Удаляем горизонтальные линии --- или ***
            .replace(Regex("^[\\s]*[-*]{3,}[\\s]*$", RegexOption.MULTILINE), "")
            .trim()
    }
}

