package com.artem_obrazumov.it_cubeapp.Models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class PostModel {
    // Константы
    public static final int STATE_DRAFT = 0;       // Черновик
    public static final int STATE_PUBLISHED = 1;   // Опубликованная запись
    // Типы постов
    public static final int POST_TYPE_NEWS = 0;       // Новостной пост
    public static final int POST_TYPE_CONTEST = 1;    // Конкурс
    public static final int POST_TYPE_HACKATHON = 2;  // Хакатон

    // Поля поста
    private long uploadTime;                // Время публикации поста
    private String uid;                     // ID
    private String AuthorUid;               // ID Автора поста
    private String cubeID;                  // ID Куба
    private String title;                   // Заголовок поста
    private String description;             // Описание поста
    private int publishType;               // Тип публикации
    private ArrayList<String> imagesURLs;   // Изображения в записи

    public PostModel () {}

    public PostModel (long uploadTime, String uid, String AuthorUid, String cubeID, String title, String description, int publish_type, ArrayList<String> imagesURLs) {
        this.uploadTime = uploadTime;
        this.uid = uid;
        this.AuthorUid = AuthorUid;
        this.cubeID = cubeID;
        this.title = title;
        this.description = description;
        this.publishType = publish_type;
        this.imagesURLs = imagesURLs;
    }

    // Получение объекта Query для конкретного поста по ID
    public static Query getNewsPostQuery(String uid) {
        // Получаем данные поста из базы данных по ID
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("News_posts");
        Query query = reference.orderByChild("uid").equalTo(uid);
        return query;
    }

    public static Query getHackathonPostQuery(String uid) {
        // Получаем данные поста из базы данных по ID
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Hackathon_posts");
        Query query = reference.orderByChild("uid").equalTo(uid);
        return query;
    }

    // Получение объекта Query для конкретного поста по ID
    public static Query getContestPostQuery(String uid) {
        // Получаем данные поста из базы данных по ID
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Contest_posts");
        Query query = reference.orderByChild("uid").equalTo(uid);
        return query;
    }

    // Геттеры и сеттеры
    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAuthorUid() {
        return AuthorUid;
    }

    public void setAuthorUid(String AuthorUid) {
        this.AuthorUid = AuthorUid;
    }

    public String getCubeID() {
        return cubeID;
    }

    public void setCubeID(String cubeID) {
        this.cubeID = cubeID;
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

    public int getPublishType() {
        return publishType;
    }

    public void setPublishType(int publishType) {
        this.publishType = publishType;
    }

    public ArrayList<String> getImagesURLs() {
        return imagesURLs;
    }

    public void setImagesURLs(ArrayList<String> imagesURLs) {
        this.imagesURLs = imagesURLs;
    }
}
