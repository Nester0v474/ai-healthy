package com.example.airich.ui.healthy

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.airich.R
import com.example.airich.data.FoodDatabase
import com.example.airich.data.dao.ChatMessageDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HealthyActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HealthyActivity"
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var chatDao: ChatMessageDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: starting")

        try {
            setContentView(R.layout.activity_healthy)
            Log.d(TAG, "onCreate: layout set")

            drawerLayout = findViewById(R.id.drawerLayout)

            val database = FoodDatabase.getDatabase(this)
            chatDao = database.chatMessageDao()

            findViewById<android.widget.TextView>(R.id.tvSessionHistory).setOnClickListener {
                showSessionHistory()
                drawerLayout.closeDrawer(findViewById(R.id.rightDrawer))
            }

            findViewById<android.widget.TextView>(R.id.tvHealthRecommendations).setOnClickListener {
                showHealthRecommendations()
                drawerLayout.closeDrawer(findViewById(R.id.rightDrawer))
            }

            findViewById<android.widget.TextView>(R.id.tvChatSettings).setOnClickListener {
                showChatSettings()
                drawerLayout.closeDrawer(findViewById(R.id.rightDrawer))
            }

            if (savedInstanceState == null) {
                supportFragmentManager.commit {
                    replace(R.id.fragmentContainer, HealthyChatFragment())
                    setReorderingAllowed(true)
                }
                Log.d(TAG, "onCreate: fragment added")
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: error", e)
            finish()
        }
    }

    private fun showSessionHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val messages = chatDao.getAllMessages().first()
                withContext(Dispatchers.Main) {
                    if (messages.isEmpty()) {
                        Toast.makeText(this@HealthyActivity, "История сессий пуста", Toast.LENGTH_SHORT).show()
                    } else {
                        val messageText = messages.takeLast(20).joinToString("\n\n") { msg ->
                            "${if (msg.isFromUser) "Вы" else "Healthy"}: ${msg.text}"
                        }
                        AlertDialog.Builder(this@HealthyActivity)
                            .setTitle("История сессий (последние 20 сообщений)")
                            .setMessage(messageText)
                            .setPositiveButton("ОК", null)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "showSessionHistory: error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HealthyActivity, "Ошибка загрузки истории", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showHealthRecommendations() {
        AlertDialog.Builder(this)
            .setTitle("Рекомендации по здоровью")
            .setMessage("Рекомендации формируются на основе ваших данных:\n\n" +
                    "• Настроение\n" +
                    "• Питание\n" +
                    "• Сон\n" +
                    "• Выполнение задач\n\n" +
                    "Healthy анализирует эти данные и даёт персонализированные советы в чате.")
            .setPositiveButton("ОК", null)
            .show()
    }

    private fun showChatSettings() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_chat_settings, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val prefs = getSharedPreferences("chat_settings", android.content.Context.MODE_PRIVATE)

        val switchHistory = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchHistory)
        val switchPersonalization = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchPersonalization)
        val switchNotifications = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchNotifications)

        switchHistory.isChecked = prefs.getBoolean("history", true)
        switchPersonalization.isChecked = prefs.getBoolean("personalization", true)
        switchNotifications.isChecked = prefs.getBoolean("notifications", false)

        switchHistory.setOnCheckedChangeListener { _, isChecked -> prefs.edit().putBoolean("history", isChecked).apply() }
        switchPersonalization.setOnCheckedChangeListener { _, isChecked -> prefs.edit().putBoolean("personalization", isChecked).apply() }
        switchNotifications.setOnCheckedChangeListener { _, isChecked -> prefs.edit().putBoolean("notifications", isChecked).apply() }

        dialogView.findViewById<android.view.View>(R.id.btnOk).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}
