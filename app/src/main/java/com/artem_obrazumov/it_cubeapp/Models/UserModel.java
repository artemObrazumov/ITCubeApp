package com.artem_obrazumov.it_cubeapp.Models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class UserModel {
    // Константы
    public static final int STATUS_STUDENT = 0;
    public static final int STATUS_PARENT = 1;
    public static final int STATUS_TEACHER = 2;
    public static final int STATUS_ADMIN = 3;
    public static final int STATUS_GLOBAL_ADMIN = 4;

    // Переменные модели пользователя
    private String uid;                        // ID пользователя
    private String name;                       // Имя пользователя
    private String surname;                    // Фамилия пользователя
    private String email;                      // Email пользователя
    private long dateOfBirth;                  // Дата рождения
    private String cubeId;                     // ID IT-Куба ученика
    private ArrayList<String> schedulesId;     // Группы пользователя
    private ArrayList<String> directionsID;    // Направления пользователя
    // Аватарка пользователя
    private String avatar = "https://firebasestorage.googleapis.com/v0/b/it-cube-app.appspot.com/o/default_user_profile_icon.jpg?alt=media&token=d67bfbf3-a20a-40ca-aa67-44366ea585f5";
    private int userStatus = STATUS_STUDENT;   // Статус пользователя

    private String parentID;                   // ID родительского аккаунта (если есть)

    // Пустой конструктор
    public UserModel () {}

    // Конструктор без аватарки
    public UserModel (String uid, String name, String surname, String email, long dateOfBirth, String cubeId, int userStatus) {
        this.uid = uid;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.cubeId = cubeId;
        this.userStatus = userStatus;
    }

    public static String getUserStatus(int status) {
        switch (status) {
            case STATUS_STUDENT:
                return "Ученик";
            case STATUS_PARENT:
                return "Родитель";
            case STATUS_TEACHER:
                return "Учитель";
            case STATUS_ADMIN:
                return "Администратор";
            case STATUS_GLOBAL_ADMIN:
                return "Глобальный администратор";
            default:
                return "Пользователь";
        }
    }

    // Получение объекта Query для конкретного пользователя по ID
    public static Query getUserQuery(String uid) {
        // Получаем данные пользователя из базы данных по ID
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Users_data");
        Query query = reference.orderByChild("uid").equalTo(uid);
        return query;
    }


    // Геттеры и сеттеры
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCubeId() {
        return cubeId;
    }

    public void setCubeId(String cubeId) {
        this.cubeId = cubeId;
    }

    public ArrayList<String> getSchedulesId() {
        return schedulesId;
    }

    public void setSchedulesId(ArrayList<String> schedulesId) {
        this.schedulesId = schedulesId;
    }

    public ArrayList<String> getDirectionsID() {
        if (directionsID == null) {
            return new ArrayList<>();
        } else {
            return directionsID;
        }
    }

    public void setDirectionsID(ArrayList<String> directionsID) {
        this.directionsID = directionsID;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }
}
