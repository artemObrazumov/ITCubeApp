package com.artem_obrazumov.it_cubeapp.Models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.*;

public class ITCubeModel {
    // Поля IT-куба
    private String ID;
    private String address;
    private String description;
    private String city;
    private ArrayList<String> Directions;
    private ArrayList<String> Groups;
    private ArrayList<String> Students;
    private ArrayList<String> PhotosURLs;

    // Конструкторы
    public ITCubeModel(String ID, String address, String description, String city, ArrayList<String> directions,
                       ArrayList<String> groups, ArrayList<String> students, ArrayList<String> photosURLs) {
        this.ID = ID;
        this.address = address;
        this.description = description;
        this.city = city;
        this.Directions = directions;
        this.Groups = groups;
        this.Students = students;
        this.PhotosURLs = photosURLs;
    }

    public ITCubeModel() {}

    // Получение объекта Query для куба по ID
    public static Query getCubeQuery(String cubeID) {
        // Получаем данные куба из базы данных по ID
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("IT_Cubes");
        Query query = reference.orderByChild("id").equalTo(cubeID);
        return query;
    }

    // Геттеры и сеттеры
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public ArrayList<String> getDirections() {
        return Directions;
    }

    public void setDirections(ArrayList<String> directions) {
        Directions = directions;
    }

    public ArrayList<String> getGroups() {
        return Groups;
    }

    public void setGroups(ArrayList<String> groups) {
        Groups = groups;
    }

    public ArrayList<String> getStudents() {
        return Students;
    }

    public void setStudents(ArrayList<String> students) {
        Students = students;
    }

    public ArrayList<String> getPhotosURLs() {
        return PhotosURLs;
    }

    public void setPhotosURLs(ArrayList<String> photosURLs) {
        PhotosURLs = photosURLs;
    }
}
