package com.artem_obrazumov.it_cubeapp.ui.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.artem_obrazumov.it_cubeapp.databinding.ActivityChildCreationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import static com.artem_obrazumov.it_cubeapp.ui.Activities.RegisterActivity.GET_IT_CUBE;

public class ChildRegistrationActivity extends AppCompatActivity {

    // Binding
    private ActivityChildCreationBinding binding;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    // Объекты для получения даты рождения
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Date dateOfBirthTime;

    // Объект ребенка
    private final UserModel child = new UserModel();

    private String selectedCubeID = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChildCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryRegisterChild();
            }
        });

        initializeDatePicker();
    }

    // Попытка регистрации ребенка в бд
    private void tryRegisterChild() {
        if (valid()) {
            fillChildFields();
            // Данные верны, предлагаем пользователю войти в Куб
            Intent intent = new Intent(ChildRegistrationActivity.this, CubeSelectActivity.class);
            intent.putExtra("canSkip", false);
            startActivityForResult(intent, GET_IT_CUBE);
        }
    }

    private void initializeDatePicker() {
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
        binding.dateOfBirthSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Запускаем оукно для выбора даты рождения
                new DatePickerDialog(
                        ChildRegistrationActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });
    }

    // Отображение даты рождения
    private void updateDateOfBirth() {
        String dateOfBirthString = new SimpleDateFormat("dd.MM.yyyy", Locale.US).format(dateOfBirthTime);
        binding.dateOfBirthSelector.setText(
                String.format( "Дата рождения: %s", dateOfBirthString )
        );
    }

    // Проверка валидности заполнения полей
    private boolean valid() {
        if (binding.inputName.getText().toString().trim().length() < 3) {
            binding.inputName.setError(getString(R.string.short_name));
            binding.inputName.requestFocus();
            return false;
        }
        if (binding.inputSurname.getText().toString().trim().length() < 3) {
            binding.inputSurname.setError(getString(R.string.short_surname));
            binding.inputSurname.requestFocus();
            return false;
        }
        if (dateOfBirthTime == null) {
            // Не выбрана дата рождения
            Toast.makeText(ChildRegistrationActivity.this,
                    getString(R.string.date_of_birth_not_selected), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Заполнения полей ребенка
    private void fillChildFields() {
        child.setName(binding.inputName.getText().toString().trim());
        child.setSurname(binding.inputSurname.getText().toString().trim());
        child.setDateOfBirth(calendar.getTimeInMillis());
        child.setUid(UUID.randomUUID().toString());
        child.setParentID(auth.getCurrentUser().getUid());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_IT_CUBE && resultCode == RESULT_OK && data != null) {
            // Получаем идентификатор куба
            selectedCubeID = data.getStringExtra("selectedCubeID");
            child.setCubeId(selectedCubeID);
            // Продолжаем регистрацию
            registerChild();
        }
    }

    // Регистрация ребенка в бд
    private void registerChild() {
        DatabaseReference reference = database.getReference("Users_data/" + child.getUid());
        reference.setValue(child);
        // Добавление ребенка в список детей пользователя
        DatabaseReference parentReference =
                database.getReference( String.format( "Children_list/%s/%s",
                        auth.getCurrentUser().getUid(), child.getUid()
                ));
        parentReference.setValue(true);
        finish();
    }
}