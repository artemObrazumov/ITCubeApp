package com.artem_obrazumov.it_cubeapp.Models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class DirectionModel {
    // Поля направления
    private String id;
    private String title;
    private String description;
    private ArrayList<String> programs;
    private ArrayList<String> schedules;

    // Конструкторы
    public DirectionModel(String id, String title, String description, ArrayList<String> programs, ArrayList<String> schedules) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.programs = programs;
        this.schedules = schedules;
    }

    public DirectionModel() {}

    // Получение объекта Query для направления по ID
    public static Query getDirectionQuery(String directionsID) {
        // Получаем данные направления из базы данных по ID
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Directions");
        Query query = reference.orderByChild("id").equalTo(directionsID);
        return query;
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getPrograms() {
        return programs;
    }

    public void setPrograms(ArrayList<String> programs) {
        this.programs = programs;
    }

    public ArrayList<String> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<String> schedules) {
        this.schedules = schedules;
    }
}
