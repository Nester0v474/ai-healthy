package com.example.airich.utils

import kotlin.text.RegexOption

object TextUtils {

    fun removeMarkdown(text: String): String {
        return text

            .replace(Regex("^#{1,6}\\s*", RegexOption.MULTILINE), "")

            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("__(.*?)__"), "$1")

            .replace(Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)"), "$1")
            .replace(Regex("(?<!_)_(?!_)(.*?)(?<!_)_(?!_)"), "$1")

            .replace(Regex("~~(.*?)~~"), "$1")

            .replace(Regex("`(.*?)`"), "$1")

            .replace(Regex("\\[([^\\]]+)\\]\\([^\\)]+\\)"), "$1")

            .replace(Regex("!\\[([^\\]]*)\\]\\([^\\)]+\\)"), "")

            .replace(Regex("^[\\s]*[-*+]\\s+", RegexOption.MULTILINE), "")

            .replace(Regex("^[\\s]*\\d+\\.\\s+", RegexOption.MULTILINE), "")

            .replace(Regex("^[\\s]*[-*]{3,}[\\s]*$", RegexOption.MULTILINE), "")
            .trim()
    }
}
