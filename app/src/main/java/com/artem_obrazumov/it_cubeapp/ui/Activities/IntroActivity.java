package com.artem_obrazumov.it_cubeapp.ui.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.artem_obrazumov.it_cubeapp.Models.UserModel;
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

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        // Обработка нажатий на кнопки
        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                new AlertDialog.Builder(IntroActivity.this)
                        .setTitle(getString(R.string.select_user_status))
                        .setItems(new String[]{ getString(R.string.user_account), getString(R.string.parent_account) },
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                // Собственный аккаунт
                                                intent.putExtra("userStatus", UserModel.STATUS_STUDENT);
                                                break;
                                            case 1:
                                                // Аккаунт родителя
                                                intent.putExtra("userStatus", UserModel.STATUS_PARENT);
                                                break;
                                        }
                                        startActivity(intent);       // Регистрация
                                    }
                                })
                        .create().show();
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