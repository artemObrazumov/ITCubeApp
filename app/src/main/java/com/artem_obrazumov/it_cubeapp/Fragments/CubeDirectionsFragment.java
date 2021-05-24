package com.artem_obrazumov.it_cubeapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Activities.ProfileActivity;
import com.artem_obrazumov.it_cubeapp.Adapters.DirectionsListAdapter;
import com.artem_obrazumov.it_cubeapp.Adapters.UsersAdapter;
import com.artem_obrazumov.it_cubeapp.Models.DirectionModel;
import com.artem_obrazumov.it_cubeapp.Models.ITCubeModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CubeDirectionsFragment extends Fragment {

    // View-элементы
    private RecyclerView directionsRecyclerView;
    private ProgressBar progressBar;

    private DirectionsListAdapter adapter;
    private ArrayList<DirectionModel> directions;
    private ArrayList<String> directionIDs;
    private String cubeID;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cube_directions, container, false);

        // Инициализация элементов
        directionsRecyclerView = root.findViewById(R.id.directions_list);
        directionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        directionsRecyclerView.setNestedScrollingEnabled(false);

        // Создаем разделитель между направлениями
        DividerItemDecoration divider = new DividerItemDecoration(directionsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        directionsRecyclerView.addItemDecoration(divider);

        progressBar = root.findViewById(R.id.progressBar);
        cubeID = getArguments().getString("cubeID");
        adapter = new DirectionsListAdapter(new ArrayList<>());
        directionsRecyclerView.setAdapter(adapter);

        findDirectionsList(cubeID);
        return root;
    }

    // Поиск списка направлений куба
    private void findDirectionsList(String cubeID) {
        ITCubeModel.getCubeQuery(cubeID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            directionIDs = ds.getValue(ITCubeModel.class).getDirections();
                            if (directionIDs == null) {
                                directionIDs = new ArrayList<>();
                            }
                        }
                        getDirections();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                }
        );
    }

    // Получение данных о направлениях
    private void getDirections() {
        directions = new ArrayList<>();
        for (int i = 0; i < directionIDs.size(); i++) {
            DirectionModel.getDirectionQuery(directionIDs.get(i)).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                DirectionModel direction = ds.getValue(DirectionModel.class);
                                directions.add(direction);
                            }
                            adapter.setDataSet(directions);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
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

}