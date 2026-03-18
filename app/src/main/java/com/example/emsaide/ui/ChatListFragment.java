package com.example.emsaide.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emsaide.R;
import com.example.emsaide.data.model.EmailAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天列表界面
 */
public class ChatListFragment extends Fragment {
    
    private RecyclerView chatRecyclerView;
    private View emptyView;
    private FloatingActionButton addAccountFab;
    
    private ChatListAdapter adapter;
    private ChatListViewModel viewModel;
    
    private OnChatSelectedListener listener;
    
    public interface OnChatSelectedListener {
        void onChatSelected(long accountId);
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnChatSelectedListener) {
            listener = (OnChatSelectedListener) context;
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        addAccountFab = view.findViewById(R.id.addAccountFab);
        
        // 初始化 RecyclerView
        adapter = new ChatListAdapter(new ArrayList<>(), this::onAccountClick);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(adapter);
        
        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);
        
        // 观察数据
        viewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> {
            if (accounts != null && !accounts.isEmpty()) {
                adapter.setAccounts(accounts);
                chatRecyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            } else {
                chatRecyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
        
        // 添加按钮点击事件
        addAccountFab.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_chatList_to_emailSettings);
        });
    }
    
    private void onAccountClick(EmailAccount account) {
        if (listener != null) {
            listener.onChatSelected(account.getId());
        } else {
            // 如果没有监听器，使用 Navigation
            Bundle bundle = new Bundle();
            bundle.putLong("accountId", account.getId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.chatDetailFragment, bundle);
        }
    }
}
