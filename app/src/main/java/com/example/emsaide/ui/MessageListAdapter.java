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
                // 发送的消息 - 靠右对齐，绿色气泡
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) 
                    messageContainer.getLayoutParams();
                params.gravity = Gravity.END;
                messageContainer.setLayoutParams(params);
                
                // 设置绿色气泡背景
                messageContainer.setBackgroundResource(R.drawable.message_bubble_sent);
                
                // 设置文字颜色为白色
                subjectText.setTextColor(Color.WHITE);
                senderText.setTextColor(Color.WHITE);
                contentText.setTextColor(Color.WHITE);
                timeText.setTextColor(Color.parseColor("#CCFFFFFF"));
                
            } else if (type == ChatMessage.MessageType.RECEIVED) {
                // 接收的消息 - 靠左对齐，灰色气泡
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) 
                    messageContainer.getLayoutParams();
                params.gravity = Gravity.START;
                messageContainer.setLayoutParams(params);
                
                // 设置灰色气泡背景
                messageContainer.setBackgroundResource(R.drawable.message_bubble_received);
                
                // 设置文字颜色为深色
                subjectText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.primary_dark));
                senderText.setTextColor(Color.parseColor("#666666"));
                contentText.setTextColor(Color.BLACK);
                timeText.setTextColor(Color.parseColor("#99000000"));
                
            } else if (type == ChatMessage.MessageType.SYSTEM) {
                // 系统消息 - 居中，黄色背景
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) 
                    messageContainer.getLayoutParams();
                params.gravity = Gravity.CENTER;
                messageContainer.setLayoutParams(params);
                messageContainer.setBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.message_system_bg));
                
                subjectText.setTextColor(Color.BLACK);
                senderText.setTextColor(Color.BLACK);
                contentText.setTextColor(Color.BLACK);
                timeText.setTextColor(Color.BLACK);
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
                    // 只显示邮箱名称部分，不显示完整邮箱
                    String displayName = sender;
                    if (sender.contains("<")) {
                        displayName = sender.substring(0, sender.indexOf("<")).trim();
                    }
                    if (displayName.isEmpty()) {
                        // 如果没有名称，显示邮箱地址的用户名部分
                        String email = extractEmailFromAddress(sender);
                        if (email != null && email.contains("@")) {
                            displayName = email.substring(0, email.indexOf("@"));
                        } else {
                            displayName = email != null ? email : sender;
                        }
                    }
                    senderText.setText(displayName);
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
        
        private String extractEmailFromAddress(String address) {
            if (address == null || address.isEmpty()) {
                return null;
            }
            
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
            java.util.regex.Matcher matcher = pattern.matcher(address);
            
            if (matcher.find()) {
                return matcher.group();
            }
            
            return null;
        }
    }
}
