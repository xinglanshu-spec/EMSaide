package com.example.emsaide.ui;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emsaide.R;
import com.example.emsaide.data.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 消息列表适配器
 */
public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {
    
    private List<ChatMessage> messages;
    private final SimpleDateFormat dateFormat;
    
    public MessageListAdapter(List<ChatMessage> messages) {
        this.messages = messages;
        this.dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }
    
    public void setMessages(List<ChatMessage> newMessages) {
        this.messages = newMessages != null ? newMessages : Collections.emptyList();
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        
        private final TextView subjectText;
        private final TextView senderText;
        private final TextView contentText;
        private final TextView timeText;
        private final LinearLayout messageContainer;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectText = itemView.findViewById(R.id.subjectText);
            senderText = itemView.findViewById(R.id.senderText);
            contentText = itemView.findViewById(R.id.contentText);
            timeText = itemView.findViewById(R.id.timeText);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }
        
        void bind(ChatMessage message) {
            // 根据消息类型设置样式
            ChatMessage.MessageType type = message.getType();
            
            if (type == ChatMessage.MessageType.SENT) {
                // 发送的消息 - 靠右对齐，绿色背景
                messageContainer.setBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.message_sent_bg));
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) 
                    messageContainer.getLayoutParams();
                params.gravity = Gravity.END;
                messageContainer.setLayoutParams(params);
            } else if (type == ChatMessage.MessageType.RECEIVED) {
                // 接收的消息 - 靠左对齐，白色背景
                messageContainer.setBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.message_received_bg));
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) 
                    messageContainer.getLayoutParams();
                params.gravity = Gravity.START;
                messageContainer.setLayoutParams(params);
            } else if (type == ChatMessage.MessageType.SYSTEM) {
                // 系统消息 - 居中，黄色背景
                messageContainer.setBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.message_system_bg));
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) 
                    messageContainer.getLayoutParams();
                params.gravity = Gravity.CENTER;
                messageContainer.setLayoutParams(params);
            }
            
            // 设置主题（如果有）
            String subject = message.getSubject();
            if (subject != null && !subject.isEmpty()) {
                subjectText.setText(subject);
                subjectText.setVisibility(View.VISIBLE);
            } else {
                subjectText.setVisibility(View.GONE);
            }
            
            // 设置发件人（接收的消息显示发件人）
            if (type == ChatMessage.MessageType.RECEIVED) {
                String sender = message.getSender();
                if (sender != null && !sender.isEmpty()) {
                    senderText.setText("来自：" + sender);
                    senderText.setVisibility(View.VISIBLE);
                } else {
                    senderText.setVisibility(View.GONE);
                }
            } else {
                senderText.setVisibility(View.GONE);
            }
            
            // 设置内容
            contentText.setText(message.getContent());
            
            // 设置时间
            Date date = new Date(message.getTimestamp());
            timeText.setText(dateFormat.format(date));
        }
    }
}
