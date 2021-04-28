package com.example.harjoitustyo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CreateAccountFragment extends Fragment {

    EditText createUsername, createPassword, confirmPassword;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        createUsername = view.findViewById(R.id.username_create);
        createPassword = view.findViewById(R.id.password_create);
        confirmPassword = view.findViewById(R.id.password_confrim);
        super.onViewCreated(view, savedInstanceState);
    }

    public String getCreateUsername(){
        return createUsername.getText().toString();
    }
    public String getCreatePassword(){
        return createPassword.getText().toString();
    }
    public String getConfirmPassword(){
        return confirmPassword.getText().toString();
    }
}
