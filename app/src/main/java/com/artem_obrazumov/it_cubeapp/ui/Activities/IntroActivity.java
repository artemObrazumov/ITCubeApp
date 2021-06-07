package com.artem_obrazumov.it_cubeapp.ui.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.artem_obrazumov.it_cubeapp.R;
import com.artem_obrazumov.it_cubeapp.databinding.ActivityIntroBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class IntroActivity extends AppCompatActivity {

    // Binding
    private ActivityIntroBinding binding;

    @Override
    protected void onStart() {
        super.onStart();
        // Инициализация системы аунтефикации
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // С помощью SharedPreferences узнаем, запоминали ли мы пользователя
        boolean isSignedIn = getSharedPreferences("remember_me", Context.MODE_PRIVATE)
                        .getBoolean("remembered_me", false);
        // Если нет, то сбрасываем настройки пользователя
        if (!isSignedIn) {
            auth.signOut();
        }

        // Если пользователь уже авторизирован, то сразу переходим к главной активности
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Если пользователь не подписан на главную тему уведомлений, то подписываем
        FirebaseMessaging.getInstance().subscribeToTopic("global_topic");

        // Обработка нажатий на кнопки
        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(getApplicationContext(), RegisterActivity.class) ); // Регистрация
            }
        });

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(getApplicationContext(), LoginActivity.class) ); // Авторизация
            }
        });

        binding.forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(getApplicationContext(), RecoverPasswordActivity.class) ); // Восстановление пароля
            }
        });
    }
}