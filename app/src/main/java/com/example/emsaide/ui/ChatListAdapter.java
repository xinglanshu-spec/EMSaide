package com.example.emsaide.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emsaide.R;
import com.example.emsaide.data.model.EmailAccount;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 聊天列表适配器
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    
    private List<EmailAccount> accounts;
    private final OnAccountClickListener listener;
    private final SimpleDateFormat dateFormat;
    
    public interface OnAccountClickListener {
        void onAccountClick(EmailAccount account);
    }
    
    public ChatListAdapter(List<EmailAccount> accounts, OnAccountClickListener listener) {
        this.accounts = accounts;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_account, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmailAccount account = accounts.get(position);
        holder.bind(account);
    }
    
    @Override
    public int getItemCount() {
        return accounts != null ? accounts.size() : 0;
    }
    
    public void setAccounts(List<EmailAccount> newAccounts) {
        this.accounts = newAccounts != null ? newAccounts : Collections.emptyList();
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        
        private final TextView accountNameText;
        private final TextView emailText;
        private final TextView lastMessageText;
        private final TextView timeText;
        private final TextView unreadBadge;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            accountNameText = itemView.findViewById(R.id.accountNameText);
            emailText = itemView.findViewById(R.id.emailText);
            lastMessageText = itemView.findViewById(R.id.lastMessageText);
            timeText = itemView.findViewById(R.id.timeText);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAccountClick(accounts.get(position));
                }
            });
        }
        
        void bind(EmailAccount account) {
            // 设置账户名称
            String name = account.getAccountName();
            if (name == null || name.isEmpty()) {
                name = account.getEmail();
            }
            accountNameText.setText(name);
            
            // 设置邮箱地址
            emailText.setText(account.getEmail());
            
            // 显示提示信息
            lastMessageText.setText("点击开始聊天或同步邮件");
            timeText.setText("");
            
            // TODO: 当有消息时，显示最后一条消息和未读数
            // 目前隐藏未读徽章
            unreadBadge.setVisibility(View.GONE);
        }
    }
}
