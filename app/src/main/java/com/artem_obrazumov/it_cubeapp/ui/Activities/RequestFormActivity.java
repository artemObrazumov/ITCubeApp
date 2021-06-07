package com.artem_obrazumov.it_cubeapp.ui.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.artem_obrazumov.it_cubeapp.Models.DirectionModel;
import com.artem_obrazumov.it_cubeapp.Models.ProgramModel;
import com.artem_obrazumov.it_cubeapp.Models.RequestModel;
import com.artem_obrazumov.it_cubeapp.Models.ScheduleModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.artem_obrazumov.it_cubeapp.Services.MessageService;
import com.artem_obrazumov.it_cubeapp.Tasks;
import com.artem_obrazumov.it_cubeapp.databinding.ActivityRequestFormBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RequestFormActivity extends AppCompatActivity {

    private String directionID;
    private String cubeID;
    private DirectionModel direction;
    private ArrayList<ProgramModel> programs;
    private ArrayList<ScheduleModel> schedules;

    // Binding
    private ActivityRequestFormBinding binding;

    // Окно для уведомления пользователя о загрузке
    private ProgressDialog progressDialog;

    // Объекты для получения даты рождения
    private Calendar dateOfBirthCalendar;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Date dateOfBirthTime;

    // FireBase
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    // Объект запроса
    private RequestModel requestFormData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Так как нам важно наличие идентификатора направления, мы ищем его в первую очередь
        getDirectionID();
        getDirectionData();
        setupActionBar(getString(R.string.action_add_new_course));

        programs = new ArrayList<>();
        schedules = new ArrayList<>();

        // Инициализация FireBase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        // Инииализация объектов для получения даты
        dateOfBirthCalendar = Calendar.getInstance();
        dateOfBirthTime = new Date();
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                dateOfBirthCalendar.set(Calendar.YEAR, year);
                dateOfBirthCalendar.set(Calendar.MONTH, month);
                dateOfBirthCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateOfBirthTime = dateOfBirthCalendar.getTime();
                updateDateOfBirth();
            }
        };
        binding.dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Запускаем оукно для выбора даты рождения
                new DatePickerDialog(
                        RequestFormActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        dateOfBirthCalendar.get(Calendar.YEAR),
                        dateOfBirthCalendar.get(Calendar.MONTH),
                        dateOfBirthCalendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });
        progressDialog = new ProgressDialog(this);

        getUserDataAndSetupUI(auth.getCurrentUser().getUid());
    }

    private void getDirectionID() {
        directionID = getIntent().getStringExtra("directionID");
        if (directionID == null) {
            // Если нам не удалось получить идентификатор направления, то закрываем форму
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Получение информации о направлении
    private void getDirectionData() {
        DirectionModel.getDirectionQuery(directionID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            direction = ds.getValue(DirectionModel.class);
                            binding.formTitle.setText(direction.getTitle());
                            binding.formDescription.setText(direction.getDescription());
                            getPrograms();
                            getSchedules();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );
    }

    // Получение программ направления
    private void getPrograms() {
        ArrayList<String> programTitles = new ArrayList<>();
        ArrayAdapter<String> programsAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_element);
        binding.activityProgram.setAdapter(programsAdapter);

        for (int i = 0; i < direction.getPrograms().size(); i++) {
            String programID = direction.getPrograms().get(i);
            ProgramModel.getProgramQuery(programID).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                ProgramModel program = ds.getValue(ProgramModel.class);
                                programs.add(program);
                                programTitles.add(program.getTitle());

                                programsAdapter.clear();
                                programsAdapter.addAll(programTitles);
                                programsAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    // Получение программ направления
    private void getSchedules() {
        ArrayList<String> scheduleStrings = new ArrayList<>();
        ArrayAdapter<String> schedulesAdapter =
                new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item);
        binding.programSchedule.setAdapter(schedulesAdapter);

        for (int i = 0; i < direction.getSchedules().size(); i++) {
            String scheduleID = direction.getSchedules().get(i);
            ScheduleModel.getScheduleQuery(scheduleID).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                ScheduleModel schedule = ds.getValue(ScheduleModel.class);
                                schedules.add(schedule);
                                scheduleStrings.add(schedule.toString());

                                schedulesAdapter.clear();
                                schedulesAdapter.addAll(scheduleStrings);
                                schedulesAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    // Настройка верхней панели
    private void setupActionBar(String string) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(string);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Метод для получения информации о пользователе
    private void getUserDataAndSetupUI(String uid) {
        progressDialog.setMessage(getString(R.string.loading));
        UserModel.getUserQuery(uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            UserModel user = ds.getValue(UserModel.class);
                            cubeID = user.getCubeId();
                            setupUI(user);
                        }
                        progressDialog.hide();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.load_user_data_error), Toast.LENGTH_SHORT).show();
                        progressDialog.hide();
                    }
                }
        );
    }

    // Метод для установки значений в поля
    private void setupUI(UserModel user) {
        binding.inputName.setText(user.getName());
        binding.inputSurname.setText(user.getSurname());
        dateOfBirthCalendar.setTimeInMillis(user.getDateOfBirth());
        dateOfBirthTime.setTime(dateOfBirthCalendar.getTimeInMillis());
        updateDateOfBirth();
        binding.inputEmail.setText(user.getEmail());
    }

    // Обновление даты рождения в строке
    private void updateDateOfBirth() {
        String dateOfBirthString = new SimpleDateFormat("dd.MM.yyyy", Locale.US).format(dateOfBirthTime);

        binding.dateOfBirth.setText(
                String.format( "Дата рождения: %s г.", dateOfBirthString )
        );
    }

    // Метод для валидации формы
    private boolean validateForm() {
        // Сбор данных
        String name = binding.inputName.getText().toString().trim();
        String surname = binding.inputSurname.getText().toString().trim();
        String patronymic = binding.inputPatronymic.getText().toString().trim();
        String email = binding.inputEmail.getText().toString().trim();
        String parent_name = binding.inputParentName.getText().toString().trim();
        String parent_surname = binding.inputParentSurname.getText().toString().trim();
        String parent_patronymic = binding.inputParentPatronymic.getText().toString().trim();
        String teacher_name = binding.inputTeacherName.getText().toString().trim();
        String teacher_surname = binding.inputTeacherSurname.getText().toString().trim();
        String teacher_patronymic = binding.inputTeacherPatronymic.getText().toString().trim();
        String phone = binding.inputPhone.getText().toString().trim();
        String parent_phone = binding.inputParentPhone.getText().toString().trim();
        String form = binding.formNumberSelect.getSelectedItem().toString() +
                      binding.formLetterSelect.getSelectedItem().toString();
        String school = binding.schoolSelect.getSelectedItem().toString();

        // Валидация данных
        if (name.length() < 3) {
            // Слишком короткое имя
            binding.inputName.setError( getString(R.string.short_name) );
            binding.inputName.requestFocus();
        } else if (surname.length() < 3) {
            // Слишком короткая фамилия
            binding.inputSurname.setError( getString(R.string.short_surname) );
            binding.inputSurname.requestFocus();;
        } else if (patronymic.length() < 3) {
            // Слишком короткое отчество
            binding.inputPatronymic.setError( getString(R.string.short_patronymic) );
            binding.inputPatronymic.requestFocus();;
        } else if ( !Patterns.EMAIL_ADDRESS.matcher(email).matches() ) {
            // Почта введена неверно
            binding.inputEmail.setError( getString(R.string.wrong_email) );
            binding.inputEmail.requestFocus();
        } else if (parent_name.length() < 3) {
            // Слишком короткое имя родителя
            binding.inputParentName.setError( getString(R.string.short_name) );
            binding.inputParentName.requestFocus();
        } else if (parent_surname.length() < 3) {
            // Слишком короткая фамилия родителя
            binding.inputParentSurname.setError( getString(R.string.short_surname) );
            binding.inputParentSurname.requestFocus();
        } else if (parent_patronymic.length() < 3) {
            // Слишком короткое отчество родителя
            binding.inputParentPatronymic.setError( getString(R.string.short_patronymic) );
            binding.inputParentPatronymic.requestFocus();
        } else if (teacher_name.length() < 3) {
            // Слишком короткое имя классного руководителя
            binding.inputTeacherName.setError( getString(R.string.short_name) );
            binding.inputTeacherName.requestFocus();
        } else if (teacher_surname.length() < 3) {
            // Слишком короткая фамилия классного руководителя
            binding.inputTeacherSurname.setError( getString(R.string.short_surname) );
            binding.inputTeacherSurname.requestFocus();
        } else if (teacher_patronymic.length() < 3) {
            // Слишком короткое отчество классного руководителя
            binding.inputTeacherPatronymic.setError( getString(R.string.short_patronymic) );
            binding.inputTeacherPatronymic.requestFocus();
        } else {
            try {
                // Данные валидны, заполняем форму для отправки
                String programID = programs.get(binding.activityProgram.getSelectedItemPosition()).getId();
                String scheduleID = schedules.get(binding.programSchedule.getSelectedItemPosition()).getId();
                String scheduleString = schedules.get(binding.programSchedule.getSelectedItemPosition()).toString();
                requestFormData = new RequestModel("0", auth.getCurrentUser().getUid(), email, phone, parent_phone,
                        (surname + " " + name + " " + patronymic), (parent_surname + " " + parent_name + " " + parent_patronymic),
                        (teacher_surname + " " + teacher_name + " " + teacher_patronymic), school, form,
                        dateOfBirthCalendar.getTimeInMillis(), directionID, programID, scheduleID, cubeID,
                        direction.getTitle(), scheduleString);
                return true;
            } catch (Exception ignored) {
                Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return false;
    }

    // Отправка формы
    private void submitForm() {
        // Загрузка объекта формы в БД
        DatabaseReference reference = database.getReference("Request_forms");
        DatabaseReference keyReference = reference.push();
        requestFormData.setId(keyReference.getKey());
        keyReference.setValue(requestFormData);

        // Отправка уведомления администраторам
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = createNotificationRequestJSON();
                new Tasks.NotificationSender(getApplicationContext(), json).doInBackground();
            }
        }).start();

        Toast.makeText(this, getString(R.string.form_was_sent), Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    // Создание JSON-файла для отправки уведомления
    private JSONObject createNotificationRequestJSON() {
        try {
            JSONObject notificationJSON = new JSONObject();
            JSONObject notificationDataJSON = new JSONObject();
            String title = getString(R.string.new_request_part) + direction.getTitle();

            notificationDataJSON.put("messageType", MessageService.NEW_REQUEST_NOTIFICATION);
            notificationDataJSON.put("title", title);

            notificationJSON.put("data", notificationDataJSON);
            notificationJSON.put("to", "/topics/global_topic");

            return notificationJSON;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_submit) {
            // Кнопка записи на новый курс
            if (validateForm()) {
                new AlertDialog.Builder(RequestFormActivity.this)
                        .setTitle(getString(R.string.want_to_send_form))
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                submitForm();
                            }
                        })
                        .create().show();
            }
        } else {
            // Кнопка "назад"
            new AlertDialog.Builder(RequestFormActivity.this)
                    .setMessage(getString(R.string.want_to_leave_form))
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onBackPressed();
                        }
                    })
                    .create().show();
        }
        return super.onOptionsItemSelected(item);
    }
}