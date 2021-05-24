package com.artem_obrazumov.it_cubeapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.artem_obrazumov.it_cubeapp.Adapters.ProgramsAdapter;
import com.artem_obrazumov.it_cubeapp.Adapters.ScheduleAdapter;
import com.artem_obrazumov.it_cubeapp.Models.DirectionModel;
import com.artem_obrazumov.it_cubeapp.Models.ITCubeModel;
import com.artem_obrazumov.it_cubeapp.Models.ProgramModel;
import com.artem_obrazumov.it_cubeapp.Models.ScheduleModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class NewDirectionsActivity extends AppCompatActivity {

    String cubeID = "-MX1963Jn8_pqE0e5MeE";
    //TODO: Берем ид из интента

    // View-элементы
    private EditText direction_title, direction_description;
    private RecyclerView programs_list, schedules_list;
    private Button add_program, add_schedule;

    // Адаптеры
    private ProgramsAdapter programsAdapter;
    private ScheduleAdapter schedulesAdapter;

    // Списки с добовляемыми данными направления
    private ArrayList<ProgramModel> programsList;
    private ArrayList<ScheduleModel> schedulesList;

    // База данных
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_directions);
        // Инициализация элементов
        direction_title = findViewById(R.id.input_title);
        direction_description = findViewById(R.id.input_description);
        direction_description.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        programs_list = findViewById(R.id.programs_list);
        schedules_list = findViewById(R.id.schedules_list);
        add_program = findViewById(R.id.add_program);
        add_schedule = findViewById(R.id.add_schedule);

        database = FirebaseDatabase.getInstance();

        // Инициализация кнопок добавления программ обучений и расписаний
        add_program.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                programsAdapter.addItem();
            }
        });
        add_schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                schedulesAdapter.addItem();
            }
        });

        initializeAdapters();
        setupNavBar(getString(R.string.add_direction));
    }

    // Установка верхней панели
    private void setupNavBar(String title) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
    }

    private void initializeAdapters() {
        // Инициализация адаптеров
        programsAdapter = new ProgramsAdapter(new ArrayList<>());
        programsAdapter.addItem();
        programs_list.setLayoutManager(new LinearLayoutManager(this));
        programs_list.setNestedScrollingEnabled(false);
        programs_list.setAdapter(programsAdapter);

        schedulesAdapter = new ScheduleAdapter(new ArrayList<>());
        schedulesAdapter.addItem();
        schedules_list.setLayoutManager(new LinearLayoutManager(this));
        schedules_list.setNestedScrollingEnabled(false);
        schedules_list.setAdapter(schedulesAdapter);
    }

    // Метод для получения списака программ обучения
    public ArrayList<ProgramModel> getProgramsList() throws Exception {
        ArrayList<ProgramModel> programs = new ArrayList<>();
        for (int i = 0; i < programsAdapter.getItemCount(); i++) {
            ProgramsAdapter.ViewHolder holder = (ProgramsAdapter.ViewHolder) programs_list.findViewHolderForAdapterPosition(i);
            String programTitle = holder.getInputTitle().getText().toString().trim();
            String programDescription = holder.getInputDescription().getText().toString().trim();
            if (programTitle.length() < 5) {
                holder.getInputTitle().setError(getString(R.string.short_title));
                holder.getInputTitle().requestFocus();
                throw new Exception("invalid data");
            }
            if (programDescription.length() < 20) {
                holder.getInputDescription().setError(getString(R.string.short_description));
                holder.getInputDescription().requestFocus();
                throw new Exception("invalid data");
            }
            programs.add(new ProgramModel("id", programTitle, programDescription));
        }
        return programs;
    }

    // Метод для получения списака программ обучения
    public ArrayList<ScheduleModel> getSchedulesList() throws Exception {
        ArrayList<ScheduleModel> schedules = new ArrayList<>();
        for (int i = 0; i < schedulesAdapter.getItemCount(); i++) {
            ScheduleAdapter.ViewHolder holder = (ScheduleAdapter.ViewHolder) schedules_list.findViewHolderForAdapterPosition(i);
            schedules.add(getScheduleFromHolder(holder));
        }
        return schedules;
    }

    // Метод для получения расписания из ViewHolder
    private ScheduleModel getScheduleFromHolder(ScheduleAdapter.ViewHolder holder) throws Exception {
        ScheduleModel schedule = new ScheduleModel();
        if (holder.getMonday().isChecked()) {
            schedule.getDaysOfWeek().put("monday", true);
        }
        if (holder.getTuesday().isChecked()) {
            schedule.getDaysOfWeek().put("tuesday", true);
        }
        if (holder.getWednesday().isChecked()) {
            schedule.getDaysOfWeek().put("wednesday", true);
        }
        if (holder.getThursday().isChecked()) {
            schedule.getDaysOfWeek().put("thursday", true);
        }
        if (holder.getFriday().isChecked()) {
            schedule.getDaysOfWeek().put("friday", true);
        }
        if (holder.getSaturday().isChecked()) {
            schedule.getDaysOfWeek().put("saturday", true);
        }
        schedule.setDirectionName(direction_title.getText().toString().trim());
        schedule.setTime(holder.getTimeSelect().getSelectedItem().toString());
        // Если данные невалидны (ни один день не выбран), то выкидываем ошибку
        if (!schedule.getDaysOfWeek().get("monday") &&
            !schedule.getDaysOfWeek().get("tuesday") &&
            !schedule.getDaysOfWeek().get("wednesday") &&
            !schedule.getDaysOfWeek().get("thursday") &&
            !schedule.getDaysOfWeek().get("friday") &&
            !schedule.getDaysOfWeek().get("saturday")) {
            holder.getMonday().requestFocus();
            Toast.makeText(this, getString(R.string.days_of_week_not_selected), Toast.LENGTH_SHORT).show();
            throw new Exception("Days of week aren't selected!");
        }
        return schedule;
    }

    // Метод для публикации направления
    private void submitDirection() {
        try {
            programsList = getProgramsList();
            schedulesList = getSchedulesList();
        } catch (Exception ignored) {
            return;
        }

        DirectionModel direction = new DirectionModel();
        direction.setTitle(direction_title.getText().toString().trim());
        direction.setDescription(direction_description.getText().toString().trim());

        if (!validateDirectionData(direction)) {
            return;
        }
            
        ArrayList<String> programsIDs = new ArrayList<>();
        for (int i = 0; i < programsList.size(); i++) {
            // Получаем ключ программы обучения и заносим ее в БД
            DatabaseReference reference = database.getReference("Programs");
            DatabaseReference keyReference = reference.push();
            String programId = keyReference.getKey();
            programsIDs.add(programId);
            ProgramModel program = programsList.get(i);
            program.setId(programId);
            keyReference.setValue(program);
        }
        ArrayList<String> schedulesIDs = new ArrayList<>();
        for (int i = 0; i < schedulesList.size(); i++) {
            // Получаем ключ расписания и заносим его в БД
            DatabaseReference reference = database.getReference("Schedules");
            DatabaseReference keyReference = reference.push();
            String scheduleId = keyReference.getKey();
            schedulesIDs.add(scheduleId);
            ScheduleModel schedule = schedulesList.get(i);
            schedule.setId(scheduleId);
            keyReference.setValue(schedule);
        }
        direction.setPrograms(programsIDs);
        direction.setSchedules(schedulesIDs);
        // Получаем ключ направления и заносим его в БД
        DatabaseReference reference = database.getReference("Directions");
        DatabaseReference keyReference = reference.push();
        direction.setId(keyReference.getKey());
        keyReference.setValue(direction);

        // Добавляем направление в куб
        ITCubeModel.getCubeQuery(cubeID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ITCubeModel cube = ds.getValue(ITCubeModel.class);
                    HashMap<String, Object> updatedData = new HashMap<>();
                    ArrayList<String> updatedDirectionsList = cube.getDirections();
                    if (updatedDirectionsList == null) {
                        updatedDirectionsList = new ArrayList<>();
                    }
                    updatedDirectionsList.add(direction.getId());
                    updatedData.put("directions", updatedDirectionsList);

                    database.getReference("IT_Cubes").child(cubeID).updateChildren(updatedData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    finish();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_loading), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateDirectionData(DirectionModel direction) {
        if (direction.getTitle().length() < 5) {
            direction_title.setError(getString(R.string.short_title));
            direction_title.requestFocus();
            return false;
        }
        if (direction.getDescription().length() < 50) {
            direction_description.setError(getString(R.string.short_description));
            direction_title.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_submit:
                // Кнопка публикация направления
                submitDirection();
                break;
            default:
                // Кнопка "назад"
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // При попытке закрытия экрана показываем предупреждение
        new AlertDialog.Builder(this)
                .setMessage( getString(R.string.want_to_exit) )
                .setCancelable(true)
                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Удаление поста
                        NewDirectionsActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
    }
}