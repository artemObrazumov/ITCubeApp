package com.artem_obrazumov.it_cubeapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.artem_obrazumov.it_cubeapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // View-элементы
    CheckBox remember_me_checkbox;
    EditText input_email, input_password;
    Button submit_button;

    // Окно для уведомления пользователя об авторизации
    ProgressDialog progressDialog;

    // FireBase (Аутентификация)
    private FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Инициализация элементов
        remember_me_checkbox = findViewById(R.id.remember_me_checkbox);
        input_email = findViewById(R.id.input_email);
        input_password = findViewById(R.id.input_password);
        submit_button = findViewById(R.id.submit_button);

        // Инициализируем диалоговое окно
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage( getString(R.string.autorization_process) );

        // Инициализация FireBase
        fbAuth = FirebaseAuth.getInstance();

        // Обработка нажатий на кнопку
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Сбор данных
                String email = input_email.getText().toString().trim();
                String password = input_password.getText().toString().trim();

                if (remember_me_checkbox.isChecked()) {
                    // Запоминаем пользователя
                    getSharedPreferences("remember_me", Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("remembered_me", true)
                            .apply();
                }

                progressDialog.show();

                try {
                    fbAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            progressDialog.dismiss();
                                            if (task.isSuccessful()) {
                                                // Войти в аккаунт удалось
                                                FirebaseUser user = fbAuth.getCurrentUser();

                                                Toast.makeText(getApplicationContext(), getString(R.string.successful_sign_in), Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            } else {
                                                // Не получилось войти в аккаунт
                                                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                                    // Неправильно введена почта
                                                    Toast.makeText(getApplicationContext(), getString(R.string.invalid_email), Toast.LENGTH_LONG).show();
                                                    input_email.setError(getString(R.string.invalid_email));
                                                    input_email.setFocusable(true);
                                                } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                                    // Неправильно введен пароль
                                                    Toast.makeText(getApplicationContext(), getString(R.string.invalid_password), Toast.LENGTH_LONG).show();
                                                    input_password.setError(getString(R.string.invalid_password));
                                                    input_password.setFocusable(true);
                                                }
                                            }
                                        }
                                    }
                            );
                } catch (Exception ignored) {
                    Toast.makeText(LoginActivity.this, getString(R.string.cant_login), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}