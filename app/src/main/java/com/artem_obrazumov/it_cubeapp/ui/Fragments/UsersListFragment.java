package com.artem_obrazumov.it_cubeapp.ui.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.ui.Activities.ProfileActivity;
import com.artem_obrazumov.it_cubeapp.Adapters.UsersAdapter;
import com.artem_obrazumov.it_cubeapp.Models.ITCubeModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersListFragment extends Fragment {

    // View-элементы
    private RecyclerView usersRecyclerView;

    private UsersAdapter adapter;
    private String cubeID;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_users_list, container, false);
        try {
            usersRecyclerView = root.findViewById(R.id.UsersList);
            cubeID = getArguments().getString("cubeID");
            adapter = new UsersAdapter(new ArrayList<>());
            setUsersListListener();
            findStudentsList(cubeID);
        } catch (Exception e) {
            Navigation.findNavController(getView()).navigateUp();
            Toast.makeText(getContext(), getString(R.string.loading_students_error), Toast.LENGTH_SHORT).show();
        }
        return root;
    }

    // Установка слушателя для списка пользователей
    private void setUsersListListener() {
        UsersAdapter.OnUserClickListener listener = new UsersAdapter.OnUserClickListener() {
            @Override
            public void onUserClicked(String userID) {
                // Переход на профиль пользователя
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                intent.putExtra("profileUid", userID);
                startActivity(intent);
            }

            @Override
            public void onUserStatusChange(String userID, int newStatus) {

            }

            @Override
            public void onUserBanned(String userID) {

            }
        };

        adapter.setOnUserClickListener(listener);
    }

    private void findStudentsList(String cubeID) {
        ITCubeModel.getCubeQuery(cubeID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ArrayList<String> studentsIDs =
                                    ds.getValue(ITCubeModel.class).getStudents();
                            setupStudentsList(studentsIDs);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Navigation.findNavController(getView()).navigateUp();
                        Toast.makeText(getContext(), getString(R.string.loading_students_error), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupStudentsList(ArrayList<String> studentsIDs) {
        adapter.getDataSet().clear();
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecyclerView.setAdapter(adapter);

        for (int i = 0; i < studentsIDs.size(); i++) {
            UserModel.getUserQuery(studentsIDs.get(i)).addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                UserModel user = ds.getValue(UserModel.class);
                                adapter.getDataSet().add(user);
                                adapter.copyBackup();
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Navigation.findNavController(getView()).navigateUp();
                            Toast.makeText(getContext(), getString(R.string.loading_students_error), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Перемещение назад при нажатии кнопки
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(getView()).navigateUp();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.searchbar_menu, menu);
        SearchView searchBar = (SearchView) menu.findItem(R.id.search_bar).getActionView();
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.filter(query);
                return true;
            }
        });
    }
}