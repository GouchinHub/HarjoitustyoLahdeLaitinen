package com.example.harjoitustyo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private FragmentManager manager;
    private LoginFragment loginFragment;
    private CreateAccountFragment createAccFragment;
    private static final LoginManager loginManager = LoginManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loadUsers();
        loginFragment = new LoginFragment();
        createAccFragment = new CreateAccountFragment();
        manager = getSupportFragmentManager();

        //Replace default toolbar with custom toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //load login fragment as default view, when activity is first opened
        if (savedInstanceState == null) {
            manager.beginTransaction().replace(R.id.fragment_container,
                    loginFragment).commit();
        }
        //Check if user from previous session is still active.
        loadLoggedInUser();
        //If active user found load straight to
        if (loginManager.getActiveUser() != null) {
            loadMainActivity();
        }
    }

    //change to main activity.
    public void loadMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("user", loginManager.getActiveUser());
        startActivity(intent);
    }

    public void loadCreateAccountFragment(View v) {
        manager.beginTransaction().replace(R.id.fragment_container, createAccFragment).commit();
    }

    public void loadLoginFragment(View v) {
        manager.beginTransaction().replace(R.id.fragment_container, loginFragment).commit();
    }

    //load ArrayList of User object from login manager and save to shared preferences under "user data"
    public void saveUsers() {
        SharedPreferences sharedPreferences = getSharedPreferences("user data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(loginManager.getUsers());
        editor.putString("users", json);
        editor.apply();
    }

    //Method loads all user data from shared preferences and sends list to login manager for storage.
    private void loadUsers() {
        SharedPreferences sharedPreferences = getSharedPreferences("user data", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("users", null);
        Type type = new TypeToken<ArrayList<User>>() {
        }.getType();
        if (gson.fromJson(json, type) != null) {
            loginManager.loadUsers(gson.fromJson(json, type));
        }
    }

    //Method loads data for currently logged user from shared preferences and calls login manager to set active user.
    //In case the is no logged in user, receives null from shared preferences.
    private void loadLoggedInUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("Logged user", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("user", null);
        User user = gson.fromJson(json, User.class);
        loginManager.setActiveUser(user);
    }

    //Method gets username and password from input fields and calls login function
    //if login returns true(successful login), load main activity
    public void loginUser(View v) {
        String username = loginFragment.getLoginUsername();
        String password = loginFragment.getLoginPassword();
        if (loginManager.login(username, password) != null) {
            loadMainActivity();
        } else {
            Toast toast = Toast.makeText(LoginActivity.this, "Invalid username or password!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void loginGuest(View v) {
        loginManager.createGuestAccount();
        loadMainActivity();
    }

    //Method gets username and password from input fields and calls createAccount function.
    public void createNewAccount(View v) {
        String username = createAccFragment.getCreateUsername();
        String password = createAccFragment.getCreatePassword();
        String pwConfirm = createAccFragment.getConfirmPassword();
        String feedBack = loginManager.createAccount(username, password, pwConfirm);
        //save users only if creation was successful.
        if (feedBack.equals("User Created")) {
            saveUsers();
        }
        Toast toast = Toast.makeText(LoginActivity.this, feedBack, Toast.LENGTH_SHORT);
        toast.show();
    }
}