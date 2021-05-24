package com.artem_obrazumov.it_cubeapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.artem_obrazumov.it_cubeapp.Activities.CubeInfoEditActivity;
import com.artem_obrazumov.it_cubeapp.Activities.NewDirectionsActivity;
import com.artem_obrazumov.it_cubeapp.Models.CityModel;
import com.artem_obrazumov.it_cubeapp.Models.ITCubeModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AdminPanelFragment extends Fragment {

    private String cubeID = "-MX1963Jn8_pqE0e5MeE";

    // View-элементы
    private TextView cubeAddress, cubeCity, cubeDescription,
            editCubeInfo, addActivities, watchStudents, watchDirections, watchRequest;
    private ImageView cubeBackgroundImage;
    private ConstraintLayout panel_layout, loading_layout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_panel, container, false);

        // Инициализация кнопок
        cubeAddress = root.findViewById(R.id.cube_address);
        cubeCity = root.findViewById(R.id.cube_city);
        cubeDescription = root.findViewById(R.id.cube_description);
        editCubeInfo = root.findViewById(R.id.edit_cube_info);
        addActivities = root.findViewById(R.id.add_activities);
        watchStudents = root.findViewById(R.id.watch_students);
        watchDirections = root.findViewById(R.id.watch_directions);
        watchRequest = root.findViewById(R.id.watch_request);
        cubeBackgroundImage = root.findViewById(R.id.cube_background_image);

        panel_layout = root.findViewById(R.id.panel_layout);
        loading_layout = root.findViewById(R.id.loading_layout);

        // Привязка слушателей к кнопкам
        editCubeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CubeInfoEditActivity.class);
                intent.putExtra("cubeID", cubeID);
                startActivity(intent);
            }
        });

        addActivities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NewDirectionsActivity.class);
                startActivity(intent);
            }
        });

        watchStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("cubeID", cubeID);
                Navigation.findNavController(v).navigate(R.id.nav_students_list, bundle);
            }
        });

        watchDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("cubeID", cubeID);
                Navigation.findNavController(v).navigate(R.id.nav_cube_directions_list, bundle);
            }
        });

        watchRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.nav_requests_list);
            }
        });

        loadCubeInfo();

        return root;
    }

    // Загрузка информации о кубе
    private void loadCubeInfo() {
        ITCubeModel.getCubeQuery(cubeID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ITCubeModel cube = ds.getValue(ITCubeModel.class);
                            // Установка данных о кубе
                            cubeAddress.setText(cube.getAddress());
                            cubeDescription.setText(cube.getDescription());

                            try {
                                String firstImageLink = cube.getPhotosURLs().get(0);
                                Glide.with(getContext())
                                        .load(firstImageLink)
                                        .apply(new RequestOptions().override(900, 900))
                                        .centerCrop()
                                        .placeholder(R.color.placeholder_color)
                                        .into(cubeBackgroundImage);
                            } catch (Exception ignored) {}

                            CityModel.getCityQuery(cube.getCity()).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds: snapshot.getChildren()) {
                                                cubeCity.setText(ds.getValue(CityModel.class).getName());
                                                panel_layout.setVisibility(View.VISIBLE);
                                                loading_layout.setVisibility(View.GONE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), getString(R.string.unable_to_load_panel), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), getString(R.string.unable_to_load_panel), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

}