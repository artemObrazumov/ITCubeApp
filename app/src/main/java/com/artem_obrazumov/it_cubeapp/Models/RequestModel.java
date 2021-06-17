package com.artem_obrazumov.it_cubeapp.Models;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RequestModel {
    // Константы
    public static final int STATE_UNWATCHED = 0;  // Анкета не обработана
    public static final int STATE_ACCEPTED = 1;   // Анкета принята
    public static final int STATE_DENIED = 2;     // Анкета отклонена
    public static final int STATE_RESERVED = 3;   // Анкета в резерве
    public static final int STATE_ENROLLED = 4;   // Анкета обработана

    // Поля
    private String id;                                      // Идентификатор
    private String userID;                                  // ID пользователя - отправителя
    private int state;                                      // Состояние
    private String email, phone, parent_phone,              // Основные данные
        studentFullName,                                    // ФИО пользователя
        parentFullName,                                     // ФИО родителя
        teacherFullName;                                    // ФИО кл. рук.
    private String school, form;                            // Данные школы и класса
    private long dateOfBirth;                               // Дата рождения
    private String activity;                                // Направление
    private String program;                                 // Программа обучения
    private String schedule;                                // Расписание
    private String cubeID;                                  // ID Куба
    private String directionName;                           // Название направления
    private String scheduleString;                          // Расписание
    private String reason;                                  // Причина отклонения (или переноса в резерв)
    private String parentId;                                // ID родителя (если есть)

    // Конструкторы
    public RequestModel(String id, String userID, String email, String phone, String parent_phone,
                        String studentFullName, String parentFullName, String teacherFullName,
                        String school, String form, long dateOfBirth, String activity, String program,
                        String schedule, String cubeID, String directionName, String scheduleString) {
        this.id = id;
        this.userID = userID;
        this.state = STATE_UNWATCHED;
        this.email = email;
        this.phone = phone;
        this.parent_phone = parent_phone;
        this.studentFullName = studentFullName;
        this.parentFullName = parentFullName;
        this.teacherFullName = teacherFullName;
        this.school = school;
        this.form = form;
        this.dateOfBirth = dateOfBirth;
        this.activity = activity;
        this.program = program;
        this.schedule = schedule;
        this.cubeID = cubeID;
        this.directionName = directionName;
        this.scheduleString = scheduleString;
    }

    public RequestModel () {}

    // Преобразование в строку

    @Override
    public String toString() {
        String dateOfBirthStr =
                new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(dateOfBirth);

        return String.format(
                "Заявка\n" +
                        "Желаемое направление: %s по расписанию %s\n" +
                        "ФИО ученика: %s\n" +
                        "Дата рождения ученика: %s\n" +
                        "ФИО родителя: %s\n" +
                        "ФИО классного руководителя: %s\n" +
                        "Школа: %s\n" +
                        "Класс: %s\n" +
                        "КОНТАКТНЫЕ ДАННЫЕ\n" +
                        "Телефон: %s\n" +
                        "Телефон родителя: %s\n" +
                        "Email: %s",
                directionName, scheduleString,
                studentFullName, dateOfBirthStr, parentFullName,
                teacherFullName, school, form, phone, parent_phone, email
        );
    }


    // Геттеры и сеттеры

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getParent_phone() {
        return parent_phone;
    }

    public void setParent_phone(String parent_phone) {
        this.parent_phone = parent_phone;
    }

    public String getStudentFullName() {
        return studentFullName;
    }

    public void setStudentFullName(String studentFullName) {
        this.studentFullName = studentFullName;
    }

    public String getParentFullName() {
        return parentFullName;
    }

    public void setParentFullName(String parentFullName) {
        this.parentFullName = parentFullName;
    }

    public String getTeacherFullName() {
        return teacherFullName;
    }

    public void setTeacherFullName(String teacherFullName) {
        this.teacherFullName = teacherFullName;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getCubeID() {
        return cubeID;
    }

    public void setCubeID(String cubeID) {
        this.cubeID = cubeID;
    }

    public String getDirectionName() {
        return directionName;
    }

    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }

    public String getScheduleString() {
        return scheduleString;
    }

    public void setScheduleString(String scheduleString) {
        this.scheduleString = scheduleString;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
