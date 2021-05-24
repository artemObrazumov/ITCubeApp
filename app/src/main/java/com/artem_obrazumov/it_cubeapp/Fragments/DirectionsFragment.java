package com.artem_obrazumov.it_cubeapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Activities.MainActivity;
import com.artem_obrazumov.it_cubeapp.Activities.MyRequestsActivity;
import com.artem_obrazumov.it_cubeapp.Activities.RequestFormActivity;
import com.artem_obrazumov.it_cubeapp.Adapters.DirectionsListAdapter;
import com.artem_obrazumov.it_cubeapp.Models.DirectionModel;
import com.artem_obrazumov.it_cubeapp.Models.ITCubeModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DirectionsFragment extends Fragment {

    // View-элементы
    RecyclerView userDirectionsList;
    RecyclerView availableDirectionsList;
    TextView directionsListTitle;
    TextView availableDirectionsListTitle;

    // Адаптеры для списков
    DirectionsListAdapter userDirectionsAdapter;
    DirectionsListAdapter availableDirectionsAdapter;

    // Firebase
    FirebaseAuth auth;
    FirebaseDatabase database;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_directions, container, false);

        // Инициализация элементов
        userDirectionsList = root.findViewById(R.id.user_directions_list);
        availableDirectionsList = root.findViewById(R.id.directions_list);
        directionsListTitle = root.findViewById(R.id.directions_list_title);
        availableDirectionsListTitle = root.findViewById(R.id.available_directions_list_title);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        initializeLists();

        return root;
    }

    // Инициализация адаптеров
    private void initializeLists() {
        UserModel.getUserQuery(auth.getCurrentUser().getUid()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            UserModel user = ds.getValue(UserModel.class);
                            initializeCoursesList(user.getDirectionsID());
                            initializeAvailableCoursesList(
                                    user.getCubeId(), user.getDirectionsID());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), getString(R.string.load_user_data_error), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // Инициализация списка с курсами, на которые записан пользователь
    private void initializeCoursesList(ArrayList<String> directionsIDs) {
        ArrayList<DirectionModel> userDirections = new ArrayList<>();
        userDirectionsAdapter = new DirectionsListAdapter(userDirections);
        userDirectionsList.setLayoutManager(new LinearLayoutManager(getContext()));
        userDirectionsList.setNestedScrollingEnabled(false);
        userDirectionsList.setAdapter(userDirectionsAdapter);
        // Создаем разделитель между направлениями
        DividerItemDecoration divider = new DividerItemDecoration(userDirectionsList.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        userDirectionsList.addItemDecoration(divider);

        // Заполнение адаптера данными о направлениях пользователя
        for (int i = 0; i < directionsIDs.size(); i++) {
            DirectionModel.getDirectionQuery(directionsIDs.get(i)).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                userDirections.add(ds.getValue(DirectionModel.class));
                            }
                            userDirectionsAdapter.setDataSet(userDirections);
                            if (userDirections.size() == 0) {
                                // Если список направлений пуст, то прячем его
                                directionsListTitle.setVisibility(View.GONE);
                                userDirectionsList.setVisibility(View.GONE);
                            } else {
                                directionsListTitle.setVisibility(View.VISIBLE);
                                userDirectionsList.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), getString(R.string.load_user_data_error), Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    // Инициализация списка с курсами, на которые может записаться пользователь
    private void initializeAvailableCoursesList(String cubeId, ArrayList<String> userDirectionsIDs) {
        ArrayList<DirectionModel> availableDirections = new ArrayList<>();
        availableDirectionsAdapter = new DirectionsListAdapter(availableDirections, DirectionsListAdapter.MODE_AVAILABLE);
        availableDirectionsAdapter.setOnDirectionClickListener(new DirectionsListAdapter.OnDirectionClickListener() {
            @Override
            public void onDirectionSubscribeButtonClicked(String directionID) {
                Intent intent = new Intent(getActivity(), RequestFormActivity.class);
                intent.putExtra("directionID", directionID);
                startActivity(intent);
            }
        });
        availableDirectionsList.setLayoutManager(new LinearLayoutManager(getContext()));
        availableDirectionsList.setNestedScrollingEnabled(false);
        availableDirectionsList.setAdapter(availableDirectionsAdapter);
        // Создаем разделитель между направлениями
        DividerItemDecoration divider = new DividerItemDecoration(availableDirectionsList.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        availableDirectionsList.addItemDecoration(divider);

        ITCubeModel.getCubeQuery(cubeId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ITCubeModel cube = ds.getValue(ITCubeModel.class);
                    ArrayList<String> cubeDirections = cube.getDirections();

                    /* Если мы видим направление, которое доступено в этом кубе, но его нет у
                       пользователя, то добавляем в список с доступными курсами */
                    for (int i = 0; i < cubeDirections.size(); i++) {
                        if (!userDirectionsIDs.contains(cubeDirections.get(i))) {
                            DirectionModel.getDirectionQuery(cubeDirections.get(i)).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                availableDirections.add(ds.getValue(DirectionModel.class));
                                            }
                                            // Инициализация адаптера полученными данными
                                            availableDirectionsAdapter.setDataSet(availableDirections);
                                            if (availableDirections.size() == 0) {
                                                // Если список доступных направлений пуст, то прячем его
                                                availableDirectionsListTitle.setVisibility(View.GONE);
                                                availableDirectionsList.setVisibility(View.GONE);
                                            } else {
                                                availableDirectionsListTitle.setVisibility(View.VISIBLE);
                                                availableDirectionsList.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), getString(R.string.load_user_data_error), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), getString(R.string.load_user_data_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Добавление уникальных объектов в меню фрагмента
        inflater.inflate(R.menu.directions_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_view_requests) {
            startActivity(new Intent(getContext(), MyRequestsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}