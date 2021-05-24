package com.artem_obrazumov.it_cubeapp.Models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.*;

public class CityModel {
    private String id;
    private String name;
    private ArrayList<String> cubesInCity;
    private ArrayList<String> schoolsInCity;

    // Конструкторы
    public CityModel(String id, ArrayList<String> cubesInCity, ArrayList<String> schoolsInCity, String name) {
        this.id = id;
        this.name = name;
        this.cubesInCity = cubesInCity;
        this.schoolsInCity = schoolsInCity;
    }

    public CityModel() {}

    // Получение объекта Query для куба по ID
    public static Query getCityQuery(String cityID) {
        // Получаем данные куба из базы данных по ID
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Cities");
        Query query = reference.orderByChild("id").equalTo(cityID);
        return query;
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getCubesInCity() {
        return cubesInCity;
    }

    public void setCubesInCity(ArrayList<String> cubesInCity) {
        this.cubesInCity = cubesInCity;
    }

    public ArrayList<String> getSchoolsInCity() {
        return schoolsInCity;
    }

    public void setSchoolsInCity(ArrayList<String> schoolsInCity) {
        this.schoolsInCity = schoolsInCity;
    }
}
