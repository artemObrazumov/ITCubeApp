package com.artem_obrazumov.it_cubeapp.ui.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.artem_obrazumov.it_cubeapp.Models.CityModel;
import com.artem_obrazumov.it_cubeapp.Models.DirectionModel;
import com.artem_obrazumov.it_cubeapp.Models.ITCubeModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.artem_obrazumov.it_cubeapp.databinding.ActivityProfileBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    // Binding
    private ActivityProfileBinding binding;

    // ID просматриваемого профиля
    private String profileUid;

    // База данных
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Получение верхней панели
        ActionBar action_bar = getSupportActionBar();
        // Изменение названия верхней панели
        action_bar.setTitle(R.string.profile);
        // Включение кнопки "назад" на верхней панели
        action_bar.setDisplayHomeAsUpEnabled(true);

        // Инициализация элементов
        auth = FirebaseAuth.getInstance();

        // Получаем ID просматриваемого профиля
        try {
            profileUid = getIntent().getExtras().getString("profileUid");
            if (profileUid == null) {
                throw new Exception("No ID");
            }
        } catch (Exception e) {
            // Если не удалось получить ID, то выходим из активности
            Toast.makeText(this, R.string.profile_error, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Отображаем данные пользователя
        UserModel.getUserQuery(profileUid).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            UserModel user = ds.getValue(UserModel.class);
                            binding.profileName.setText( user.getName() + " " + user.getSurname() );
                            binding.profileStatus.setText( UserModel.getUserStatus( user.getUserStatus() ) );
                            long userDateOfBirth = user.getDateOfBirth();
                            binding.profileAge.setText( getUserAge(userDateOfBirth) );
                            getCubeInfo(user.getCubeId(), user.getUserStatus());
                            getDirectionsList(user.getDirectionsID());

                            // Загружаем аватарку пользователя по ссылке
                            String avatarURL = user.getAvatar();
                            try {
                                Glide.with(ProfileActivity.this).load(avatarURL).placeholder(R.drawable.default_user_profile_icon).into(binding.profileAvatar);
                            } catch (Exception e) {
                                // Если не удалось загрузить изображение с интернета, ничего не делаем
                            }

                            // Прикрепляем слушатель нажатий на аватарку
                            binding.profileAge.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Переходим на другую активность и передаем ссылку на фото
                                    Intent intent = new Intent(getApplicationContext(), ImageViewerActivity.class);
                                    intent.putExtra("imageURL", avatarURL);
                                    startActivity(intent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Если не удалось получить данные профиля, то выходим из активности
                        Toast.makeText(getApplicationContext(), R.string.profile_error, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );
    }

    // Получение информации о направлениях пользователя
    private void getDirectionsList(ArrayList<String> directionsID) {
        for (int i = 0; i < directionsID.size(); i++) {
            DirectionModel.getDirectionQuery(directionsID.get(i)).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                String directionName = ds.getValue(DirectionModel.class).getTitle();
                                binding.userDirections.append("\n");
                                binding.userDirections.append(directionName);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    }
            );
        }
    }

    // Получение информации о кубе
    private void getCubeInfo(String cubeId, int userStatus) {
        ITCubeModel.getCubeQuery(cubeId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ITCubeModel cube = ds.getValue(ITCubeModel.class);
                            if (userStatus == UserModel.STATUS_TEACHER) {
                                binding.cubeInfo.setText(getString(R.string.teacher_in_cube) + cube.getAddress());
                                binding.userDirections.setVisibility(View.GONE);
                            } else if (userStatus == UserModel.STATUS_ADMIN) {
                                binding.cubeInfo.setText(getString(R.string.admin_in_cube) + cube.getAddress());
                                binding.userDirections.setVisibility(View.GONE);
                            } else if (userStatus == UserModel.STATUS_GLOBAL_ADMIN) {
                                binding.cubeInfo.setText(getString(R.string.platform_admin));
                                binding.userDirections.setVisibility(View.GONE);
                            } else {
                                binding.cubeInfo.setText(getString(R.string.studying_in_cube) + cube.getAddress());
                                binding.userDirections.setVisibility(View.VISIBLE);
                            }

                            if (userStatus != UserModel.STATUS_GLOBAL_ADMIN) {
                                findCityName(cube.getCity());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                }
        );
    }

    // Получение адреса куба
    private void findCityName(String cityID) {
        CityModel.getCityQuery(cityID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            String cityPart = String.format(
                                    " (г. %s)", ds.getValue(CityModel.class).getName()
                            );
                            binding.userDirections.append("\n");
                            binding.cubeInfo.append(cityPart);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Устанавливаем меню для активности, если пользователь просматривает свой аккаунт
        if (profileUid.equals(auth.getCurrentUser().getUid())) {
            getMenuInflater().inflate(R.menu.profile_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                // Кнопка "редактировать"
                startActivity(new Intent(this, ProfileEditActivity.class));
                break;
            default:
                // Кнопка "назад"
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getUserAge(long dateOfBirthInMills) {
        Calendar dateOfBirthCalendar = Calendar.getInstance();
        Calendar currentTimeCalendar = Calendar.getInstance();
        dateOfBirthCalendar.setTimeInMillis(dateOfBirthInMills);

        int userAge = currentTimeCalendar.get(Calendar.YEAR) - dateOfBirthCalendar.get(Calendar.YEAR);
        if (currentTimeCalendar.get(Calendar.DAY_OF_YEAR) < dateOfBirthCalendar.get(Calendar.DAY_OF_YEAR)){
            userAge--;
        }
        String suffix;
        if (userAge >= 11 && userAge <= 19 || userAge % 10 == 1) {
            suffix = " год";
        } else if (userAge % 10 >= 2 && userAge % 10 <= 4) {
            suffix = " года";
        } else {
            suffix = " лет";
        }

        return userAge + suffix;
    }
}