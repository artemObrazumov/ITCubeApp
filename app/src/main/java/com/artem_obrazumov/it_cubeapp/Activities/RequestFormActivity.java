package com.artem_obrazumov.it_cubeapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.HashMap;
import java.util.Locale;

public class RequestFormActivity extends AppCompatActivity {

    private String directionID;
    private String cubeID;
    private DirectionModel direction;
    private ArrayList<ProgramModel> programs;
    private ArrayList<ScheduleModel> schedules;

    // View-элементы
    private TextView form_title, form_description;
    private EditText
        input_name, input_surname, input_patronymic, input_phone,
        input_email, input_parent_name, input_parent_surname, input_parent_patronymic,
        input_parent_phone, input_teacher_name, input_teacher_surname, input_teacher_patronymic;
    private TextView date_of_birth;
    private Spinner activity_program, program_schedule, school_select, form_number_select, form_letter_select;

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
        setContentView(R.layout.activity_request_form);
        // Так как нам важно наличие идентификатора направления, мы ищем его в первую очередь
        getDirectionID();
        getDirectionData();
        setupActionBar(getString(R.string.action_add_new_course));
        initializeViewElements();

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
        date_of_birth.setOnClickListener(new View.OnClickListener() {
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
                            form_title.setText(direction.getTitle());
                            form_description.setText(direction.getDescription());
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
        activity_program.setAdapter(programsAdapter);

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
        program_schedule.setAdapter(schedulesAdapter);

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

    /* Так как нужно инициализировать достаточно много объектов,
       был создан отдельный метод для этого */
    private void initializeViewElements() {
        form_title = findViewById(R.id.form_title);
        form_description = findViewById(R.id.form_description);
        input_name = findViewById(R.id.input_name);
        input_surname = findViewById(R.id.input_surname);
        input_patronymic = findViewById(R.id.input_patronymic);
        input_email = findViewById(R.id.input_email);
        input_phone = findViewById(R.id.input_phone);
        input_parent_name = findViewById(R.id.input_parent_name);
        input_parent_surname = findViewById(R.id.input_parent_surname);
        input_parent_patronymic = findViewById(R.id.input_parent_patronymic);
        input_parent_phone = findViewById(R.id.input_parent_phone);
        input_teacher_name = findViewById(R.id.input_teacher_name);
        input_teacher_surname = findViewById(R.id.input_teacher_surname);
        input_teacher_patronymic = findViewById(R.id.input_teacher_patronymic);
        date_of_birth = findViewById(R.id.date_of_birth);
        school_select = findViewById(R.id.school_select);
        activity_program = findViewById(R.id.activity_program);
        program_schedule = findViewById(R.id.program_schedule);
        form_number_select = findViewById(R.id.form_number_select);
        form_letter_select = findViewById(R.id.form_letter_select);
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
        input_name.setText(user.getName());
        input_surname.setText(user.getSurname());
        dateOfBirthCalendar.setTimeInMillis(user.getDateOfBirth());
        dateOfBirthTime.setTime(dateOfBirthCalendar.getTimeInMillis());
        updateDateOfBirth();
        input_email.setText(user.getEmail());
    }

    // Обновление даты рождения в строке
    private void updateDateOfBirth() {
        String dateOfBirthString = new SimpleDateFormat("dd.MM.yyyy", Locale.US).format(dateOfBirthTime);

        date_of_birth.setText(
                String.format( "Дата рождения: %s г.", dateOfBirthString )
        );
    }

    // Метод для валидации формы
    private boolean validateForm() {
        // Сбор данных
        String name = input_name.getText().toString().trim();
        String surname = input_surname.getText().toString().trim();
        String patronymic = input_patronymic.getText().toString().trim();
        String email = input_email.getText().toString().trim();
        String parent_name = input_parent_name.getText().toString().trim();
        String parent_surname = input_parent_surname.getText().toString().trim();
        String parent_patronymic = input_parent_patronymic.getText().toString().trim();
        String teacher_name = input_teacher_name.getText().toString().trim();
        String teacher_surname = input_teacher_surname.getText().toString().trim();
        String teacher_patronymic = input_teacher_patronymic.getText().toString().trim();
        String phone = input_phone.getText().toString().trim();
        String parent_phone = input_parent_phone.getText().toString().trim();
        String form = form_number_select.getSelectedItem().toString() +
                      form_letter_select.getSelectedItem().toString();
        String school = school_select.getSelectedItem().toString();

        // Валидация данных
        if (name.length() < 3) {
            // Слишком короткое имя
            input_name.setError( getString(R.string.short_name) );
            input_name.requestFocus();
        } else if (surname.length() < 3) {
            // Слишком короткая фамилия
            input_surname.setError( getString(R.string.short_surname) );
            input_surname.requestFocus();;
        } else if (patronymic.length() < 3) {
            // Слишком короткое отчество
            input_patronymic.setError( getString(R.string.short_patronymic) );
            input_patronymic.requestFocus();;
        } else if ( !Patterns.EMAIL_ADDRESS.matcher(email).matches() ) {
            // Почта введена неверно
            input_email.setError( getString(R.string.wrong_email) );
            input_email.requestFocus();
        } else if (parent_name.length() < 3) {
            // Слишком короткое имя родителя
            input_parent_name.setError( getString(R.string.short_name) );
            input_parent_name.requestFocus();
        } else if (parent_surname.length() < 3) {
            // Слишком короткая фамилия родителя
            input_parent_surname.setError( getString(R.string.short_surname) );
            input_parent_surname.requestFocus();
        } else if (parent_patronymic.length() < 3) {
            // Слишком короткое отчество родителя
            input_parent_patronymic.setError( getString(R.string.short_patronymic) );
            input_parent_patronymic.requestFocus();
        } else if (teacher_name.length() < 3) {
            // Слишком короткое имя классного руководителя
            input_teacher_name.setError( getString(R.string.short_name) );
            input_teacher_name.requestFocus();
        } else if (teacher_surname.length() < 3) {
            // Слишком короткая фамилия классного руководителя
            input_teacher_surname.setError( getString(R.string.short_surname) );
            input_teacher_surname.requestFocus();
        } else if (teacher_patronymic.length() < 3) {
            // Слишком короткое отчество классного руководителя
            input_teacher_patronymic.setError( getString(R.string.short_patronymic) );
            input_teacher_patronymic.requestFocus();
        } else {
            try {
                // Данные валидны, заполняем форму для отправки
                String programID = programs.get(activity_program.getSelectedItemPosition()).getId();
                String scheduleID = schedules.get(program_schedule.getSelectedItemPosition()).getId();
                String scheduleString = schedules.get(program_schedule.getSelectedItemPosition()).toString();
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