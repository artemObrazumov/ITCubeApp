package com.artem_obrazumov.it_cubeapp.Models;

import android.widget.ArrayAdapter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;

public class ScheduleModel {
    // Поля расписания
    private String id;                                               // ID расписания
    private HashMap<String, Boolean> daysOfWeek = new HashMap<>();   // Дни занятий
    private String directionName;                                    // Название направления
    private String selectedTime;                                     // Время занятий
    private ArrayList<String> students;                              // Ученики группы

    public ScheduleModel() {
        initializeDaysOfWeekHashMap();
    }

    // Инициализация HashMap
    private void initializeDaysOfWeekHashMap() {
        daysOfWeek.put("monday", false);
        daysOfWeek.put("tuesday", false);
        daysOfWeek.put("wednesday", false);
        daysOfWeek.put("thursday", false);
        daysOfWeek.put("friday", false);
        daysOfWeek.put("saturday", false);
    }

    // Получение объекта Query для расписания по ID
    public static Query getScheduleQuery(String scheduleID) {
        // Получаем данные расписания из базы данных по ID
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Schedules");
        Query query = reference.orderByChild("id").equalTo(scheduleID);
        return query;
    }

    // Строчный вывод расписания
    @Override
    public String toString() {
        StringBuilder scheduleString = new StringBuilder();
        if (this.daysOfWeek.get("monday")) {
            scheduleString.append("Пн, ");
        }
        if (this.daysOfWeek.get("tuesday")) {
            scheduleString.append("Вт, ");
        }
        if (this.daysOfWeek.get("wednesday")) {
            scheduleString.append("Ср, ");
        }
        if (this.daysOfWeek.get("thursday")) {
            scheduleString.append("Чт, ");
        }
        if (this.daysOfWeek.get("friday")) {
            scheduleString.append("Пт, ");
        }
        if (this.daysOfWeek.get("saturday")) {
            scheduleString.append("Сб, ");
        }
        scheduleString.append(this.selectedTime);
        return scheduleString.toString();
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, Boolean> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(HashMap<String, Boolean> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public String getDirectionName() {
        return directionName;
    }

    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }

    public String getSelectedTime() {
        return selectedTime;
    }

    public void setTime(String selectedTime) {
        this.selectedTime = selectedTime;
    }

    public ArrayList<String> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<String> students) {
        this.students = students;
    }
}
