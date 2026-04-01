package com.example.emsaide.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emsaide.R;
import com.example.emsaide.data.model.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人列表适配器
 */
public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {
    
    private List<Contact> contacts;
    private final OnContactClickListener listener;
    
    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }
    
    public ContactListAdapter(List<Contact> contacts, OnContactClickListener listener) {
        this.contacts = contacts;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.bind(contact);
    }
    
    @Override
    public int getItemCount() {
        return contacts != null ? contacts.size() : 0;
    }
    
    public void setContacts(List<Contact> newContacts) {
        this.contacts = newContacts != null ? newContacts : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        
        private final TextView avatarText;
        private final TextView nameText;
        private final TextView emailText;
        private final TextView unreadBadge;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarText = itemView.findViewById(R.id.avatarText);
            nameText = itemView.findViewById(R.id.nameText);
            emailText = itemView.findViewById(R.id.emailText);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onContactClick(contacts.get(position));
                }
            });
        }
        
        void bind(Contact contact) {
            // 设置名称
            String name = contact.getName();
            if (name == null || name.isEmpty()) {
                name = contact.getEmail();
            }
            nameText.setText(name);
            
            // 设置邮箱
            emailText.setText(contact.getEmail());
            
            // 设置头像（首字母）
            String firstLetter = name.substring(0, 1).toUpperCase();
            avatarText.setText(firstLetter);
            
            // 设置头像背景颜色
            int color = ContextCompat.getColor(itemView.getContext(), R.color.primary);
            if (contact.getAvatarColor() != 0) {
                color = contact.getAvatarColor();
            }
            avatarText.setBackgroundColor(color);
            
            // 隐藏未读徽章（暂时）
            unreadBadge.setVisibility(View.GONE);
        }
    }
}
