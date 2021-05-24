package com.artem_obrazumov.it_cubeapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Activities.ProfileActivity;
import com.artem_obrazumov.it_cubeapp.Adapters.GroupAdapter;
import com.artem_obrazumov.it_cubeapp.Adapters.UsersAdapter;
import com.artem_obrazumov.it_cubeapp.Models.ScheduleModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupFragment extends Fragment {

    // View-элементы
    private RecyclerView groupsRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout messageLayout;
    private TextView errorMessage;

    // База данных
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    // Список с группами пользователя
    private ArrayList<ScheduleModel> groups;

    // Адаптер для списка групп
    private GroupAdapter groupsAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_groups, container, false);

        // Инициализация элементов
        groupsRecyclerView = root.findViewById(R.id.groups_list);
        progressBar = root.findViewById(R.id.progressBar);
        messageLayout = root.findViewById(R.id.message_layout);
        errorMessage = root.findViewById(R.id.nothing_found_text);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        groups = new ArrayList<>();
        groupsAdapter = new GroupAdapter();
        initializeRecyclerView();
        getGroupsList(auth.getCurrentUser().getUid());

        return root;
    }

    // Инициализация списка
    private void initializeRecyclerView() {
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsAdapter.setOnUserClickListener(new UsersAdapter.OnUserClickListener() {
            @Override
            public void onUserClicked(String userID) {
                // Переход на профиль пользователя
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                intent.putExtra("profileUid", userID);
                startActivity(intent);
            }

            @Override
            public void onUserStatusChange(String userID, int newStatus) {}

            @Override
            public void onUserBanned(String userID) {}
        });
        groupsRecyclerView.setAdapter(groupsAdapter);
    }

    // Получение групп пользователя
    private void getGroupsList(String userID) {
        UserModel.getUserQuery(userID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ArrayList<String> groupsIDs = ds.getValue(UserModel.class).getSchedulesId();
                            if (groupsIDs == null) {
                                groupsIDs = new ArrayList<>();
                            }

                            for (int i = 0; i < groupsIDs.size(); i++) {
                                ScheduleModel.getScheduleQuery(groupsIDs.get(i)).addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds: snapshot.getChildren()) {
                                                    groups.add(ds.getValue(ScheduleModel.class));
                                                }
                                                groupsAdapter.setDataSet(groups);
                                                progressBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                messageLayout.setVisibility(View.VISIBLE);
                                                errorMessage.setText(getString(R.string.load_user_data_error));
                                            }
                                        }
                                );
                            }

                            if (groupsIDs.size() == 0) {
                                progressBar.setVisibility(View.GONE);
                                messageLayout.setVisibility(View.VISIBLE);
                                errorMessage.setText(getString(R.string.no_groups));
                            } else {
                                messageLayout.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        messageLayout.setVisibility(View.VISIBLE);
                        errorMessage.setText(getString(R.string.load_user_data_error));
                    }
                }
        );
    }
}