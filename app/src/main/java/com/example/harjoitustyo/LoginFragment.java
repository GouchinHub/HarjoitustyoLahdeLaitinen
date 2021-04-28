package com.example.harjoitustyo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {

    EditText loginUsername, loginPassword;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        loginUsername = view.findViewById(R.id.username_input);
        loginPassword = view.findViewById(R.id.password_input);
        super.onViewCreated(view, savedInstanceState);
    }

    public String getLoginUsername() {
        return loginUsername.getText().toString();
    }

    public String getLoginPassword() {
        return loginPassword.getText().toString();
    }
}
