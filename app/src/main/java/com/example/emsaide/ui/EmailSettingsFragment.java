package com.example.emsaide.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.emsaide.R;
import com.example.emsaide.data.model.EmailAccount;
import com.example.emsaide.data.repository.EmailRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 邮箱设置界面
 */
public class EmailSettingsFragment extends Fragment {
    
    private static final String TAG = "EmailSettingsFragment";
    
    private Spinner providerSpinner;
    private TextInputEditText accountNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private CheckBox useImapCheckbox;
    private CheckBox useSslCheckbox;
    private View pop3Layout;
    private View imapLayout;
    private TextInputEditText pop3ServerInput;
    private TextInputEditText pop3PortInput;
    private TextInputEditText imapServerInput;
    private TextInputEditText imapPortInput;
    private TextInputEditText smtpServerInput;
    private TextInputEditText smtpPortInput;
    private MaterialButton testConnectionButton;
    private MaterialButton saveButton;
    
    private EmailRepository repository;
    private ExecutorService executor;
    
    private Long editAccountId; // 如果是编辑模式，保存账户 ID
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_email_settings, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // 初始化视图
        providerSpinner = view.findViewById(R.id.providerSpinner);
        accountNameInput = view.findViewById(R.id.accountNameInput);
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        useImapCheckbox = view.findViewById(R.id.useImapCheckbox);
        useSslCheckbox = view.findViewById(R.id.useSslCheckbox);
        pop3Layout = view.findViewById(R.id.pop3Layout);
        imapLayout = view.findViewById(R.id.imapLayout);
        pop3ServerInput = view.findViewById(R.id.pop3ServerInput);
        pop3PortInput = view.findViewById(R.id.pop3PortInput);
        imapServerInput = view.findViewById(R.id.imapServerInput);
        imapPortInput = view.findViewById(R.id.imapPortInput);
        smtpServerInput = view.findViewById(R.id.smtpServerInput);
        smtpPortInput = view.findViewById(R.id.smtpPortInput);
        testConnectionButton = view.findViewById(R.id.testConnectionButton);
        saveButton = view.findViewById(R.id.saveButton);
        
        repository = new EmailRepository(requireContext());
        executor = Executors.newSingleThreadExecutor();
        
        // 检查是否是编辑模式
        if (getArguments() != null && getArguments().containsKey("accountId")) {
            editAccountId = getArguments().getLong("accountId");
            // 只有有效的 ID 才进入编辑模式
            if (editAccountId > 0) {
                loadAccountData();
            } else {
                editAccountId = null; // 无效 ID，视为新建模式
            }
        }
        
        // 设置监听器
        setupListeners();
    }
    
    private void setupListeners() {
        // 服务商选择
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.email_providers,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        providerSpinner.setAdapter(adapter);
        
        providerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fillProviderSettings(position);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // IMAP/POP3切换
        useImapCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateServerVisibility();
        });
        
        // SSL 切换
        useSslCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updatePortsForSsl();
        });
        
        // 测试连接
        testConnectionButton.setOnClickListener(v -> testConnection());
        
        // 保存
        saveButton.setOnClickListener(v -> saveAccount());
    }
    
    private void updateServerVisibility() {
        if (useImapCheckbox.isChecked()) {
            pop3Layout.setVisibility(View.GONE);
            imapLayout.setVisibility(View.VISIBLE);
        } else {
            pop3Layout.setVisibility(View.VISIBLE);
            imapLayout.setVisibility(View.GONE);
        }
    }
    
    private void updatePortsForSsl() {
        boolean ssl = useSslCheckbox.isChecked();
        
        if (useImapCheckbox.isChecked()) {
            // IMAP
            if (TextUtils.isEmpty(imapPortInput.getText())) {
                imapPortInput.setText(ssl ? "993" : "143");
            }
        } else {
            // POP3
            if (TextUtils.isEmpty(pop3PortInput.getText())) {
                pop3PortInput.setText(ssl ? "995" : "110");
            }
        }
        
        // SMTP
        if (TextUtils.isEmpty(smtpPortInput.getText())) {
            smtpPortInput.setText(ssl ? "465" : "587");
        }
    }
    
    private void fillProviderSettings(int position) {
        String[] pop3Servers = {
            "", // 占位
            "pop.gmail.com",     // Gmail
            "pop.qq.com",        // QQ
            "pop.163.com",       // 163
            "pop.126.com",       // 126
            "outlook.office365.com", // Outlook
            "pop.mail.yahoo.com", // Yahoo
            ""                   // 其他
        };
        
        String[] imapServers = {
            "", 
            "imap.gmail.com",
            "imap.qq.com",
            "imap.163.com",
            "imap.126.com",
            "outlook.office365.com",
            "imap.mail.yahoo.com",
            ""
        };
        
        String[] smtpServers = {
            "",
            "smtp.gmail.com",
            "smtp.qq.com",
            "smtp.163.com",
            "smtp.126.com",
            "smtp.office365.com",
            "smtp.mail.yahoo.com",
            ""
        };
        
        int[] pop3PortsSsl = {0, 995, 995, 995, 995, 995, 995, 0};
        int[] imapPortsSsl = {0, 993, 993, 993, 993, 993, 993, 0};
        int[] smtpPortsSsl = {0, 465, 465, 465, 465, 465, 465, 0};
        
        if (position > 0 && position <= 6) {
            pop3ServerInput.setText(pop3Servers[position]);
            imapServerInput.setText(imapServers[position]);
            smtpServerInput.setText(smtpServers[position]);
            
            useSslCheckbox.setChecked(true);
            pop3PortInput.setText(String.valueOf(pop3PortsSsl[position]));
            imapPortInput.setText(String.valueOf(imapPortsSsl[position]));
            smtpPortInput.setText(String.valueOf(smtpPortsSsl[position]));
        }
    }
    
    private void loadAccountData() {
        executor.execute(() -> {
            EmailAccount account = repository.getAccountById(editAccountId);
            if (account != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    accountNameInput.setText(account.getAccountName());
                    emailInput.setText(account.getEmail());
                    passwordInput.setText(account.getPassword());
                    useImapCheckbox.setChecked(account.isUseImap());
                    useSslCheckbox.setChecked(account.isUseSsl());
                    
                    pop3ServerInput.setText(account.getPop3Server());
                    pop3PortInput.setText(String.valueOf(account.getPop3Port()));
                    
                    imapServerInput.setText(account.getImapServer());
                    imapPortInput.setText(String.valueOf(account.getImapPort()));
                    
                    smtpServerInput.setText(account.getSmtpServer());
                    smtpPortInput.setText(String.valueOf(account.getSmtpPort()));
                    
                    updateServerVisibility();
                });
            }
        });
    }
    
    private void testConnection() {
        EmailAccount account = createAccountFromInputs();
        if (account == null) {
            return;
        }
        
        Toast.makeText(getContext(), "正在测试连接...", Toast.LENGTH_SHORT).show();
        
        executor.execute(() -> {
            boolean success = repository.testConnection(account);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(), R.string.connection_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    private void saveAccount() {
        EmailAccount account = createAccountFromInputs();
        if (account == null) {
            return;
        }
        
        executor.execute(() -> {
            try {
                long id;
                if (editAccountId != null) {
                    account.setId(editAccountId);
                    repository.updateAccount(account);
                    id = editAccountId;
                    Log.d(TAG, "updateAccount: id = " + id + ", email = " + account.getEmail());
                } else {
                    id = repository.insertAccount(account);
                    // insertAccount 内部已经有日志
                }
                
                Log.d(TAG, "saveAccount completed: id = " + id);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), R.string.success, Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Save account error", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    
    @Nullable
    private EmailAccount createAccountFromInputs() {
        String accountName = getText(accountNameInput);
        String email = getText(emailInput);
        String password = getText(passwordInput);
        
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show();
            return null;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), R.string.invalid_email_format, Toast.LENGTH_SHORT).show();
            return null;
        }
        
        EmailAccount account = new EmailAccount();
        account.setAccountName(TextUtils.isEmpty(accountName) ? email : accountName);
        account.setEmail(email);
        account.setPassword(password);
        account.setUseImap(useImapCheckbox.isChecked());
        account.setUseSsl(useSslCheckbox.isChecked());
        
        // POP3
        account.setPop3Server(getText(pop3ServerInput));
        String pop3Port = getText(pop3PortInput);
        account.setPop3Port(TextUtils.isEmpty(pop3Port) ? 995 : Integer.parseInt(pop3Port));
        
        // IMAP
        account.setImapServer(getText(imapServerInput));
        String imapPort = getText(imapPortInput);
        account.setImapPort(TextUtils.isEmpty(imapPort) ? 993 : Integer.parseInt(imapPort));
        
        // SMTP
        account.setSmtpServer(getText(smtpServerInput));
        String smtpPort = getText(smtpPortInput);
        account.setSmtpPort(TextUtils.isEmpty(smtpPort) ? 465 : Integer.parseInt(smtpPort));
        
        return account;
    }
    
    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
