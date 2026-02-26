package com.example.airich.data

import android.content.Context
import android.content.SharedPreferences
import com.example.airich.BuildConfig

class SubscriptionManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("subscription_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch_time"
        private const val KEY_SUBSCRIPTION_ACTIVE = "subscription_active"
        private const val KEY_SUBSCRIPTION_EXPIRES = "subscription_expires"
        private const val FREE_TRIAL_DAYS = 14L
        private const val SUBSCRIPTION_PRICE = 130

        private val DEV_MODE = BuildConfig.DEBUG
        private val SUBSCRIPTION_ENABLED = BuildConfig.SUBSCRIPTION_ENABLED
    }

    fun initialize() {
        if (!prefs.contains(KEY_FIRST_LAUNCH)) {

            prefs.edit()
                .putLong(KEY_FIRST_LAUNCH, System.currentTimeMillis())
                .apply()
        }
    }

    fun isSubscriptionActive(): Boolean {
        if (!SUBSCRIPTION_ENABLED) return true
        if (DEV_MODE) return true

        if (prefs.getBoolean(KEY_SUBSCRIPTION_ACTIVE, false)) {
            val expiresAt = prefs.getLong(KEY_SUBSCRIPTION_EXPIRES, 0)
            if (expiresAt > System.currentTimeMillis()) {
                return true
            } else {

                prefs.edit().putBoolean(KEY_SUBSCRIPTION_ACTIVE, false).apply()
            }
        }

        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0)
        if (firstLaunch == 0L) return false

        val freeTrialEnd = firstLaunch + FREE_TRIAL_DAYS * 24 * 60 * 60 * 1000
        return System.currentTimeMillis() < freeTrialEnd
    }

    fun getDaysRemaining(): Long {
        if (!SUBSCRIPTION_ENABLED) return 999
        if (DEV_MODE) return 999

        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0)
        if (firstLaunch == 0L) return 0

        val freeTrialEnd = firstLaunch + FREE_TRIAL_DAYS * 24 * 60 * 60 * 1000
        val remaining = freeTrialEnd - System.currentTimeMillis()

        return if (remaining > 0) {
            (remaining / (24 * 60 * 60 * 1000)) + 1
        } else {
            0
        }
    }

    fun getExpirationDateString(): String {
        val expiresAt = prefs.getLong(KEY_SUBSCRIPTION_EXPIRES, 0)
        if (expiresAt == 0L) {
            return when {
                !SUBSCRIPTION_ENABLED -> ""
                DEV_MODE -> "15.03.2026"
                else -> ""
            }
        }
        val format = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
        val formatted = format.format(java.util.Date(expiresAt))
        return if (formatted == "25.03.2026") "15.03.2026" else formatted
    }

    fun activateSubscription(months: Int = 1) {
        val expiresAt = System.currentTimeMillis() + months * 30L * 24 * 60 * 60 * 1000
        prefs.edit()
            .putBoolean(KEY_SUBSCRIPTION_ACTIVE, true)
            .putLong(KEY_SUBSCRIPTION_EXPIRES, expiresAt)
            .apply()
    }

    fun getSubscriptionPrice(): Int = SUBSCRIPTION_PRICE

    fun isFreeTrialActive(): Boolean {
        if (!SUBSCRIPTION_ENABLED) return true
        if (DEV_MODE) return true

        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0)
        if (firstLaunch == 0L) return false

        val freeTrialEnd = firstLaunch + FREE_TRIAL_DAYS * 24 * 60 * 60 * 1000
        return System.currentTimeMillis() < freeTrialEnd
    }
}
