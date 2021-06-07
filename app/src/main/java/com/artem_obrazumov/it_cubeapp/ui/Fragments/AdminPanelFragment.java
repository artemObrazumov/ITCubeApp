package com.artem_obrazumov.it_cubeapp.ui.Fragments;

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

import com.artem_obrazumov.it_cubeapp.databinding.FragmentAdminPanelBinding;
import com.artem_obrazumov.it_cubeapp.ui.Activities.CubeInfoEditActivity;
import com.artem_obrazumov.it_cubeapp.ui.Activities.NewDirectionsActivity;
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

    // Binding
    private FragmentAdminPanelBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminPanelBinding.inflate(inflater);
        View root = binding.getRoot();

        attachButtonListeners();
        loadCubeInfo();

        return root;
    }

    // Привязка слушателей к кнопкам
    private void attachButtonListeners() {
        binding.editCubeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CubeInfoEditActivity.class);
                intent.putExtra("cubeID", cubeID);
                startActivity(intent);
            }
        });

        binding.addActivities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NewDirectionsActivity.class);
                startActivity(intent);
            }
        });

        binding.watchStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("cubeID", cubeID);
                Navigation.findNavController(v).navigate(R.id.nav_students_list, bundle);
            }
        });

        binding.watchDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("cubeID", cubeID);
                Navigation.findNavController(v).navigate(R.id.nav_cube_directions_list, bundle);
            }
        });

        binding.watchRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.nav_requests_list);
            }
        });
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
                            binding.cubeAddress.setText(cube.getAddress());
                            binding.cubeDescription.setText(cube.getDescription());

                            try {
                                String firstImageLink = cube.getPhotosURLs().get(0);
                                Glide.with(getContext())
                                        .load(firstImageLink)
                                        .apply(new RequestOptions().override(900, 900))
                                        .centerCrop()
                                        .placeholder(R.color.placeholder_color)
                                        .into(binding.cubeBackgroundImage);
                            } catch (Exception ignored) {}

                            CityModel.getCityQuery(cube.getCity()).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds: snapshot.getChildren()) {
                                                binding.cubeCity.setText(ds.getValue(CityModel.class).getName());
                                                binding.panelLayout.setVisibility(View.VISIBLE);
                                                binding.loadingLayout.setVisibility(View.GONE);
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