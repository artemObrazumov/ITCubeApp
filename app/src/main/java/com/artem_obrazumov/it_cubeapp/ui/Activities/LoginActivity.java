package com.artem_obrazumov.it_cubeapp.ui.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.artem_obrazumov.it_cubeapp.R;
import com.artem_obrazumov.it_cubeapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    // Binding
    private ActivityLoginBinding binding;

    // Окно для уведомления пользователя об авторизации
    private ProgressDialog progressDialog;

    // FireBase (Аутентификация)
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Инициализация элементов
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage( getString(R.string.autorization_process) );
        auth = FirebaseAuth.getInstance();

        // Обработка нажатий на кнопку
        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    // Вход в аккаунт
    private void signIn() {
        // Сбор данных
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();

        if (binding.rememberMeCheckbox.isChecked()) {
            // Запоминаем пользователя
            getSharedPreferences("remember_me", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("remembered_me", true)
                    .apply();
        }

        progressDialog.show();

        try {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        // Войти в аккаунт удалось
                                        Toast.makeText(getApplicationContext(), getString(R.string.successful_sign_in), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    } else {
                                        // Не получилось войти в аккаунт
                                        manageException(task.getException());
                                    }
                                }
                            }
                    );
        } catch (Exception ignored) {
            Toast.makeText(LoginActivity.this, getString(R.string.cant_login), Toast.LENGTH_SHORT).show();
        }
    }

    // Обработка исключений при авторизации
    private void manageException(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            // Неправильно введена почта
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_email), Toast.LENGTH_LONG).show();
            binding.inputEmail.setError(getString(R.string.invalid_email));
            binding.inputEmail.setFocusable(true);
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            // Неправильно введен пароль
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_password), Toast.LENGTH_LONG).show();
            binding.inputPassword.setError(getString(R.string.invalid_password));
            binding.inputPassword.setFocusable(true);
        }
    }
}