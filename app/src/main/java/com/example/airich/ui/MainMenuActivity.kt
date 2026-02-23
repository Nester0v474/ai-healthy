package com.example.airich.ui

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.airich.data.FoodDatabase
import com.example.airich.data.SubscriptionManager
import com.example.airich.databinding.ActivityMainMenuBinding
import com.example.airich.ui.habittracker.HabitTrackerActivity
import com.example.airich.ui.reminders.RemindersActivity
import com.example.airich.ui.sleepdiary.SleepDiaryActivity
import com.example.airich.ui.foodtracker.FoodTrackerActivity
import com.example.airich.ui.moodtracker.MoodTrackerActivity
import com.example.airich.ui.healthytasktracker.HealthyTaskTrackerActivity
import com.example.airich.ui.healthy.HealthyActivity
import com.example.airich.utils.TextUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var subscriptionManager: SubscriptionManager
    private val timeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscriptionManager = SubscriptionManager(this)
        subscriptionManager.initialize()

        loadLastChatMessage()

        binding.cardLastMessage.setOnClickListener { startActivity(Intent(this, HealthyActivity::class.java)) }
        binding.btnFoodTracker.setOnClickListener { startActivity(Intent(this, FoodTrackerActivity::class.java)) }
        binding.btnMoodTracker.setOnClickListener { startActivity(Intent(this, MoodTrackerActivity::class.java)) }
        binding.btnHabitTracker.setOnClickListener { startActivity(Intent(this, HabitTrackerActivity::class.java)) }
        binding.btnReminders.setOnClickListener { startActivity(Intent(this, RemindersActivity::class.java)) }
        binding.btnSleepDiary.setOnClickListener { startActivity(Intent(this, SleepDiaryActivity::class.java)) }
        binding.btnHopeTracker.setOnClickListener { startActivity(Intent(this, HealthyTaskTrackerActivity::class.java)) }
        binding.btnHope.setOnClickListener { startActivity(Intent(this, HealthyActivity::class.java)) }
        binding.btnAccount.setOnClickListener { showAccountDialog() }
        binding.tvSwipeHint.setOnClickListener { binding.mainDrawerLayout.openDrawer(Gravity.END) }
    }

    override fun onResume() {
        super.onResume()
        loadLastChatMessage()
    }

    private fun loadLastChatMessage() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = FoodDatabase.getDatabase(this@MainMenuActivity)
                val lastMessage = database.chatMessageDao().getLastMessage().first()
                launch(Dispatchers.Main) {
                    if (lastMessage != null) {
                        val cleanText = if (lastMessage.isFromUser) lastMessage.text else TextUtils.removeMarkdown(lastMessage.text)
                        binding.tvLastChatMessage.text = cleanText
                        binding.tvLastChatTime.text = timeFormatter.format(Date(lastMessage.timestamp))
                    } else {
                        binding.tvLastChatMessage.text = "Начните диалог с Healthy"
                        binding.tvLastChatTime.text = ""
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainMenuActivity", "loadLastChatMessage", e)
            }
        }
    }

    private fun showAccountDialog() {
        val isActive = subscriptionManager.isSubscriptionActive()
        val isFreeTrial = subscriptionManager.isFreeTrialActive()
        val daysRemaining = subscriptionManager.getDaysRemaining()
        val price = subscriptionManager.getSubscriptionPrice()
        val expireDate = subscriptionManager.getExpirationDateString()
        val title = when {
            isFreeTrial && !isActive -> "Пробный период"
            isActive -> "Подписка активна"
            else -> "Premium"
        }
        val message = when {
            isFreeTrial && !isActive -> "🎁 Две недели — в подарок\n\nОсталось: $daysRemaining дн. полного доступа.\n\n• AI-врач Healthy 24/7\n• Трекеры и дневники\n\nПосле триала — подписка $price ₽/мес."
            isActive -> "Спасибо, что вы с нами!\n\nПолный доступ до $expireDate."
            else -> "Откройте все возможности Healthy: AI-врач, трекеры, дневники.\n\nПодписка $price ₽/мес."
        }
        val builder = AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("ОК", null)
        if (!isActive && !isFreeTrial) {
            builder.setNeutralButton("Оформить подписку") { _, _ ->
                Toast.makeText(this, "Подписка будет доступна в следующей версии", Toast.LENGTH_LONG).show()
            }
        }
        builder.show()
    }
}
