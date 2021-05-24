package com.artem_obrazumov.it_cubeapp.Activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.artem_obrazumov.it_cubeapp.Models.ITCubeModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    public static final int GET_IT_CUBE = 100;
    private String selectedCubeID = "0";

    // View-элементы
    private EditText input_name, input_surname, input_email, input_password, input_password_repeat;
    private TextView date_of_birth_selector;
    private Button next_button;

    // Объекты для получения даты рождения
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Date dateOfBirthTime;

    // Окно для уведомления пользователя о регистрации
    ProgressDialog progressDialog;

    // FireBase
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    // Временная информация о пользователе
    private String name, surname, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация элементов
        input_name = findViewById(R.id.input_name);
        input_surname = findViewById(R.id.input_surname);
        input_email = findViewById(R.id.input_email);
        input_password = findViewById(R.id.input_password);
        input_password_repeat = findViewById(R.id.input_password_repeat);
        date_of_birth_selector = findViewById(R.id.date_of_birth_selector);
        next_button = findViewById(R.id.button_next);

        // Инициализация объектов для получения даты рождения
        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 0);
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateOfBirthTime = calendar.getTime();
                updateDateOfBirth();
            }
        };
        date_of_birth_selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Запускаем оукно для выбора даты рождения
                new DatePickerDialog(
                        RegisterActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        // Инициализируем диалоговое окно
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage( getString(R.string.registration_process) );

        // Инициализация FireBase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Обработка нажатий на кнопку
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Сбор данных
                name = input_name.getText().toString().trim();
                surname = input_surname.getText().toString().trim();
                email = input_email.getText().toString().trim();
                password = input_password.getText().toString().trim();
                String password_repeat = input_password_repeat.getText().toString().trim();

                // Валидация данных
                if (name.length() < 3) {
                    // Слишком короткое имя
                    input_name.setError( getString(R.string.short_name) );
                    input_name.setFocusable(true);
                } else if (surname.length() < 3) {
                    // Слишком короткая фамилия
                    input_surname.setError( getString(R.string.short_surname) );
                    input_surname.setFocusable(true);
                }
                else if ( !Patterns.EMAIL_ADDRESS.matcher(email).matches() ) {
                    // Почта введена неверно
                    input_email.setError( getString(R.string.wrong_email) );
                    input_email.setFocusable(true);
                } else if (password.length() < 6) {
                    // Пароль слишком короткий
                    input_password.setError( getString(R.string.short_password) );
                    input_password.setFocusable(true);
                } else if ( !password.equals(password_repeat) ) {
                    // Пароли не совпадают
                    input_password.setError( getString(R.string.wrong_password_repeat) );
                } else if (dateOfBirthTime == null) {
                    // Не выбрана дата рождения
                    Toast.makeText(RegisterActivity.this, getString(R.string.date_of_birth_not_selected), Toast.LENGTH_SHORT).show();
                } else {
                    // Данные верны, предлагаем пользователю войти в Куб
                    Intent intent = new Intent(RegisterActivity.this, CubeSelectActivity.class);
                    startActivityForResult(intent, GET_IT_CUBE);
                }
            }
        });
    }

    private void RegisterUser(String name, String surname, String email, String pass) {
        progressDialog.show();
        //Регистрация пользователя
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Мы успешно зарегистрировались
                            progressDialog.dismiss();

                            // Получаем ID Пользователя
                            String uid = auth.getCurrentUser().getUid();
                            // Собираем данные о пользователя в модель
                            UserModel createdUser = new UserModel(
                                    uid, name, surname, email, calendar.getTimeInMillis(), selectedCubeID, UserModel.STATUS_STUDENT);

                            if (!selectedCubeID.equals("0")) {
                                // Если был выбран куб, то записываем пользователя в список учеников
                                subscribeToCube(selectedCubeID, uid);
                            }

                            // Заполняем базу данными
                            DatabaseReference reference = database.getReference("Users_data");
                            reference.push().setValue(createdUser);

                            Toast.makeText(getApplicationContext(), getString( R.string.account_created ), Toast.LENGTH_SHORT).show();

                            // Запоминаем пользователя
                            getSharedPreferences("remember_me", Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("remembered_me", true)
                                    .apply();

                            // Переходим на другую активность
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("canShowCubeSelect", false);
                            startActivity(intent);
                        } else {
                            // Произошла ошибка. Выводим сообщение о неудаче.
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), getString( R.string.account_creating_error ), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
            .addOnFailureListener(RegisterActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Произошла ошибка. Выводим сообщение о неудаче.
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), getString( R.string.account_creating_error ), Toast.LENGTH_SHORT).show();
                    Log.e( getString(R.string.app_name), e.getMessage() );
                }
            });
    }

    // Запись ученика в куб
    private void subscribeToCube(String cubeID, String uid) {
        ITCubeModel.getCubeQuery(cubeID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ITCubeModel cube = ds.getValue(ITCubeModel.class);
                    HashMap<String, Object> updatedData = new HashMap<>();
                    ArrayList<String> updatedStudentsList = cube.getStudents();
                    if (updatedStudentsList == null) {
                        updatedStudentsList = new ArrayList<>();
                    }
                    updatedStudentsList.add(uid);
                    updatedData.put("students", updatedStudentsList);

                    database.getReference("IT_Cubes").child(cubeID).updateChildren(updatedData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_loading), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Обновление даты рождения в строке
    private void updateDateOfBirth() {
        String dateOfBirthString = new SimpleDateFormat("dd.MM.yyyy", Locale.US).format(dateOfBirthTime);
        date_of_birth_selector.setText(
                String.format( "Дата рождения: %s", dateOfBirthString )
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_IT_CUBE && resultCode == RESULT_OK && data != null) {
            // Получаем идентификатор куба
            selectedCubeID = data.getStringExtra("selectedCubeID");
            // Продолжаем регистрацию
            RegisterUser(name, surname, email, password);
        }
    }
}