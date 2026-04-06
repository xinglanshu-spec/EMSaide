package com.example.emsaide.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emsaide.R;
import com.example.emsaide.data.model.ChatMessage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天详情界面
 */
public class ChatDetailFragment extends Fragment {
    
    private RecyclerView messageRecyclerView;
    private View emptyMessageView;
    private EditText recipientInput;
    private EditText subjectInput;
    private EditText messageInput;
    private MaterialButton sendButton;
    
    private MessageListAdapter adapter;
    private ChatDetailViewModel viewModel;
    
    // 联系人信息
    private long contactId;
    private String contactName;
    private String contactEmail;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        messageRecyclerView = view.findViewById(R.id.messageRecyclerView);
        emptyMessageView = view.findViewById(R.id.emptyMessageView);
        recipientInput = view.findViewById(R.id.recipientInput);
        subjectInput = view.findViewById(R.id.subjectInput);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        
        // 获取参数
        if (getArguments() != null) {
            contactId = getArguments().getLong("contactId", -1);
            contactName = getArguments().getString("contactName", "");
            contactEmail = getArguments().getString("contactEmail", "");
            
            // 自动填充收件人
            if (!contactEmail.isEmpty()) {
                recipientInput.setText(contactEmail);
            }
        }
        
        // 初始化 RecyclerView
        adapter = new MessageListAdapter(new ArrayList<>());
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messageRecyclerView.setAdapter(adapter);
        
        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(ChatDetailViewModel.class);
        
        // 加载消息（使用联系人 ID）
        if (contactId != -1) {
            viewModel.loadMessagesForContact(contactId);
            
            // 自动同步新邮件
            autoSyncEmails();
        }
        
        // 观察消息数据
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null && !messages.isEmpty()) {
                adapter.setMessages(messages);
                messageRecyclerView.setVisibility(View.VISIBLE);
                emptyMessageView.setVisibility(View.GONE);
                
                // 滚动到底部
                messageRecyclerView.scrollToPosition(messages.size() - 1);
                
                // 标记为已读
                viewModel.markAllAsRead();
            } else {
                messageRecyclerView.setVisibility(View.GONE);
                emptyMessageView.setVisibility(View.VISIBLE);
            }
        });
        
        // 同步按钮（长按）
        sendButton.setOnLongClickListener(v -> {
            showSyncDialog();
            return true;
        });
        
        // 发送按钮点击
        sendButton.setOnClickListener(v -> sendMessage());
    }
    
    private void sendMessage() {
        String to = recipientInput.getText().toString().trim();
        String subject = subjectInput.getText().toString().trim();
        String content = messageInput.getText().toString().trim();
        
        if (to.isEmpty() || content.isEmpty()) {
            Snackbar.make(requireView(), "请输入收件人和消息内容", Snackbar.LENGTH_SHORT).show();
            return;
        }
        
        viewModel.sendEmail(to, subject, content, new ChatDetailViewModel.SendCallback() {
            @Override
            public void onSuccess() {
                messageInput.setText("");
                subjectInput.setText("");
                Snackbar.make(requireView(), "邮件已发送", Snackbar.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String error) {
                Snackbar.make(requireView(), "发送失败：" + error, Snackbar.LENGTH_LONG).show();
            }
        });
    }
    
    private void showSyncDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.sync)
                .setMessage("确定要同步新邮件吗？")
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    performSync();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    /**
     * 自动同步邮件（进入聊天界面时调用）
     */
    private void autoSyncEmails() {
        // 静默同步，不显示对话框
        performSync();
    }
    
    /**
     * 执行同步操作
     */
    private void performSync() {
        viewModel.syncEmails(new ChatDetailViewModel.SyncCallback() {
            @Override
            public void onSuccess(int count) {
                if (count > 0) {
                    Snackbar.make(requireView(), 
                        getString(R.string.sync_complete, count), 
                        Snackbar.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onError(String error) {
                // 自动同步时不显示错误提示，避免打扰用户
                // 如果是手动同步，可以显示错误
                android.util.Log.e("ChatDetailFragment", "Sync error: " + error);
            }
        });
    }
}
