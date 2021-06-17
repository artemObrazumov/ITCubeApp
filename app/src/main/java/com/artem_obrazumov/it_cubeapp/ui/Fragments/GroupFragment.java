package com.artem_obrazumov.it_cubeapp.ui.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.artem_obrazumov.it_cubeapp.UserData;
import com.artem_obrazumov.it_cubeapp.databinding.FragmentGroupsBinding;
import com.artem_obrazumov.it_cubeapp.ui.Activities.ProfileActivity;
import com.artem_obrazumov.it_cubeapp.Adapters.GroupAdapter;
import com.artem_obrazumov.it_cubeapp.Adapters.UsersAdapter;
import com.artem_obrazumov.it_cubeapp.Models.ScheduleModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupFragment extends Fragment {

    // Binding
    private FragmentGroupsBinding binding;

    // Firebase
    private FirebaseAuth auth;

    // Список с группами пользователя
    private ArrayList<ScheduleModel> groups;

    // Адаптер для списка групп
    private GroupAdapter groupsAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGroupsBinding.inflate(inflater);
        View root = binding.getRoot();

        // Инициализация элементов
        auth = FirebaseAuth.getInstance();

        groups = new ArrayList<>();
        groupsAdapter = new GroupAdapter();
        initializeRecyclerView();
        if (UserData.thisUser.getUserStatus() == UserModel.STATUS_PARENT) {
            // Если пользователь - родитель, то он выбирает ребенка, группы которого он хочет посмотреть
            getGroupsListAsParent();
        } else {
            getGroupsList(auth.getCurrentUser().getUid());
        }

        return root;
    }

    // Инициализация списка
    private void initializeRecyclerView() {
        binding.groupsList.setLayoutManager(new LinearLayoutManager(getContext()));
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
        binding.groupsList.setAdapter(groupsAdapter);
    }

    private void getGroupsListAsParent() {
        UserData.CreateChildrenNames();
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.select_child))
                .setItems(UserData.userChildrenNames.toArray(new String[0]),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getGroupsList(UserData.userChildrenList.get(which).getUid());
                            }
                        })
                .create().show();
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
                                                binding.progressBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                binding.messageLayout.setVisibility(View.VISIBLE);
                                                binding.nothingFoundText.setText(getString(R.string.load_user_data_error));
                                            }
                                        }
                                );
                            }

                            if (groupsIDs.size() == 0) {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.messageLayout.setVisibility(View.VISIBLE);
                                binding.nothingFoundText.setText(getString(R.string.no_groups));
                            } else {
                                binding.messageLayout.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.messageLayout.setVisibility(View.VISIBLE);
                        binding.nothingFoundText.setText(getString(R.string.load_user_data_error));
                    }
                }
        );
    }
}