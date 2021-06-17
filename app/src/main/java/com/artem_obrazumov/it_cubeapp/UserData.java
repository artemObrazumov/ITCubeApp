package com.artem_obrazumov.it_cubeapp;

import androidx.annotation.NonNull;

import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/*
    Синглтон, созданный для сохранения некоторых данных о пользователе во время приложения.
    В большинстве случаев не рекомендуется к использованию.
 */
public class UserData {
    public static UserModel thisUser;
    public static ArrayList<UserModel> userChildrenList = new ArrayList<>();
    public static ArrayList<String> userChildrenNames = new ArrayList<>();
    public static void GetChildrenListFromDB() {
        FirebaseDatabase.getInstance().getReference("Children_list/" + thisUser.getUid())
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userChildrenList.clear();
                userChildrenNames.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    GetChildById(ds.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    public static void CreateChildrenNames() {
        userChildrenNames.clear();
        for (UserModel child : userChildrenList) {
            userChildrenNames.add(child.getName() + " " + child.getSurname());
        }
    }
    private static void GetChildById(String id) {
        UserModel.getUserQuery(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            UserModel user = ds.getValue(UserModel.class);
                            userChildrenList.add(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
