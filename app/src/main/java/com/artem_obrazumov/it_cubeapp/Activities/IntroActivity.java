package com.artem_obrazumov.it_cubeapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.artem_obrazumov.it_cubeapp.Models.DirectionModel;
import com.artem_obrazumov.it_cubeapp.Models.ITCubeModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;

public class IntroActivity extends AppCompatActivity {

    // View-элементы
    private Button Login_button, Register_button;
    private TextView forget_password;

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
        setContentView(R.layout.activity_intro);
        // Если пользователь не подписан на главную тему уведомлений, то подписываем
        FirebaseMessaging.getInstance().subscribeToTopic("global_topic");

        // Инициализация элементов
        Login_button = findViewById(R.id.login_button);
        Register_button = findViewById(R.id.register_button);
        forget_password = findViewById(R.id.forget_password);

        // Обработка нажатий на кнопки
        Register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(getApplicationContext(), RegisterActivity.class) ); // Регистрация
            }
        });

        Login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(getApplicationContext(), LoginActivity.class) ); // Авторизация
            }
        });

        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(getApplicationContext(), RecoverPasswordActivity.class) ); // Восстановление пароля
            }
        });
    }
}