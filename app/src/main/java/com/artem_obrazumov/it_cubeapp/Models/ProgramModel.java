package com.artem_obrazumov.it_cubeapp.Models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ProgramModel {
    // Поля программы обучения
    private String id;
    private String title;
    private String description;

    // Конструкторы
    public ProgramModel(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public ProgramModel() {}

    // Получение объекта Query для программы по ID
    public static Query getProgramQuery(String programID) {
        // Получаем данные программы из базы данных по ID
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Programs");
        Query query = reference.orderByChild("id").equalTo(programID);
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


}
