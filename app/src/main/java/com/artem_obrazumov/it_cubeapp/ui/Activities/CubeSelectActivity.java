package com.artem_obrazumov.it_cubeapp.ui.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Adapters.GalleryAdapter;
import com.artem_obrazumov.it_cubeapp.Models.CityModel;
import com.artem_obrazumov.it_cubeapp.Models.ITCubeModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.artem_obrazumov.it_cubeapp.databinding.ActivityCubeSelectBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CubeSelectActivity extends AppCompatActivity {

    // Куб, выбранный в настоящее время
    private String selectedCubeID = "0";

    // Binding
    private ActivityCubeSelectBinding binding;

    // Адаптер для фото
    private GalleryAdapter adapter;

    // Массивы с городами и IT-Кубами
    private ArrayList<CityModel> cities;
    private ArrayList<ITCubeModel> itCubes;

    // Firebase
    private FirebaseDatabase database;

    // Диалоговое окно для уведомления пользователя о загрузке
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube_select);

        // Инициализация элементов
        progressDialog = new ProgressDialog(this);
        adapter = new GalleryAdapter(GalleryAdapter.MODE_HORIZONTAL);
        adapter.setOnImageClickListener(new GalleryAdapter.OnImageClickListener() {
            @Override
            public void onClick(View v, String url) {
                // Переходим на другую активность и передаем ссылку на фото
                Intent intent = new Intent(getApplicationContext(), ImageViewerActivity.class);
                intent.putExtra("imageURL", url);
                startActivity(intent);
            }
        });

        binding.selectLaterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Оставить выбор кубаа на потом
                Intent intent = new Intent();
                intent.putExtra("selectedCubeID", "0");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Возвращаем идентификатор куба
                Intent intent = new Intent();
                intent.putExtra("selectedCubeID", selectedCubeID);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        // Инициализация Firebase
        database = FirebaseDatabase.getInstance();

        initializeCitiesSpinner();
    }

    // Метод для инициализации спиннера с городами
    private void initializeCitiesSpinner() {
        progressDialog.setMessage(getString(R.string.loading_cities));
        progressDialog.show();

        cities = new ArrayList<>();
        ArrayList<String> citiesNames = new ArrayList<>();
        DatabaseReference reference = database.getReference("Cities");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    CityModel city = ds.getValue(CityModel.class);
                    cities.add(city);
                    citiesNames.add(city.getName());

                    // Устанавливаем значения в спиннере
                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_element, citiesNames);

                    binding.citiesSpinner.setAdapter(adapter);
                    binding.citiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            initializeCubesAdapter(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }
                progressDialog.hide();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Ошибка при загрузке городов, уходим с активности
                Toast.makeText(CubeSelectActivity.this, getString(R.string.cities_load_error), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // Инициализация списка с IT-Кубами
    private void initializeCubesAdapter(int position) {
        progressDialog.setMessage(getString(R.string.loading_cubes));
        progressDialog.show();

        itCubes = new ArrayList<>();
        ArrayList<String> cubesID = new ArrayList<>(cities.get(position).getCubesInCity());
        ArrayList<String> cubesAddresses = new ArrayList<>();

        // Получаем значения адресов кубов
        for (int i = 0; i < cubesID.size(); i++) {
            ITCubeModel.getCubeQuery(cubesID.get(i)).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                ITCubeModel Cube = ds.getValue(ITCubeModel.class);
                                itCubes.add(Cube);
                                cubesAddresses.add(Cube.getAddress());

                                // Устанавливаем значения в спиннере
                                ArrayAdapter<String> adapter =
                                        new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_element, cubesAddresses);
                                binding.cubesSpinner.setAdapter(adapter);

                                binding.cubesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        initializeCubePreview(position);
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                });
                            }
                            progressDialog.hide();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Ошибка при загрузке кубов, уходим с активности
                            Toast.makeText(CubeSelectActivity.this, getString(R.string.cubes_load_error), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
            );
        }
    }

    // Метод для инициализации описания куба
    private void initializeCubePreview(int pos) {
        selectedCubeID = itCubes.get(pos).getID();
        progressDialog.setMessage(getString(R.string.loading_description));
        progressDialog.show();

        ArrayList<String> imagesURLs = itCubes.get(pos).getPhotosURLs();
        adapter.setDataSet(imagesURLs);

        binding.previewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.previewImages.setAdapter(adapter);
        binding.cubePreviewText.setText(itCubes.get(pos).getDescription());

        progressDialog.hide();
    }
}