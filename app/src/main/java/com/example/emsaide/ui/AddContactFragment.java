package com.example.emsaide.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.emsaide.R;
import com.example.emsaide.data.database.AppDatabase;
import com.example.emsaide.data.model.Contact;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 添加联系人界面
 */
public class AddContactFragment extends Fragment {
    
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText remarkInput;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;
    
    private AppDatabase database;
    private ExecutorService executor;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_contact, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nameInput = view.findViewById(R.id.nameInput);
        emailInput = view.findViewById(R.id.emailInput);
        remarkInput = view.findViewById(R.id.remarkInput);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        
        database = AppDatabase.getInstance(requireContext());
        executor = Executors.newSingleThreadExecutor();
        
        // 保存按钮
        saveButton.setOnClickListener(v -> saveContact());
        
        // 取消按钮
        cancelButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).popBackStack();
        });
    }
    
    private void saveContact() {
        String name = getText(nameInput);
        String email = getText(emailInput);
        String remark = getText(remarkInput);
        
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "请输入邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "邮箱格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        
        executor.execute(() -> {
            try {
                Contact contact = new Contact();
                contact.setName(TextUtils.isEmpty(name) ? email : name);
                contact.setEmail(email);
                contact.setRemark(remark);
                
                database.contactDao().insert(contact);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "联系人已添加", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "添加失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    
    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
