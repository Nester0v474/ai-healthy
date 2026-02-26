package com.example.airich.ui.healthy

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.airich.api.DeepSeekMessage
import com.example.airich.api.DeepSeekRequest
import com.example.airich.api.DeepSeekService
import com.example.airich.data.ChatMessageEntity
import com.example.airich.data.FoodDatabase
import com.example.airich.data.SubscriptionManager
import com.example.airich.data.UserStatsService
import com.example.airich.data.dao.ChatMessageDao
import com.example.airich.databinding.FragmentHealthyChatBinding
import com.example.airich.utils.TextUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HealthyChatFragment : Fragment() {

    companion object {
        private const val TAG = "HealthyChatFragment"
    }

    private var _binding: FragmentHealthyChatBinding? = null
    private val binding get() = _binding!!

    private var adapter: ChatAdapter? = null
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var chatDao: ChatMessageDao
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var userStatsService: UserStatsService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: starting")
        try {
            _binding = FragmentHealthyChatBinding.inflate(inflater, container, false)
            Log.d(TAG, "onCreateView: binding created")
            return binding.root
        } catch (e: Exception) {
            Log.e(TAG, "onCreateView: error", e)
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: starting")

        try {
            if (_binding == null) {
                Log.e(TAG, "onViewCreated: binding is null!")
                return
            }

            val database = FoodDatabase.getDatabase(requireContext())
            chatDao = database.chatMessageDao()
            subscriptionManager = SubscriptionManager(requireContext())
            subscriptionManager.initialize()
            userStatsService = UserStatsService(requireContext())

            loadSavedMessages()

            val context = view.context
            adapter = ChatAdapter()
            binding.rvMessages.layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            binding.rvMessages.adapter = adapter

            adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    binding.rvMessages.post {
                        binding.rvMessages.smoothScrollToPosition((adapter?.itemCount ?: 1) - 1)
                    }
                }
            })

            Log.d(TAG, "onViewCreated: RecyclerView configured")

            if (messages.isEmpty()) {
                val welcomeMessage = ChatMessage(
                    text = "Привет, я Healthy. Я помогаю разбираться с вопросами про тело, настроение и здоровье. Расскажи в двух‑трёх фразах, что тебя больше всего беспокоит сейчас?",
                    isFromUser = false
                )
                messages.add(welcomeMessage)
                adapter?.submitList(messages.toList())
                saveMessage(welcomeMessage)
                Log.d(TAG, "onViewCreated: welcome message added")
            }

            binding.btnSend.setOnClickListener {
                sendMessage()
            }

            binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage()
                    true
                } else {
                    false
                }
            }

            Log.d(TAG, "onViewCreated: completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated: error", e)
        }
    }

    private fun sendMessage() {
        try {
            val binding = _binding ?: run {
                Log.w(TAG, "sendMessage: binding is null")
                return
            }

            val adapter = adapter ?: run {
                Log.w(TAG, "sendMessage: adapter is null")
                return
            }

            val messageText = binding.etMessage.text?.toString()?.trim()
            if (messageText.isNullOrBlank()) {
                return
            }

            val allowed = subscriptionManager.isSubscriptionActive()
            if (!allowed) {
                requireActivity().runOnUiThread { showSubscriptionDialog() }
                return
            }
            requireActivity().runOnUiThread { doSendMessage(messageText) }
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage: error", e)
        }
    }

    private fun doSendMessage(messageText: String) {
        try {
            val binding = _binding ?: return
            val adapter = adapter ?: return

            Log.d(TAG, "sendMessage: sending message: $messageText")

            val userMessage = ChatMessage(text = messageText, isFromUser = true)
            messages.add(userMessage)
            adapter.submitList(messages.toList())
            saveMessage(userMessage)

            try {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            } catch (e: Exception) {
                Log.w(TAG, "sendMessage: scroll error", e)
            }

            binding.etMessage.text?.clear()
            binding.tvTyping.visibility = View.VISIBLE

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                try {
                    val currentBinding = _binding

                    if (currentBinding != null) {
                        val userStats = withContext(Dispatchers.IO) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                userStatsService.getFullUserStats()
                            } else {
                                "Данные пользователя недоступны на этой версии Android."
                            }
                        }

                        val deepSeekMessages = mutableListOf<DeepSeekMessage>().apply {
                            val systemPrompt = buildString {
                                append("Ты Healthy — AI-помощник по здоровью, который работает как квалифицированный врач и психолог.\n\n")
                                append("Твоя задача:\n")
                                append("- Выслушивать пользователя, задавать уточняющие вопросы\n")
                                append("- Давать профессиональные советы по здоровью (физическому и ментальному)\n")
                                append("- Анализировать данные пользователя и давать персонализированные рекомендации\n")
                                append("- Всегда напоминать о необходимости очной консультации при серьёзных симптомах\n")
                                append("- Отвечать по-человечески, эмпатично, но профессионально\n")
                                append("- Использовать русский язык\n\n")
                                append("=== ДАННЫЕ ПОЛЬЗОВАТЕЛЯ ===\n")
                                append("У тебя есть доступ к данным пользователя о его настроении, питании, сне и задачах:\n\n")
                                append(userStats)
                                append("\n\nИспользуй эти данные для:\n")
                                append("- Понимания текущего состояния пользователя\n")
                                append("- Выявления закономерностей\n")
                                append("- Дачи персонализированных рекомендаций\n")
                                append("- Предложения конкретных действий\n")
                                append("- Анализа продуктивности пользователя\n")
                                append("Если данных недостаточно, можешь попросить пользователя добавить больше записей.\n")
                            }

                            add(DeepSeekMessage(role = "system", content = systemPrompt))

                            val recentMessages = messages.takeLast(10)
                            recentMessages.forEach { msg ->
                                add(DeepSeekMessage(
                                    role = if (msg.isFromUser) "user" else "assistant",
                                    content = msg.text
                                ))
                            }

                            add(DeepSeekMessage(role = "user", content = messageText))
                        }

                        val request = DeepSeekRequest(
                            model = "deepseek-chat",
                            messages = deepSeekMessages,
                            temperature = 0.7,
                            maxTokens = 2000,
                            stream = false
                        )

                        val response = DeepSeekService.getApi(requireContext()).createChatCompletion(request)

                        if (response.isSuccessful && response.body() != null) {
                            val responseBody = response.body()!!

                            if (responseBody.error != null) {
                                val error = responseBody.error
                                val errorMessage = "Извини, произошла ошибка: ${error.message ?: "Неизвестная ошибка"}"
                                val aiMessage = ChatMessage(text = errorMessage, isFromUser = false)
                                messages.add(aiMessage)
                                adapter.submitList(messages.toList())
                                saveMessage(aiMessage)
                                Log.e(TAG, "sendMessage: DeepSeek error - ${error.code}: ${error.message}")
                            } else if (responseBody.choices != null && responseBody.choices.isNotEmpty()) {
                                val choice = responseBody.choices[0]
                                var aiResponseText = choice.message?.content?.trim()
                                    ?: "Извини, не получилось сформировать ответ. Попробуй переформулировать вопрос."

                                aiResponseText = TextUtils.removeMarkdown(aiResponseText)

                                val aiMessage = ChatMessage(text = aiResponseText, isFromUser = false)
                                messages.add(aiMessage)
                                adapter.submitList(messages.toList())
                                saveMessage(aiMessage)

                                try {
                                    currentBinding.rvMessages.scrollToPosition(messages.size - 1)
                                } catch (e: Exception) {
                                    Log.w(TAG, "sendMessage: scroll error in coroutine", e)
                                }

                                Log.d(TAG, "sendMessage: DeepSeek response received")
                            } else {
                                val errorMessage = "Извини, не получилось получить ответ от AI. Попробуй ещё раз."
                                val aiMessage = ChatMessage(text = errorMessage, isFromUser = false)
                                messages.add(aiMessage)
                                adapter.submitList(messages.toList())
                                saveMessage(aiMessage)
                                Log.e(TAG, "sendMessage: Empty response from DeepSeek")
                            }
                        } else {
                            val errorBody = try {
                                response.errorBody()?.string() ?: "Unknown error"
                            } catch (e: Exception) {
                                "Error reading error body: ${e.message}"
                            }
                            val errorMessage = when (response.code()) {
                                400 -> "Неверный запрос. Попробуй переформулировать сообщение."
                                401 -> "Ошибка авторизации. Проверьте API ключ в настройках сервера."
                                429 -> "Слишком много запросов. Подождите немного."
                                500, 502, 503, 504 ->
                                    "Сервер недоступен (код ${response.code()}). Убедитесь, что Node.js сервер запущен и работает."
                                else ->
                                    "Ошибка при обращении к AI (код ${response.code()}). Проверьте, что Node.js сервер запущен. Попробуй ещё раз через минуту."
                            }
                            val aiMessage = ChatMessage(text = errorMessage, isFromUser = false)
                            messages.add(aiMessage)
                            adapter.submitList(messages.toList())
                            saveMessage(aiMessage)
                            Log.e(TAG, "sendMessage: HTTP error - ${response.code()}: ${response.message()}, body: $errorBody")
                        }

                        currentBinding.tvTyping.visibility = View.GONE
                    } else {
                        Log.w(TAG, "sendMessage: fragment detached or binding null")
                        _binding?.tvTyping?.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "sendMessage: error in coroutine", e)
                    _binding?.let { b ->
                        val isConnectionError = e.message?.contains("Unable to resolve host") == true ||
                            e.message?.contains("timeout") == true ||
                            e.message?.contains("Connection refused") == true ||
                            e.message?.contains("Failed to connect") == true ||
                            e is java.net.SocketTimeoutException ||
                            e is java.net.UnknownHostException
                        val errorMessage = when {
                            e.message?.contains("Unable to resolve host") == true ->
                                "Нет подключения к интернету или сервер недоступен. Проверьте соединение и убедитесь, что Node.js сервер запущен."
                            e.message?.contains("timeout") == true ->
                                "Превышено время ожидания. Сервер не отвечает. Проверьте, что Node.js сервер запущен на порту 3000."
                            e.message?.contains("Connection refused") == true ||
                            e.message?.contains("Failed to connect") == true ->
                                "Не удалось подключиться к серверу. Убедитесь, что:\n• Node.js сервер запущен\n• Компьютер и телефон в одной Wi-Fi сети\n• IP-адрес в настройках правильный"
                            e is java.net.SocketTimeoutException ->
                                "Сервер не отвечает. Проверьте, что Node.js сервер запущен."
                            e is java.net.UnknownHostException ->
                                "Сервер недоступен. Проверьте настройки подключения."
                            else ->
                                "Произошла ошибка соединения: ${e.message ?: "Неизвестная ошибка"}. Проверьте, что Node.js сервер запущен и доступен."
                        }
                        val aiMessage = ChatMessage(text = errorMessage, isFromUser = false)
                        messages.add(aiMessage)
                        adapter?.submitList(messages.toList())
                        saveMessage(aiMessage)
                        b.tvTyping.visibility = View.GONE
                        if (isConnectionError) showServerIpDialog(requireContext())
                    } ?: run {
                        _binding?.tvTyping?.visibility = View.GONE
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage: error", e)
        }
    }

    private fun loadSavedMessages() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val savedMessages = chatDao.getAllMessages().first()
                if (savedMessages.isNotEmpty()) {
                    val chatMessages = savedMessages.map { entity ->
                        ChatMessage(
                            text = entity.text,
                            isFromUser = entity.isFromUser,
                            timestamp = entity.timestamp
                        )
                    }
                    messages.clear()
                    messages.addAll(chatMessages)
                    adapter?.submitList(messages.toList())
                    Log.d(TAG, "loadSavedMessages: loaded ${messages.size} messages")
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadSavedMessages: error", e)
            }
        }
    }

    private fun saveMessage(message: ChatMessage) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val entity = ChatMessageEntity(
                    text = message.text,
                    isFromUser = message.isFromUser,
                    timestamp = message.timestamp
                )
                chatDao.insertMessage(entity)
                Log.d(TAG, "saveMessage: message saved")
            } catch (e: Exception) {
                Log.e(TAG, "saveMessage: error", e)
            }
        }
    }

    private fun showServerIpDialog(context: Context) {
        val defaultHost = DeepSeekService.getDefaultHost()
        val currentHost = DeepSeekService.getBackendHost(context)
        val input = EditText(context).apply {
            hint = defaultHost
            setText(currentHost ?: defaultHost)
            setPadding(80, 50, 80, 30)
        }
        AlertDialog.Builder(context)
            .setTitle("Адрес сервера")
            .setMessage("Сервер не отвечает. Сейчас: ${currentHost ?: defaultHost}.\n\nПроверь на сервере:\n• cd /root/backend && npm start\n• Порт 3000 открыт: ufw allow 3000")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val host = input.text?.toString()?.trim()
                if (!host.isNullOrEmpty()) {
                    DeepSeekService.setBackendHost(context, host)
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        val ok = DeepSeekService.checkServerReachable(context)
                        withContext(Dispatchers.Main) {
                            if (ok) {
                                Toast.makeText(context, "Сервер доступен. Отправь сообщение.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Сервер недоступен. Запусти backend на сервере (npm start), открой порт 3000.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .setNeutralButton("Сбросить") { _, _ ->
                DeepSeekService.setBackendHost(context, null)
                Toast.makeText(context, "Сброшено на $defaultHost. Отправь сообщение снова.", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun showSubscriptionDialog() {
        val price = subscriptionManager.getSubscriptionPrice()
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Premium — полный доступ")
            .setMessage("""
                Две недели бесплатно закончились.

                Продолжайте пользоваться Healthy без ограничений:
                • AI-врач и психолог 24/7
                • Трекеры питания, сна, настроения

                Подписка $price ₽/мес. Без автопродлений.
            """.trimIndent())
            .setPositiveButton("Оформить подписку") { _, _ ->
                Toast.makeText(requireContext(), "Подписка будет доступна в следующей версии", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Позже", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")

        _binding?.btnSend?.setOnClickListener(null)
        _binding?.etMessage?.setOnEditorActionListener(null)
        _binding?.rvMessages?.adapter = null
        adapter = null
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}
