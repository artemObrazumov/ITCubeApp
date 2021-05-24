package com.artem_obrazumov.it_cubeapp.Activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class ProfileEditActivity extends AppCompatActivity {

    // View-элементы
    EditText input_name, input_surname;
    ImageView input_avatar;
    TextView erase_avatar, date_of_birth;

    // Окно для уведомления пользователя о смене параметров профиля
    ProgressDialog progressDialog;

    // Объекты для получения даты рождения
    Calendar dateOfBirthCalendar;
    DatePickerDialog.OnDateSetListener dateSetListener;
    Date dateOfBirthTime;

    // Firebase
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;
    String UserDatabaseID;

    // Объект, который будет хранить новую аватарку в случае необходимости
    Uri newAvatarURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Получение верхней панели
        ActionBar action_bar = getSupportActionBar();
        // Изменение названия верхней панели
        action_bar.setTitle(R.string.edit_profile);
        // Включение кнопки "назад" на верхней панели
        action_bar.setDisplayHomeAsUpEnabled(true);

        // Инициализация элементов
        input_name = findViewById(R.id.input_name);
        input_surname = findViewById(R.id.input_surname);
        input_avatar = findViewById(R.id.input_avatar);
        erase_avatar = findViewById(R.id.erase_avatar);
        date_of_birth = findViewById(R.id.date_of_birth);

        // Слушатель для изменения аватарки пользователя
        input_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAvatarUpdate();
            }
        });

        // Инициализация объектов для получения даты рождения
        dateOfBirthCalendar = Calendar.getInstance();
        dateOfBirthTime = new Date();
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                dateOfBirthCalendar.set(Calendar.YEAR, year);
                dateOfBirthCalendar.set(Calendar.MONTH, month);
                dateOfBirthCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateOfBirthTime = dateOfBirthCalendar.getTime();
                updateDateOfBirth();
            }
        };
        date_of_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Запускаем оукно для выбора даты рождения
                new DatePickerDialog(
                        ProfileEditActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        dateOfBirthCalendar.get(Calendar.YEAR),
                        dateOfBirthCalendar.get(Calendar.MONTH),
                        dateOfBirthCalendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        // Прикрепление слушателей
        erase_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EraseUserAvatar();
            }
        });

        // Инициализация Firebase
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        // Инициализируем диалоговое окно
        progressDialog = new ProgressDialog(this);

        fillViews();
    }

    // Метод для заполнения полей текущими данными пользователя
    private void fillViews() {
        UserModel.getUserQuery(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            UserDatabaseID = ds.getKey();
                            input_name.setText(ds.child("name").getValue() + "");
                            input_surname.setText(ds.child("surname").getValue() + "");
                            dateOfBirthCalendar.setTimeInMillis(Long.parseLong(ds.child("dateOfBirth").getValue() + ""));
                            dateOfBirthTime.setTime(dateOfBirthCalendar.getTimeInMillis());
                            updateDateOfBirth();

                            // Загружаем аватарку пользователя по ссылке
                            String avatarURL = ds.child("avatar").getValue()+"";
                            try {
                                Glide.with(ProfileEditActivity.this).load(avatarURL).placeholder(R.drawable.default_user_profile_icon).into(input_avatar);
                            } catch (Exception e) {
                                // Если не удалось загрузить изображение с интернета, ничего не делаем
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Устанавливаем меню для активности
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_submit:
                // Кнопка "подтвердить"
                requestDataUpdate();
                break;
            default:
                // Кнопка "назад"
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Запрашиваем разрешение на обновление данныэ
    private void requestDataUpdate() {
        new AlertDialog.Builder(this)
                .setMessage( getString(R.string.want_to_edit_profile) )
                .setCancelable(true)
                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Пользователь разрешил изменять данные аккаунта
                        UpdateData();
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
    }

    // Обновление данных о пользователе
    private void UpdateData() {
        String updated_name = input_name.getText().toString().trim();
        String updated_surname = input_surname.getText().toString().trim();
        long updated_date_of_birth = dateOfBirthCalendar.getTimeInMillis();

        HashMap<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", updated_name);
        updatedData.put("surname", updated_surname);
        updatedData.put("dateOfBirth", updated_date_of_birth);

        database.getReference("Users_data").child(UserDatabaseID)
                .updateChildren(updatedData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                });
    }

    // Удаление аватарки
    private void EraseUserAvatar() {
        HashMap<String, Object> updatedData = new HashMap<>();
        updatedData.put("avatar", getString(R.string.default_avatar_link));

        database.getReference("Users_data").child(UserDatabaseID)
                .updateChildren(updatedData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Загружаем аватарку пользователя по ссылке
                        String avatarURL = getString(R.string.default_avatar_link);
                        try {
                            Glide.with(ProfileEditActivity.this).load(avatarURL).placeholder(R.drawable.default_user_profile_icon).into(input_avatar);
                        } catch (Exception e) {
                            // Если не удалось загрузить изображение с интернета, ничего не делаем
                        }
                    }
                });
    }

    // Обновление даты рождения в строке
    private void updateDateOfBirth() {
        String dateOfBirthString = new SimpleDateFormat("dd.MM.yyyy", Locale.US).format(dateOfBirthTime);

        date_of_birth.setText(
                String.format( "Дата рождения: %s г.", dateOfBirthString )
        );
    }

    // Запрос изменения автарки
    private void requestAvatarUpdate() {
        CropImage.activity(null)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(1, 1)
                .start(this);
    }

    // Метод для загрузки новой аватарки пользователя в БД
    private void UploadAvatarToStorage(Uri avatarURI) {
        progressDialog.setMessage(getString(R.string.uploading_avatar));
        progressDialog.show();
        // Получение ключа для изображения
        String avatarID = UUID.randomUUID().toString();
        StorageReference reference = storage.getReference("avatars/" + avatarID + ".png");

        // Загрузка изображения
        reference.putFile(avatarURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // При успешной загрузке изображения получаем ссылку на него
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadURL) {
                        String newAvatarURL = downloadURL.toString();
                        updateUserAvatarInDB(newAvatarURL);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), R.string.image_load_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Метод для обновления ссылки на аватарку пользователя в БД
    private void updateUserAvatarInDB(String newAvatarURL) {
        progressDialog.setMessage(getString(R.string.loading));
        HashMap<String, Object> updatedData = new HashMap<>();
        updatedData.put("avatar", newAvatarURL);

        database.getReference("Users_data").child(UserDatabaseID)
                .updateChildren(updatedData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Аватарка успешно обновлена, изменяем ее на экране
                        Toast.makeText(getApplicationContext(), R.string.avatar_updated, Toast.LENGTH_SHORT).show();
                        try {
                            Glide.with(ProfileEditActivity.this).load(newAvatarURL).placeholder(R.drawable.default_user_profile_icon).into(input_avatar);
                        } catch (Exception e) {
                            // Если не удалось загрузить изображение с интернета, ничего не делаем
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), R.string.avatar_update_error, Toast.LENGTH_SHORT).show();
                    }
                });
        progressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Получение результата с активности
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            // Получение обрезанного изображения
            if (resultCode == RESULT_OK) {
                Uri croppedImageURI = CropImage.getActivityResult(data).getUri();
                UploadAvatarToStorage(croppedImageURI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                UploadAvatarToStorage(newAvatarURI);
            }
        }
    }
}