package com.artem_obrazumov.it_cubeapp.Models;

import java.util.ArrayList;

public class ContestPostModel extends PostModel{
    // Поля, специфичные для конкурсных записей
    String link;            // Ссылка на мероприятие
    long openUntil;         // Время, до которого запись будет активна

    // Конструкторы
    public ContestPostModel() {}

    public ContestPostModel(String link, long openUntil) {
        this.link = link;
        this.openUntil = openUntil;
    }

    public ContestPostModel(long uploadTime, String uid, String AuthorUid, String cubeID, String title,
                            String description, int publish_type, ArrayList<String> imagesURLs, String link,
                            long openUntil) {
        super(uploadTime, uid, AuthorUid, cubeID, title, description, publish_type, imagesURLs);
        this.link = link;
        this.openUntil = openUntil;
    }

    // Геттеры и сеттеры
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getOpenUntil() {
        return openUntil;
    }

    public void setOpenUntil(long openUntil) {
        this.openUntil = openUntil;
    }
}
