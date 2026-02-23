package com.example.airich.ui.healthy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.airich.databinding.ItemChatMessageBinding
import com.example.airich.utils.TextUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(DiffCallback()) {
    
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding, timeFormatter)
    }
    
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ChatViewHolder(
        private val binding: ItemChatMessageBinding,
        private val timeFormatter: SimpleDateFormat
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(message: ChatMessage) {
            if (message.isFromUser) {
                // Сообщение пользователя - справа
                binding.layoutUserMessage.visibility = ViewGroup.VISIBLE
                binding.layoutAiMessage.visibility = ViewGroup.GONE
                binding.tvUserMessage.text = message.text
                binding.tvUserTime.text = timeFormatter.format(Date(message.timestamp))
            } else {
                // Сообщение AI - слева
                binding.layoutUserMessage.visibility = ViewGroup.GONE
                binding.layoutAiMessage.visibility = ViewGroup.VISIBLE
                // Удаляем markdown форматирование из ответов ИИ
                val cleanText = TextUtils.removeMarkdown(message.text)
                binding.tvAiMessage.text = cleanText
                binding.tvAiTime.text = timeFormatter.format(Date(message.timestamp))
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.timestamp == newItem.timestamp && oldItem.text == newItem.text
        }
        
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}

